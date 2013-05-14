package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.community.WelcomeMessageStatus;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableDaoFactory;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Set;

@Service
public class UserRegistrationOrLoginService {

    private final Log _log = LogFactory.getLog(getClass());

    private static final String SPREADSHEET_ID_FIELD = "ip";

    private ITableDaoFactory _tableDaoFactory;

    private ITableDao _tableDao;

    @Autowired
    private LocalValidatorFactoryBean _validatorFactory;

    @Autowired
    private IUserDao _userDao;

    @Autowired
    @Qualifier("emailVerificationEmail")
    private EmailVerificationEmail _emailVerificationEmail;

    /**
     * Returns an UserStateStruct object.First if there is a user in the session then return that.
     * Else check if the user is trying to log in. If the log in credentials are valid then log in the user.
     * Else create a brand new user.
     *
     * @param userRegistrationCommand
     * @param userLoginCommand
     * @param registrationBehavior
     * @param bindingResult
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public UserStateStruct getUserStateStruct(UserRegistrationCommand userRegistrationCommand,
                                              UserLoginCommand userLoginCommand,
                                              RegistrationBehavior registrationBehavior,
                                              BindingResult bindingResult,
                                              HttpServletRequest request,
                                              HttpServletResponse response) throws Exception {
        User user = null;
        if (isIPBlocked(request)) {
            _log.error("Ip is blocked while registering or logging in the user.");
            return null;
        }

        user = getUserFromSession(registrationBehavior, request, response);
        if (user != null) {
            UserStateStruct userStateStruct = new UserStateStruct();
            userStateStruct.setUserInSession(true);
            userStateStruct.setUser(user);
            return userStateStruct;
        }

        user = loginUser(userLoginCommand, registrationBehavior, bindingResult, request, response);
        if (user != null) {
            UserStateStruct userStateStruct = new UserStateStruct();
            if (user.isEmailValidated() && user.matchesPassword(userLoginCommand.getPassword())) {
                userStateStruct.setUserLoggedIn(true);
            } else if (!user.isEmailValidated() && registrationBehavior.sendVerificationEmail()) {
                userStateStruct.setVerificationEmailSent(true);
            }
            userStateStruct.setUser(user);
            return userStateStruct;
        }

        user = registerUser(userRegistrationCommand, registrationBehavior, bindingResult, request, response);
        if (user != null) {
            UserStateStruct userStateStruct = new UserStateStruct();
            userStateStruct.setUserRegistered(true);
            userStateStruct.setUser(user);
            return userStateStruct;
        }
        return null;
    }

    /**
     * Get the user from a session
     *
     * @param registrationBehavior
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public User getUserFromSession(RegistrationBehavior registrationBehavior,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        if (sessionContext != null) {
            User user = sessionContext.getUser();
            if (user != null) {
                return user;
            }
        }
        return null;
    }

    /**
     * Signs in the user, if the user is email validated and the command object has the right credentials.
     *
     * @param userLoginCommand
     * @param registrationBehavior
     * @param bindingResult
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public User loginUser(UserLoginCommand userLoginCommand, RegistrationBehavior registrationBehavior,
                          BindingResult bindingResult,
                          HttpServletRequest request,
                          HttpServletResponse response) {

        Set<ConstraintViolation<UserLoginCommand>> emailValidationErrors = _validatorFactory.validate(userLoginCommand, UserLoginCommand.ValidateJustEmail.class);
        if (!emailValidationErrors.isEmpty()) {
            _log.error("Validation Errors while logging in user.");
            return null;
        }

        User user = getUserDao().findUserFromEmailIfExists(userLoginCommand.getEmail());
        if (user != null) {
            if (user.isEmailValidated()) {

                Set<ConstraintViolation<UserLoginCommand>> loginErrors = _validatorFactory.validate(userLoginCommand, UserLoginCommand.ValidateLoginCredentials.class);
                if (!loginErrors.isEmpty()) {
                    _log.error("Validation Errors while logging in user.");
                    return user;
                }
                try {
                    if (user.matchesPassword(userLoginCommand.getPassword())) {
                        PageHelper.setMemberAuthorized(request, response, user, true);
                    }
                } catch (NoSuchAlgorithmException ex) {
                    _log.error("Error while trying to log in the user." + ex);
                    return null;
                }
            } else {
                if (registrationBehavior.sendVerificationEmail()) {
                    sendValidationEmail(request, user, registrationBehavior.getRedirectUrl());
                }
            }
            return user;
        }

        return null;
    }

    /**
     * Creates a new user.
     *
     * @param userRegistrationCommand
     * @param registrationBehavior
     * @param bindingResult
     * @param request
     * @param response
     * @return
     * @throws Exception
     */

