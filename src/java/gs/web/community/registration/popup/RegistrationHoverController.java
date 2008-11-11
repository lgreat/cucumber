package gs.web.community.registration.popup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import gs.web.util.ReadWriteController;
import gs.web.util.NewSubscriberDetector;
import gs.web.util.PageHelper;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.validator.UserCommandHoverValidator;
import gs.web.community.registration.RegistrationController;
import gs.web.community.registration.UserCommand;
import gs.web.tracking.OmnitureSuccessEvent;
import gs.data.community.*;
import gs.data.soap.SoapRequestException;
import gs.data.soap.CreateOrUpdateUserRequestBean;
import gs.data.soap.CreateOrUpdateUserRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RegistrationHoverController extends RegistrationController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());

    public static final String BEAN_ID = "/community/registration/popup/registrationHover.page";

    //set up defaults if none supplied
    protected void onBindOnNewForm(HttpServletRequest request,
                                   Object command,
                                   BindException errors) {

        RegistrationHoverCommand userCommand = (RegistrationHoverCommand) command;
        loadCityList(request, userCommand);

        if (request.getParameter("msl") != null) {
            userCommand.setMslOnly(true);
        }
    }

    public void onBind(HttpServletRequest request, Object command) {
        UserCommand userCommand = (UserCommand) command;
        userCommand.setCity(request.getParameter("city"));
        String terms = request.getParameter(TERMS_PARAMETER);
        userCommand.setTerms("on".equals(terms));
        String newsletter = request.getParameter(NEWSLETTER_PARAMETER);
        userCommand.setNewsletter("on".equals(newsletter));

        loadCityList(request, userCommand);
    }

    public void onBindAndValidate(HttpServletRequest request, Object command, BindException errors) throws Exception {
        UserCommandHoverValidator validator = new UserCommandHoverValidator();
        validator.setUserDao(getUserDao());
        validator.validate(request, command, errors);
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {

        // First, check to see if the request is from a blocked IP address. If so,
        // then, log the attempt and show the error view.
        String requestIP = (String)request.getAttribute("HTTP_X_CLUSTER_CLIENT_IP");
        if (StringUtils.isBlank(requestIP) || StringUtils.equalsIgnoreCase("undefined", requestIP)) {
            requestIP = request.getRemoteAddr();
        }
        try {
            if (getTableDao().getFirstRowByKey(SPREADSHEET_ID_FIELD, requestIP) != null) {
                _log.warn("Request from blocked IP Address: " + requestIP);
                return new ModelAndView(getErrorView());
            }
        } catch (Exception e) {
            _log.warn("Error checking IP address", e);
        }

        UserCommand userCommand = (UserCommand) command;
        User user = getUserDao().findUserFromEmailIfExists(userCommand.getEmail());

        OmnitureSuccessEvent ose = new OmnitureSuccessEvent(request, response);

        boolean userExists = false;

        if (user != null) {
            userExists = true;
            userCommand.setUser(user);
        } else {
            // only create the user if the user is new
            getUserDao().saveUser(userCommand.getUser());
            user = userCommand.getUser();
        }

        try {
            user.setPlaintextPassword(userCommand.getPassword());
            getUserDao().updateUser(userCommand.getUser());
        } catch (Exception e) {
            _log.warn("Error setting password: " + e.getMessage());
            if (!userExists) {
                // for new users, cancel the account on error
                getUserDao().removeUser(user.getId());
            }
            throw e;
        }

        UserProfile userProfile;
        if (user.getUserProfile() != null && user.getUserProfile().getId() != null) {
            // hack to get provisional accounts working in least amount of development time
            userProfile = user.getUserProfile();
            userProfile.setCity(userCommand.getCity());
            userProfile.setScreenName(userCommand.getScreenName());
            userProfile.setState(userCommand.getState());
        } else {
            // gotten this far, now let's update their user profile
            userProfile = userCommand.getUserProfile();

            userProfile.setUser(user);
            user.setUserProfile(userProfile);

            ose.add(OmnitureSuccessEvent.SuccessEvent.CommunityRegistration);
        }

        // set up defaults for data not collected in hover registration
        if (StringUtils.isEmpty(user.getGender())) {
            user.setGender("u");    
        }
        user.getUserProfile().setNumSchoolChildren(0);

        getUserDao().updateUser(user);

        ModelAndView mAndV = new ModelAndView();


        // only subscribe to newsletter on final step
        if (userCommand.getNewsletter()) {
            List<Subscription> subs = new ArrayList<Subscription>();

            Subscription communityNewsletterSubscription = new Subscription();
            communityNewsletterSubscription.setUser(user);
            communityNewsletterSubscription.setProduct(SubscriptionProduct.PARENT_ADVISOR);
            communityNewsletterSubscription.setState(userCommand.getState());
            subs.add(communityNewsletterSubscription);

            NewSubscriberDetector.notifyOmnitureWhenNewNewsLetterSubscriber(user, ose);
            getSubscriptionDao().addNewsletterSubscriptions(user, subs);
        }
        try {
            notifyCommunity(user.getId(), userProfile.getScreenName(), user.getEmail(),
                    user.getPasswordMd5(), userProfile.getUpdated(), request);
        } catch (SoapRequestException couure) {
            _log.error("SOAP error - " + couure.getErrorCode() + ": " + couure.getErrorMessage());
            // undo registration
            user.setEmailProvisional(userCommand.getPassword());
            getUserDao().updateUser(user);
            // send to error page
            mAndV.setViewName(getErrorView());
            return mAndV; // early exit!
        }

        if (!user.isEmailProvisional()) {
            try {
                // registration is done, let's send a confirmation email
                getRegistrationConfirmationEmail().sendToUser(user, userCommand.getPassword(), request);
            } catch (Exception ex) {
                _log.error("Error sending community registration confirmation email to " +
                        user);
                _log.error(ex);
            }
        }
        PageHelper.setMemberAuthorized(request, response, user); // auto-log in to community

        mAndV.setViewName("redirect:/community/registration/popup/sendToDestination.page");

        return mAndV;
    }

    protected void notifyCommunity(Integer userId, String screenName, String email, String password,
                                   Date dateCreated,
                                   HttpServletRequest request) throws SoapRequestException {
        String requestIP = (String)request.getAttribute("HTTP_X_CLUSTER_CLIENT_IP");
        if (StringUtils.isBlank(requestIP) || StringUtils.equalsIgnoreCase("undefined", requestIP)) {
            requestIP = request.getRemoteAddr();
        }
        CreateOrUpdateUserRequestBean bean = new CreateOrUpdateUserRequestBean
                (userId, screenName, email, password, dateCreated, requestIP);
        CreateOrUpdateUserRequest soapRequest = getSoapRequest();
        if (!UrlUtil.isDeveloperWorkstation(request.getServerName())) {
            soapRequest.setTarget("http://" +
                    SessionContextUtil.getSessionContext(request).getSessionContextUtil().getCommunityHost(request) +
                    "/soap/user");
        }
        soapRequest.createOrUpdateUserRequest(bean);
    }
}
