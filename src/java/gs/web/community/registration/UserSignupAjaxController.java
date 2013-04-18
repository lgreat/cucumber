package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.community.WelcomeMessageStatus;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.state.State;
import gs.data.util.table.ITableDao;
import gs.web.community.registration.popup.RegistrationHoverCommand;
import gs.web.tracking.CookieBasedOmnitureTracking;
import gs.web.tracking.OmnitureTracking;
import gs.web.util.SitePrefCookie;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlUtil;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.validation.Valid;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@RequestMapping("/community/registration")
public class UserSignupAjaxController {
    protected final Log _log = LogFactory.getLog(getClass());

    @Autowired
    private ITableDao _tableDao;

    @Autowired
    private IUserDao _userDao;

    public static final String SPREADSHEET_ID_FIELD = "ip";


    /*@InitBinder(value = COMMAND)
    public void customizeConversions(WebDataBinder binder) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(df, true));
    }*/

    @RequestMapping(value="/basicRegistration.page", method=RequestMethod.POST)
    public String handleJoin(
            ModelMap modelMap,
            @Valid UserRegistrationCommand userRegistrationCommand,
            UserSubscriptionCommand userSubscriptionCommand,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {

        if (isIPBlocked(request)) {

        };

        User user = getUserDao().findUserFromEmailIfExists(userRegistrationCommand.getEmail());

        boolean userExists = (user != null);

        if (!userExists) {
            // only create the user if the user is new
            user = createUser(userRegistrationCommand);

            if ("facebook".equals(userRegistrationCommand.getHow())) {
                user.setWelcomeMessageStatus(WelcomeMessageStatus.NEED_TO_SEND);
                user.setEmailVerified(true);
                user.setEmailValidated();
            }

            getUserDao().saveUser(user);

            try {
                if ("facebook".equals(userRegistrationCommand.getHow())) {
                    user.setPlaintextPassword(RandomStringUtils.random(24));
                } else {
                    user.setPlaintextPassword(userRegistrationCommand.getPassword());
                }

                UserProfile profile = createUserProfile(userRegistrationCommand, user);
                user.setUserProfile(profile);
                getUserDao().updateUser(user);

            } catch (NoSuchAlgorithmException e) {
                getUserDao().removeUser(user.getId());
            } catch (IllegalStateException e) {
                getUserDao().removeUser(user.getId());
            }
        }

        return "yay";
    }

    protected User createUser(UserRegistrationCommand userCommand) {
        User user = new User();

        user.setEmail(userCommand.getEmail());

        user.setStateAsString(userCommand.getState().getAbbreviation());

        user.setFirstName(userCommand.getFirstName());

        user.setLastName(userCommand.getLastName());

        user.setGender(userCommand.getGender());

        user.setTimeAdded(new Date());

        user.setHow(userCommand.getHow());

        return user;
    }

    public UserProfile createUserProfile(UserRegistrationCommand userRegistrationCommand, User user) {
        UserProfile profile = new UserProfile();

        profile.setScreenName(userRegistrationCommand.getScreenName());

        profile.setState(userRegistrationCommand.getState());

        profile.setCity(userRegistrationCommand.getCity());

        profile.setHow(userRegistrationCommand.getHow());

        profile.setUpdated(new Date());

        profile.setUser(user);

        return profile;

    }

    protected boolean isIPBlocked(HttpServletRequest request) {
        // First, check to see if the request is from a blocked IP address. If so,
        // then, log the attempt and show the error view.
        String requestIP = (String) request.getAttribute("HTTP_X_CLUSTER_CLIENT_IP");
        if (StringUtils.isBlank(requestIP) || StringUtils.equalsIgnoreCase("undefined", requestIP)) {
            requestIP = request.getRemoteAddr();
        }
        try {
            if (_tableDao.getFirstRowByKey(SPREADSHEET_ID_FIELD, requestIP) != null) {
                _log.warn("Request from blocked IP Address: " + requestIP);
                return true;
            }
        } catch (Exception e) {
            _log.warn("Error checking IP address", e);
        }
        return false;
    }

    protected UserProfile updateUserProfile(User user, RegistrationHoverCommand userCommand, OmnitureTracking ot) {
        UserProfile userProfile;
        if (user.getUserProfile() != null && user.getUserProfile().getId() != null) {
            // hack to get provisional accounts working in least amount of development time
            userProfile = user.getUserProfile();
            if (StringUtils.isBlank(userCommand.getCity())) {
                userProfile.setCity(null);
            } else {
                userProfile.setCity(userCommand.getCity());
            }
            userProfile.setScreenName(userCommand.getScreenName());
            userProfile.setState(userCommand.getState());
            userProfile.setHow(userCommand.joinTypeToHow());
        } else {
            // gotten this far, now let's update their user profile

            userProfile = userCommand.getUserProfile();
            userProfile.setHow(userCommand.joinTypeToHow());
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

    public ITableDao getTableDao() {
        return _tableDao;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }
}
