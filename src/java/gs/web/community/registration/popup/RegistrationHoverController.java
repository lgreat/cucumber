package gs.web.community.registration.popup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import gs.web.util.ReadWriteController;
import gs.web.util.PageHelper;
import gs.web.util.validator.UserCommandHoverValidator;
import gs.web.community.registration.RegistrationController;
import gs.web.tracking.OmnitureSuccessEvent;
import gs.data.community.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    public void onBindAndValidate(HttpServletRequest request, Object command, BindException errors) throws Exception {
        UserCommandHoverValidator validator = new UserCommandHoverValidator();
        validator.setUserDao(getUserDao());
        validator.validate(request, command, errors);
    }

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {
        if (isIPBlocked(request)) return new ModelAndView(getErrorView());

        RegistrationHoverCommand userCommand = (RegistrationHoverCommand) command;
        User user = getUserDao().findUserFromEmailIfExists(userCommand.getEmail());
        ModelAndView mAndV = new ModelAndView();
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

        setUsersPassword(user, userCommand, userExists);
        updateUserProfile(user, userCommand, ose);
        // set up defaults for data not collected in hover registration
        if (StringUtils.isEmpty(user.getGender())) {
            user.setGender("u");    
        }

        // save
        getUserDao().updateUser(user);

        // subscribe to newsletters
        if (userCommand.getNewsletter()) {
            processNewsletterSubscriptions(user, userCommand, ose);
        }
        notifyCommunity(user, userCommand, mAndV, request);

        if (!user.isEmailProvisional()) {
            sendConfirmationEmail(user, userCommand, request);
        }
        PageHelper.setMemberAuthorized(request, response, user); // auto-log in to community

        mAndV.setViewName("redirect:/community/registration/popup/sendToDestination.page");

        return mAndV;
    }

    protected UserProfile updateUserProfile(User user, RegistrationHoverCommand userCommand, OmnitureSuccessEvent ose) {
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
        if (!StringUtils.isBlank(userCommand.getHow())) {
            user.getUserProfile().setHow(userCommand.getHow());
        }
        user.getUserProfile().setUpdated(new Date());
        user.getUserProfile().setNumSchoolChildren(0);        
        return userProfile;
    }
}
