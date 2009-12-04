package gs.web.community.registration.popup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import gs.web.util.ReadWriteController;
import gs.web.util.validator.UserCommandHoverValidator;
import gs.web.community.registration.RegistrationController;
import gs.web.community.registration.UserCommand;
import gs.web.tracking.OmnitureTracking;
import gs.web.tracking.CookieBasedOmnitureTracking;
import gs.data.community.*;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * // TODO: This class is deprecated and may (should?) no longer be in use
 * @author greatschools.org>
 * @deprecated
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
        OmnitureTracking ot = new CookieBasedOmnitureTracking(request, response);
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
        updateUserProfile(user, userCommand, ot);
        // set up defaults for data not collected in hover registration
        if (StringUtils.isEmpty(user.getGender())) {
            user.setGender("u");    
        }

        // GS-7861 Fail-safe mechanism to ensure user is validated
        if (user.isEmailProvisional()) {
            user.setEmailValidated();
        }
        // save
        getUserDao().updateUser(user);

        try {
            // GS-7649 Because of hibernate caching, it's possible for a list_active record
            // (with list_member id) to be commited before the list_member record is
            // committed. Adding this commitOrRollback prevents this.
            ThreadLocalTransactionManager.commitOrRollback();

            // subscribe to newsletters
            if (userCommand.getNewsletter()) {
                processNewsletterSubscriptions(userCommand);
            }
            saveSubscriptionsForUser(userCommand, ot);
            ot.addEvar(new OmnitureTracking.Evar(OmnitureTracking.EvarNumber.RegistrationSegment, "MSL Combo Reg"));
        } catch (Exception e) {
            // if there is any sort of error prior to notifying community,
            // the user MUST BE ROLLED BACK to provisional status
            // otherwise our database is out of sync with community! Bad!
            _log.error("Unexpected error during hover registration", e);
            // undo registration
            user.setEmailProvisional(userCommand.getPassword());
            getUserDao().updateUser(user);
            // send to error page
            mAndV.setViewName(getErrorView());
            return mAndV;
        }
        notifyCommunity(user, userCommand, mAndV, request);

        mAndV.setViewName("redirect:/community/registration/popup/sendToDestination.page");

        return mAndV;
    }

    protected void setUsersPassword(User user, UserCommand userCommand, boolean userExists) throws Exception {
        try {
            user.setPlaintextPassword(userCommand.getPassword());
            getUserDao().updateUser(user);
        } catch (Exception e) {
            _log.warn("Error setting password: " + e.getMessage(), e);
            if (!userExists) {
                // for new users, cancel the account on error
                getUserDao().removeUser(user.getId());
            }
            throw e;
        }
    }

    protected UserProfile updateUserProfile(User user, RegistrationHoverCommand userCommand, OmnitureTracking ot) {
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

            ot.addSuccessEvent(OmnitureTracking.SuccessEvent.CommunityRegistration);
        }
        if (!StringUtils.isBlank(userCommand.getHow())) {
            user.getUserProfile().setHow(userCommand.getHow());
        }
        user.getUserProfile().setUpdated(new Date());
        user.getUserProfile().setNumSchoolChildren(0);        
        return userProfile;
    }
}
