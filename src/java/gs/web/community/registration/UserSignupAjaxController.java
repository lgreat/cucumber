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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@RequestMapping("/community/registration")
public class UserSignupAjaxController {
    protected final Log _log = LogFactory.getLog(getClass());

    @Autowired
    private ITableDao _tableDao;

    @Autowired
    private IUserDao _userDao;

    public static final String SPREADSHEET_ID_FIELD = "ip";

    @RequestMapping(value="/ajaxJoin.page", method=RequestMethod.POST)
    public String handleJoin(ModelMap modelMap, RegistrationHoverCommand command, HttpServletRequest request, HttpServletResponse response) {

        if (isIPBlocked(request)) {

        };

        RegistrationHoverCommand userCommand = (RegistrationHoverCommand) command;
        User user = getUserDao().findUserFromEmailIfExists(userCommand.getEmail());
        OmnitureTracking ot = new CookieBasedOmnitureTracking(request, response);
        boolean userExists = (user != null);

        if (userExists) {
            setFieldsOnUserUsingCommand(userCommand, user);
            userCommand.setUser(user);
        } else {
            // only create the user if the user is new
            getUserDao().saveUser(userCommand.getUser());
            user = userCommand.getUser();
        }

        setUsersPassword(user, userCommand, userExists);
        updateUserProfile(user, userCommand, ot);

        user.setHow(userCommand.joinTypeToHow());

        // save
        getUserDao().updateUser(user);

        return null;
    }

    protected void setUsersPassword(User user, UserCommand userCommand, boolean userExists) throws Exception {
        try {
            user.setPlaintextPassword(userCommand.getPassword());
            _userDao.updateUser(user);
        } catch (Exception e) {
            _log.warn("Error setting password: " + e.getMessage(), e);
            if (!userExists) {
                // for new users, cancel the account on error
                _userDao.removeUser(user.getId());
            }
            throw e;
        }
    }

    protected void setFieldsOnUserUsingCommand(RegistrationHoverCommand userCommand, User user) {
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
