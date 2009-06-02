package gs.web.community.registration.popup;

import gs.web.community.registration.RegistrationController;
import gs.web.community.registration.UserCommand;
import gs.web.tracking.OmnitureTracking;
import gs.web.tracking.CookieBasedOmnitureTracking;
import gs.web.util.PageHelper;
import gs.web.util.UrlUtil;
import gs.web.util.NewSubscriberDetector;
import gs.web.util.context.SessionContextUtil;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.community.User;
import gs.data.community.Subscription;
import gs.data.community.SubscriptionProduct;
import gs.data.school.School;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class SchoolInterruptRegistrationHoverController extends RegistrationController {

    @Override
    protected boolean hasChildRows() {
        return false;
    }

    protected void loadMssSchoolName(UserCommand userCommand) {
        if (userCommand.getMystatSchoolId() > 0 && userCommand.getMystatSchoolState() != null) {
            School mssSchool =
                    getSchoolDao().getSchoolById(userCommand.getMystatSchoolState(), userCommand.getMystatSchoolId());
            userCommand.setMystatSchoolName(mssSchool.getName());
            userCommand.setState(mssSchool.getDatabaseState());
            userCommand.setCity(mssSchool.getCity());
        }
    }

    @Override
    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
        loadMssSchoolName((UserCommand) command);
        return super.referenceData(request, command, errors);
    }

    @Override
    public void onBind(HttpServletRequest request, Object command) {
        super.onBind(request, command);
        // make sure state/city are set correctly in userCommand
        loadMssSchoolName((UserCommand) command);        
    }

    @Override
    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {
        if (isIPBlocked(request)) return new ModelAndView(getErrorView());

        UserCommand userCommand = (UserCommand) command;
        User user = getUserDao().findUserFromEmailIfExists(userCommand.getEmail());
        ModelAndView mAndV = new ModelAndView();
        OmnitureTracking ot = new CookieBasedOmnitureTracking(request, response);
        boolean userExists = false;

        if (user != null) {
            userExists = true;
            // update the user's name if they specified a new one
            if (StringUtils.isNotEmpty(userCommand.getFirstName())) {
                user.setFirstName(userCommand.getFirstName());
            }
            if (StringUtils.isNotEmpty(userCommand.getLastName())) {
                user.setLastName(userCommand.getLastName());
            }
            String gender = userCommand.getGender();
            if (StringUtils.isNotEmpty(gender)) {
                user.setGender(userCommand.getGender());
            }
            userCommand.setUser(user);
        } else {
            // only create the user if the user is new
            getUserDao().saveUser(userCommand.getUser());
            user = userCommand.getUser();
            user.setGender("u");
        }

        setUsersPassword(user, userCommand, userExists);

        if (isRequireEmailValidation()) {
            sendValidationEmail(user, userCommand, userExists, request);
        }

        updateUserProfile(user, userCommand, ot);

        if (user.isEmailProvisional()) {
            user.setEmailValidated();
        }

        // save
        getUserDao().updateUser(user);
        // Because of hibernate caching, it's possible for a list_active record
        // (with list_member id) to be commited before the list_member record is
        // committed. Adding this commitOrRollback prevents this.
        ThreadLocalTransactionManager.commitOrRollback();

        try {
            if (userCommand.getPartnerNewsletter()) {
                Subscription subscription = new Subscription();
                subscription.setUser(user);
                subscription.setProduct(SubscriptionProduct.SPONSOR_OPT_IN);
                subscription.setState(userCommand.getUserProfile().getState());
                userCommand.addSubscription(subscription);
            }
            if (userCommand.isMystat()) {
                // subscribe to greatnews and mss for the school
                // greatnews
                processNewsletterSubscriptions(user, userCommand, ot);
            }

            saveSubscriptionsForUser(userCommand, user, request, response);
        } catch (Exception e) {
            // if there is any sort of error prior to notifying community,
            // the user MUST BE ROLLED BACK to provisional status
            // otherwise our database is out of sync with community! Bad!
            _log.error("Unexpected error during registration", e);
            // undo registration
            user.setEmailProvisional(userCommand.getPassword());
            getUserDao().updateUser(user);
            // send to error page
            mAndV.setViewName(getErrorView());
            return mAndV;
        }
        if (!notifyCommunity(user, userCommand, mAndV, request)) {
            return mAndV; // early exit!
        }
        if (!user.isEmailProvisional()) {
            if (!isChooserRegistration()) {
                sendConfirmationEmail(user, userCommand, request);
            }
        }

        PageHelper.setMemberAuthorized(request, response, getUserDao().findUserFromEmailIfExists(userCommand.getEmail())); // auto-log in to community
        if ((StringUtils.isEmpty(userCommand.getRedirectUrl()) ||
                !UrlUtil.isCommunityContentLink(userCommand.getRedirectUrl()))) {
            String redirectUrl = "http://" +
                    SessionContextUtil.getSessionContext(request).getSessionContextUtil().getCommunityHost(request) +
                    "/members/" + user.getUserProfile().getScreenName() + "/profile/interests?registration=1";
            userCommand.setRedirectUrl(redirectUrl);
        }
        mAndV.setViewName("redirect:" + userCommand.getRedirectUrl());

        return mAndV;
    }

    @Override
    protected void processNewsletterSubscriptions(User user, UserCommand userCommand, OmnitureTracking ot) {
        List<Subscription> subs = new ArrayList<Subscription>();
        // greatnews
        Subscription communityNewsletterSubscription = new Subscription();
        communityNewsletterSubscription.setUser(user);
        communityNewsletterSubscription.setProduct(SubscriptionProduct.PARENT_ADVISOR);
        communityNewsletterSubscription.setState(userCommand.getState());
        userCommand.addSubscription(communityNewsletterSubscription);

        // mss
        if (userCommand.getMystatSchoolId() > 0 && userCommand.getMystatSchoolState() != null) {
            School school = getSchoolDao().getSchoolById(userCommand.getMystatSchoolState(),
                    userCommand.getMystatSchoolId());
            Subscription mss = new Subscription(user, SubscriptionProduct.MYSTAT, school);
            mss.setUpdatedNow();
            userCommand.addSubscription(mss);
        }
    }
}