    public User registerUser(UserRegistrationCommand userRegistrationCommand, RegistrationBehavior registrationBehavior,
                             BindingResult bindingResult,
                             HttpServletRequest request,
                             HttpServletResponse response) {

        _validatorFactory.validate(userRegistrationCommand, bindingResult);

        if (bindingResult.hasErrors()) {
            _log.error("Validation Errors while registering a user.");
        } else {
            boolean userExists = false;
            User user = getUserDao().findUserFromEmailIfExists(userRegistrationCommand.getEmail());
            if (user == null) {
                try {
                    user = createNewUser(userRegistrationCommand, registrationBehavior);

                    getUserDao().saveUser(user);

                    setUsersPassword(user, userRegistrationCommand, registrationBehavior, userExists);

                    user.setUserProfile(createNewUserProfile(userRegistrationCommand, registrationBehavior, user));
                    user.getUserProfile().setUser(user);

                    getUserDao().updateUser(user);

                    ThreadLocalTransactionManager.commitOrRollback();
                    // User object loses its session and this might fix that.
                    user = getUserDao().findUserFromId(user.getId());

                    if (registrationBehavior.sendVerificationEmail()) {
                        sendValidationEmail(request, user, registrationBehavior.getRedirectUrl());
                    }

                } catch (NoSuchAlgorithmException e) {
                    _log.error("Error while registering a user." + e);
                    getUserDao().removeUser(user.getId());
                    user = null;
                } catch (IllegalStateException e) {
                    _log.error("Error while registering a user." + e);
                    getUserDao().removeUser(user.getId());
                    user = null;
                } catch (Exception e) {
                    _log.error("Error while registering a user." + e);
                    getUserDao().removeUser(user.getId());
                    user = null;
                }

                return user;
            }
        }
        return null;
    }

    /**
     * Method to read the fields from the command and set them on the user object.
     *
     * @param userCommand
     * @param registrationBehavior
     * @return
     */

    public User createNewUser(UserRegistrationCommand userCommand, RegistrationBehavior registrationBehavior) {
        User user = new User();

        user.setEmail(userCommand.getEmail());

        if (userCommand.getState() != null) {
            user.setStateAsString(userCommand.getState().getAbbreviation());
        }

        if (userCommand.getFirstName() != null) {
            user.setFirstName(userCommand.getFirstName());
        }

        if (userCommand.getLastName() != null) {
            user.setLastName(userCommand.getLastName());
        }

        if (userCommand.getGender() != null) {
            user.setGender(userCommand.getGender());
        }

        if (!registrationBehavior.requireEmailVerification()) {
            user.setEmailVerified(true);
        }

        if (registrationBehavior.sendConfirmationEmail()) {
            user.setWelcomeMessageStatus(WelcomeMessageStatus.NEED_TO_SEND);
        } else {
            user.setWelcomeMessageStatus(WelcomeMessageStatus.NEVER_SEND);
        }

        user.setTimeAdded(new Date());

        user.setHow(userCommand.getHow());

        return user;
    }

    /**
     * Method to read the fields from the command and set them on the user profile object.
     *
     * @param userRegistrationCommand
     * @param registrationBehavior
     * @param user
     * @return
     */

