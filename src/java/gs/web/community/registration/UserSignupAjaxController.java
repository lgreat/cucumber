package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.community.WelcomeMessageStatus;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.util.table.ITableDao;
import gs.web.authorization.Facebook;
import gs.web.authorization.FacebookRequestData;
import gs.web.community.HoverHelper;
import gs.web.util.*;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;

import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.validation.Valid;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Map;

@Controller
@RequestMapping("/community/registration")
public class UserSignupAjaxController implements ReadWriteAnnotationController {
    protected final Log _log = LogFactory.getLog(getClass());

    private ITableDao _tableDao;

    @Autowired
    private IUserDao _userDao;

    private ExactTargetAPI _exactTargetAPI;

    @Autowired
    private LocalValidatorFactoryBean _validatorFactory;

    private String EMAIL_MODEL_KEY = "email";
    private String SCREEN_NAME_MODEL_KEY = "screenName";
    private String USER_ID_MODEL_KEY = "userId";
    private String NUMBER_MSL_ITEMS_MODEL_KEY = "numberMSLItems";
    private String MODEL_ACCOUNT_CREATED_KEY = "GSAccountCreated";

    public static final String SPREADSHEET_ID_FIELD = "ip";

    @RequestMapping(value="/basicRegistration.json", method=RequestMethod.POST)
    public View handleJoin(
            ModelMap modelMap,
            @Valid UserRegistrationCommand userRegistrationCommand,
            BindingResult bindingResult,
            UserSubscriptionCommand userSubscriptionCommand,
            RegistrationBehavior registrationBehavior,
            HttpServletRequest request,
            HttpServletResponse response

    ) throws Exception {
        View view = new MappingJacksonJsonView();

        _validatorFactory.validate(userRegistrationCommand, bindingResult);

        if (bindingResult.hasErrors()) {
            modelMap.put("errors", bindingResult.getAllErrors());
            return new MappingJacksonJsonView();
        }

        /*if (isIPBlocked(request)) {

        };*/

        User user = getUserDao().findUserFromEmailIfExists(userRegistrationCommand.getEmail());

        boolean userExists = (user != null);

        if (userExists) {
            // If the user had previously created a GS account but not verified their email, we'll take care of that
            // now.
            if (user.isEmailProvisional()) {
                user.setEmailVerified(true);
                user.setEmailValidated();
                if (user.getWelcomeMessageStatus().equals(WelcomeMessageStatus.DO_NOT_SEND)) {
                    user.setWelcomeMessageStatus(WelcomeMessageStatus.NEED_TO_SEND);
                }
            }

            if (registrationBehavior.isFacebookRegistration()) {
                view = doSocialSignon(request, response, user);
            }
        } else {
            // only create the user if the user is new
            user = createUser(userRegistrationCommand);

            if (!registrationBehavior.requireEmailVerification()) {
                user.setEmailVerified(true);
            }

            if (registrationBehavior.sendConfirmationEmail()) {
                user.setWelcomeMessageStatus(WelcomeMessageStatus.NEED_TO_SEND);
            }

            getUserDao().saveUser(user);

            try {
                if (registrationBehavior.isFacebookRegistration()) {
                    user.setUserProfile(createUserProfile(userRegistrationCommand));
                    user.getUserProfile().setUser(user);
                    user.getUserProfile().setScreenName("user" + user.getId());
                    user.setPlaintextPassword(RandomStringUtils.randomAlphanumeric(24));
                } else {
                    user.setPlaintextPassword(userRegistrationCommand.getPassword());
                }

                getUserDao().updateUser(user);

                modelMap.put(MODEL_ACCOUNT_CREATED_KEY, "true");
            } catch (NoSuchAlgorithmException e) {
                getUserDao().removeUser(user.getId());
            } catch (IllegalStateException e) {
                getUserDao().removeUser(user.getId());
            }

            PageHelper.setMemberAuthorized(request, response, user);
        }

        modelMap.put(USER_ID_MODEL_KEY, user.getId());
        if (user.getUserProfile() != null) {
            modelMap.put(SCREEN_NAME_MODEL_KEY, user.getUserProfile().getScreenName());
        }
        modelMap.put(EMAIL_MODEL_KEY, user.getEmail());
        modelMap.put(NUMBER_MSL_ITEMS_MODEL_KEY, user.getFavoriteSchools() != null? user.getFavoriteSchools().size() : 0 );

        return view;
    }

    public View doSocialSignon(HttpServletRequest request, HttpServletResponse response, User user) {

        FacebookRequestData facebookRequestData = Facebook.getRequestData(request);

        if (facebookRequestData.isOwnedBy(user)) {
            // log user in
            try {
                PageHelper.setMemberAuthorized(request, response, user);
            } catch (NoSuchAlgorithmException e) {
                return new MappingJacksonJsonView();
            }
            return new MappingJacksonJsonView();
        }

        return new MappingJacksonJsonView();
    }

    protected User createUser(UserRegistrationCommand userCommand) {
        User user = new User();

        user.setEmail(userCommand.getEmail());

        if (userCommand.getState() != null) {
            user.setStateAsString(userCommand.getState().getAbbreviation());
        }

        user.setFirstName(userCommand.getFirstName());

        user.setLastName(userCommand.getLastName());

        user.setGender(userCommand.getGender());

        user.setTimeAdded(new Date());

        user.setHow(userCommand.getHow());

        user.setFacebookId(userCommand.getFacebookId());

        return user;
    }

    public UserProfile createUserProfile(UserRegistrationCommand userRegistrationCommand) {
        UserProfile profile = new UserProfile();

        profile.setScreenName(userRegistrationCommand.getScreenName());

        if (userRegistrationCommand.getState() != null) {
            profile.setState(userRegistrationCommand.getState());
        }

        if (userRegistrationCommand.getCity() != null) {
            profile.setCity(userRegistrationCommand.getCity());
        }

        profile.setHow(userRegistrationCommand.getHow());

        profile.setUpdated(new Date());

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

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public ITableDao getTableDao() {
        return _tableDao;
    }

    public void setTableDao(ITableDao tableDao) {
        _tableDao = tableDao;
    }

    public ExactTargetAPI getExactTargetAPI() {
        return _exactTargetAPI;
    }

    public void setExactTargetAPI(ExactTargetAPI exactTargetAPI) {
        _exactTargetAPI = exactTargetAPI;
    }
}