    public UserProfile createNewUserProfile(UserRegistrationCommand userRegistrationCommand, RegistrationBehavior registrationBehavior, User user) {
        UserProfile profile = new UserProfile();

        profile.setHow(userRegistrationCommand.getHow());

        if (userRegistrationCommand.getState() != null) {
            profile.setState(userRegistrationCommand.getState());
        }

        if (userRegistrationCommand.getCity() != null) {
            profile.setCity(userRegistrationCommand.getCity());
        }

        if (StringUtils.isEmpty(userRegistrationCommand.getScreenName())) {
            profile.setScreenName("user" + user.getId());
        } else {
            profile.setScreenName(userRegistrationCommand.getScreenName());
        }

        Date now = new Date();
        profile.setCreated(now);

        return profile;
    }

    /**
     * Set User's password
     *
     * @param user
     * @param userRegistrationCommand
     * @param registrationBehavior
     * @param userExists
     * @throws Exception
     */

    public void setUsersPassword(User user, UserRegistrationCommand userRegistrationCommand, RegistrationBehavior registrationBehavior,
                                 boolean userExists) throws Exception {
        setUsersPassword(user, userRegistrationCommand.getPassword(), registrationBehavior.requireEmailVerification(), userExists);
    }

    /**
     * Set user's password
     *
     * @param user
     * @param password
     * @param requireEmailValidation
     * @param userExists
     * @throws Exception
     */
    public void setUsersPassword(User user, String password, boolean requireEmailValidation, boolean userExists) throws Exception {
        try {
            user.setPlaintextPassword(password);
            if (requireEmailValidation) {
                // mark account as provisional if we require email validation
                user.setEmailProvisional(password);
            }
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

    /**
     * Method to send the validation email.
     *
     * @param request
     * @param user
     * @param redirectUrl
     */

    protected void sendValidationEmail(HttpServletRequest request, User user, String redirectUrl) {
        sendValidationEmail(request, user, redirectUrl, false);
    }

    /**
     * Method to send the validation email.
     *
     * @param request
     * @param user
     * @param redirectUrl
     */
    protected void sendValidationEmail(HttpServletRequest request, User user, String redirectUrl,
                                       boolean schoolReviewFlow) {
        if (user != null && StringUtils.isNotBlank(redirectUrl)) {
            try {
                if (schoolReviewFlow) {
                    _emailVerificationEmail.sendSchoolReviewVerificationEmail(request, user, redirectUrl);
                } else {
                    _emailVerificationEmail.sendVerificationEmail(request, user, redirectUrl);
                }
            } catch (Exception e) {
                _log.error("Error sending email message: " + e, e);
            }
        }
    }

    /**
     * Method to check if a request is from a blocked IP.
     *
     * @param request
     * @return
     */
    public boolean isIPBlocked(HttpServletRequest request) {
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


    public void setPollFactory(ITableDaoFactory _tableDaoFactory) {
        _tableDao = _tableDaoFactory.getTableDao();
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public LocalValidatorFactoryBean getValidatorFactory() {
        return _validatorFactory;
    }

    public void setValidatorFactory(LocalValidatorFactoryBean validatorFactory) {
        _validatorFactory = validatorFactory;
    }

    public void setEmailVerificationEmail(EmailVerificationEmail emailVerificationEmail) {
        _emailVerificationEmail = emailVerificationEmail;
    }

    /**
     * A structure to reflect if a user was obtained from the session,or was logged in or was a new user created.
     */
    public class UserStateStruct {
        private boolean isUserLoggedIn = false;
        private boolean isUserRegistered = false;
        private boolean isUserInSession = false;
        private boolean isVerificationEmailSent = false;
        private User user;

        public boolean isUserLoggedIn() {
            return isUserLoggedIn;
        }

        public void setUserLoggedIn(boolean userLoggedIn) {
            isUserLoggedIn = userLoggedIn;
        }

        public boolean isUserRegistered() {
            return isUserRegistered;
        }

        public void setUserRegistered(boolean userRegistered) {
            isUserRegistered = userRegistered;
        }

        public boolean isUserInSession() {
            return isUserInSession;
        }

        public void setUserInSession(boolean userInSession) {
            isUserInSession = userInSession;
        }

        public boolean isVerificationEmailSent() {
            return isVerificationEmailSent;
        }

        public void setVerificationEmailSent(boolean verificationEmailSent) {
            isVerificationEmailSent = verificationEmailSent;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }
}