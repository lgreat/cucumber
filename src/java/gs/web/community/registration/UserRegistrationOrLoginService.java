package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.community.WelcomeMessageStatus;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableDaoFactory;
import gs.web.auth.FacebookHelper;
import gs.web.auth.FacebookSession;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.RandomStringUtils;
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
import java.util.HashMap;
import java.util.Map;
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
     * Returns an UserRegistrationOrLoginSummary object that reflects if the user was in the session or logged in or registered.
     * First if there is a user in the session then return that.
     * Else check if the user is trying to log in. If the log in credentials are valid then log in the user.
     * Else create a brand new user.
     *
     */
    public Summary loginOrRegister(UserRegistrationCommand userRegistrationCommand,
                                   UserLoginCommand userLoginCommand,
                                   RegistrationOrLoginBehavior registrationOrLoginBehavior,
                                   BindingResult bindingResult,
                                   HttpServletRequest request,
                                   HttpServletResponse response) throws Exception {
        User user = null;
        if (isIPBlocked(request)) {
            _log.warn("Ip is blocked while registering or logging in the user.");
            return null;
        }

        Summary summary;

        if (registrationOrLoginBehavior.isFacebookRegistration()) {
            user = loginFacebookUser(userRegistrationCommand, registrationOrLoginBehavior, bindingResult, request, response);
        }

        if (user != null) {
            summary = new Summary();
            if (user.isEmailValidated() && user.matchesPassword(userLoginCommand.getPassword())) {
                summary.setWasUserLoggedIn(true);
            } else if (!user.isEmailValidated() && registrationOrLoginBehavior.sendVerificationEmail()) {
                summary.setWasVerificationEmailSent(true);
            }
            summary.setUser(user);
            return summary;
        }

        summary = registerUser(userRegistrationCommand, registrationOrLoginBehavior, bindingResult, request);
        if (summary != null && summary.wasUserRegistered()) {
            return summary;
        }
        return null;
    }

    /**
     * Get the user from a session
     *
     */
    public Summary getUserFromSession(HttpServletRequest request) {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        if (sessionContext != null) {
            User user = sessionContext.getUser();
            if (user != null) {
                UserRegistrationOrLoginService.Summary summary = new UserRegistrationOrLoginService.Summary();
                summary.setWasUserInSession(true);
                summary.setUser(user);
                return summary;
            }
        }
        return null;
    }

    public User loginFacebookUser(UserRegistrationCommand userRegistrationCommand,
                                  RegistrationOrLoginBehavior registrationOrLoginBehavior,
                                  BindingResult bindingResult,
                                  HttpServletRequest request,
                                  HttpServletResponse response) throws Exception{

        if (isIPBlocked(request)) {
            _log.warn("Ip is blocked while registering or logging in the user.");
            return null;
        }

        _validatorFactory.validate(userRegistrationCommand, bindingResult);

        if (bindingResult.hasErrors()) {
            _log.error("Validation Errors while logging in user.");
            return null;
        }

        User user = getUserDao().findUserFromEmailIfExists(userRegistrationCommand.getEmail());
        if (user != null) {
            FacebookSession facebookSession = FacebookHelper.getFacebookSession(request);

            // If facebook session isn't null, then the signed request was properly decrypted
            if (facebookSession != null) {
                boolean authenticated = (
                    (user.isFacebookUser() && facebookSession.isOwnedBy(user))
                    || !user.isFacebookUser()
                );

                if (authenticated) {
                    try {
                        PageHelper.setMemberAuthorized(request, response, user, true);
                    } catch (NoSuchAlgorithmException ex) {
                        _log.error("Error while trying to log in the user." + ex);
                    }
                }

                if (!user.isFacebookUser()) {
                    boolean modified = convertToFacebookAccountIfNeeded(user, request, userRegistrationCommand, registrationOrLoginBehavior);

                    if (modified) {
                        _userDao.saveUser(user);
                        ThreadLocalTransactionManager.commitOrRollback();
                    }
                }
            }

            return user;
        }

        return null;
    }

    /**
     * Signs in the user, if the user is email validated and the command object has the right credentials.
     *
     */
    public Summary loginUser(UserLoginCommand userLoginCommand,
                             HttpServletRequest request,
                             HttpServletResponse response) throws Exception {

        if (isIPBlocked(request)) {
            _log.warn("Ip is blocked while registering or logging in the user.");
            return null;
        }

        Set<ConstraintViolation<UserLoginCommand>> emailValidationErrors = _validatorFactory.validate(userLoginCommand, UserLoginCommand.ValidateJustEmail.class);
        if (!emailValidationErrors.isEmpty()) {
            _log.error("Validation Errors while logging in user.");
            return null;
        }

        User user = getUserDao().findUserFromEmailIfExists(userLoginCommand.getEmail());

        if (user != null) {
            UserRegistrationOrLoginService.Summary summary = new UserRegistrationOrLoginService.Summary();
            summary.setUser(user);

            if (user.isEmailValidated()) {
                Set<ConstraintViolation<UserLoginCommand>> loginErrors = _validatorFactory.validate(userLoginCommand, UserLoginCommand.ValidateLoginCredentials.class);

                if (!loginErrors.isEmpty()) {
                    _log.error("Validation Errors while logging in user.");
                    return summary;
                }
                try {
                    if (user.matchesPassword(userLoginCommand.getPassword())) {
                        PageHelper.setMemberAuthorized(request, response, user, true);
                        summary.setWasUserLoggedIn(true);
                    }
                } catch (NoSuchAlgorithmException ex) {
                    _log.error("Error while trying to log in the user." + ex);
                }
            }
            return summary;
        }

        return null;
    }

    /**
     * Sends Verification email if the user is email provisional.
     */
    public Summary sendVerificationEmail(UserLoginCommand userLoginCommand, RegistrationOrLoginBehavior registrationOrLoginBehavior,
                                         HttpServletRequest request) {

        Set<ConstraintViolation<UserLoginCommand>> emailValidationErrors = _validatorFactory.validate(userLoginCommand, UserLoginCommand.ValidateJustEmail.class);
        if (!emailValidationErrors.isEmpty()) {
            _log.error("Validation Errors Sending verification email.");
            return null;
        }
        User user = getUserDao().findUserFromEmailIfExists(userLoginCommand.getEmail());

        if (user != null && user.isEmailProvisional()) {
            UserRegistrationOrLoginService.Summary summary = new UserRegistrationOrLoginService.Summary();
            summary.setUser(user);
            sendValidationEmail(request, user, registrationOrLoginBehavior);
            summary.setWasVerificationEmailSent(true);
            return summary;
        }
        return null;
    }

    /**
     * Registers a new user.
     */
    public Summary registerUser(UserRegistrationCommand userRegistrationCommand, RegistrationOrLoginBehavior registrationOrLoginBehavior,
                                BindingResult bindingResult,
                                HttpServletRequest request) {

        if (isIPBlocked(request)) {
            _log.warn("Ip is blocked while registering or logging in the user.");
            return null;
        }

        _validatorFactory.validate(userRegistrationCommand, bindingResult);

        if (bindingResult.hasErrors()) {
            _log.error("Validation Errors while registering a user.");
        } else {
            UserRegistrationOrLoginService.Summary summary = new UserRegistrationOrLoginService.Summary();
            boolean userExists = false;
            User user = getUserDao().findUserFromEmailIfExists(userRegistrationCommand.getEmail());

            //If there was no user or if the user is email only user.
            if (user == null || (user != null && user.isPasswordEmpty())) {
                try {

                    if (user == null) {
                        user = createNewUser(userRegistrationCommand, registrationOrLoginBehavior);
                    } else {
                        //Email only users.
                        userExists = true;
                        setAttributesOnUser(user, userRegistrationCommand, registrationOrLoginBehavior);
                    }

                    getUserDao().saveUser(user);
                    ThreadLocalTransactionManager.commitOrRollback();
                    // User object loses its session and this might fix that.
                    user = getUserDao().findUserFromId(user.getId());

                    if (registrationOrLoginBehavior.isFacebookRegistration()) {
                        user.setFacebookId(userRegistrationCommand.getFacebookId());
                        String password = RandomStringUtils.randomAlphanumeric(14);
                        setUsersPassword(user, password, registrationOrLoginBehavior.requireEmailVerification(), userExists);
                    } else if (userRegistrationCommand.getPassword() != null) {
                        setUsersPassword(user, userRegistrationCommand, registrationOrLoginBehavior, userExists);
                    }

                    if (user.getUserProfile() == null) {
                        user.setUserProfile(createNewUserProfile(userRegistrationCommand, registrationOrLoginBehavior, user));
                        user.getUserProfile().setUser(user);
                    }

                    getUserDao().updateUser(user);
                    if (registrationOrLoginBehavior.sendVerificationEmail()) {
                        sendValidationEmail(request, user, registrationOrLoginBehavior);
                    }
                    summary.setUser(user);
                    summary.setWasUserRegistered(true);

                } catch (NoSuchAlgorithmException e) {
                    _log.error("Error while registering a user." + e);
                    if (user != null) {
                        getUserDao().removeUser(user.getId());
                        user = null;
                        summary.setUser(user);
                        summary.setWasUserRegistered(false);
                    }
                } catch (IllegalStateException e) {
                    _log.error("Error while registering a user." + e);
                    if (user != null) {
                        getUserDao().removeUser(user.getId());
                        user = null;
                        summary.setUser(user);
                        summary.setWasUserRegistered(false);
                    }
                } catch (Exception e) {
                    _log.error("Error while registering a user." + e);
                    if (user != null) {
                        getUserDao().removeUser(user.getId());
                        user = null;
                        summary.setUser(user);
                        summary.setWasUserRegistered(false);
                    }
                }
            } else if (user != null) {
                summary.setUser(user);
            }
            return summary;
        }
        return null;
    }

    /**
     * Creates a new User object
     */
    protected User createNewUser(UserRegistrationCommand userCommand, RegistrationOrLoginBehavior registrationOrLoginBehavior) {
        User user = new User();

        user.setEmail(userCommand.getEmail());

        setAttributesOnUser(user, userCommand, registrationOrLoginBehavior);

        return user;
    }

    /**
     * Method to read the fields from the command and set them on the user object.
     *
     */
    protected void setAttributesOnUser(User user, UserRegistrationCommand userCommand, RegistrationOrLoginBehavior registrationOrLoginBehavior) {

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

        if (!registrationOrLoginBehavior.requireEmailVerification()) {
            user.setEmailVerified(true);
        }

        if (registrationOrLoginBehavior.sendConfirmationEmail()) {
            user.setWelcomeMessageStatus(WelcomeMessageStatus.NEED_TO_SEND);
        } else {
            user.setWelcomeMessageStatus(WelcomeMessageStatus.NEVER_SEND);
        }

        //GS-14425 SS: I need to be able to have welcomeMessageStatus set DO_NOT_SEND here. Adding this now since
        // it shouldnt affect anyone else's code
        if (registrationOrLoginBehavior.getWelcomeMessageStatus() != null) {
            user.setWelcomeMessageStatus(registrationOrLoginBehavior.getWelcomeMessageStatus());
        }

        if (userCommand.getFacebookId() != null) {
            user.setFacebookId(userCommand.getFacebookId());
        }

        user.setTimeAdded(new Date());

        user.setHow(userCommand.getHow());
    }

    /**
     * Method to read the fields from the command and set them on the user profile object.
     *
     */

    protected UserProfile createNewUserProfile(UserRegistrationCommand userRegistrationCommand, RegistrationOrLoginBehavior registrationOrLoginBehavior, User user) {
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
     */

    protected void setUsersPassword(User user, UserRegistrationCommand userRegistrationCommand, RegistrationOrLoginBehavior registrationOrLoginBehavior,
                                 boolean userExists) throws Exception {
        setUsersPassword(user, userRegistrationCommand.getPassword(), registrationOrLoginBehavior.requireEmailVerification(), userExists);
    }

    /**
     * Set user's password
     *
     */
    protected void setUsersPassword(User user, String password, boolean requireEmailValidation, boolean userExists) throws Exception {
        try {
            user.setPlaintextPassword(password);
            if (requireEmailValidation) {
                // mark account as provisional if we require email validation
                user.setEmailProvisional(password);
            }
            getUserDao().updateUser(user);
        } catch (Exception e) {
            _log.error("Error setting password: " + e.getMessage(), e);
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
     */

    protected void sendValidationEmail(HttpServletRequest request, User user, RegistrationOrLoginBehavior registrationOrLoginBehavior) {
        if (registrationOrLoginBehavior.getSchool() != null) {
            Map<String, String> otherParams = new HashMap<String, String>();
            otherParams.put("schoolId", registrationOrLoginBehavior.getSchool().getId().toString());
            otherParams.put("state", registrationOrLoginBehavior.getSchool().getDatabaseState().toString());
            sendValidationEmail(request, user, registrationOrLoginBehavior.getRedirectUrl(), otherParams);
        } else {
            sendValidationEmail(request, user, registrationOrLoginBehavior.getRedirectUrl());
        }
    }

    /**
     * Method to send the validation email.
     *
     */

    protected void sendValidationEmail(HttpServletRequest request, User user, String redirectUrl, Map<String, String> otherParams) {
        sendValidationEmail(request, user, redirectUrl, otherParams, false);
    }


    /**
     * Method to send the validation email.
     *
     */

    protected void sendValidationEmail(HttpServletRequest request, User user, String redirectUrl) {
        sendValidationEmail(request, user, redirectUrl, null, false);
    }

    /**
     * Method to send the validation email.
     *
     */
    protected void sendValidationEmail(HttpServletRequest request, User user, String redirectUrl,
                                       Map<String, String> otherParams, boolean schoolReviewFlow) {
        if (user != null && StringUtils.isNotBlank(redirectUrl)) {
            try {
                if (schoolReviewFlow) {
                    _emailVerificationEmail.sendSchoolReviewVerificationEmail(request, user, redirectUrl);
                } else {
                    _emailVerificationEmail.sendVerificationEmail(request, user, redirectUrl, otherParams);
                }
            } catch (Exception e) {
                _log.error("Error sending email message: " + e, e);
            }
        }
    }

    /**
     * Method to check if a request is from a blocked IP.
     *
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

    public boolean convertToFacebookAccountIfNeeded(User user,
                                                    HttpServletRequest request,
                                                    UserRegistrationCommand userRegistrationCommand,
                                                    RegistrationOrLoginBehavior registrationOrLoginBehavior) throws Exception {
        boolean userModified = false;

        if (user.getFacebookId() == null) {
            FacebookSession facebookSession = FacebookHelper.getFacebookSession(request);
            if (facebookSession != null) {
                String userId = facebookSession.getUserId();
                if (userId != null) {
                    user.setFacebookId(userId);
                    userModified = true;
                }
            }
        }

        if (user.getPasswordMd5() == null) {
            String password = RandomStringUtils.randomAlphanumeric(14);
            setUsersPassword(user, password, registrationOrLoginBehavior.requireEmailVerification(), true);
        }

        if (user.getUserProfile() == null) {
            user.setUserProfile(createNewUserProfile(userRegistrationCommand, registrationOrLoginBehavior, user));
            user.getUserProfile().setUser(user);
        }

        if (user.isEmailProvisional()) {
            user.setEmailValidated();
            if (user.getWelcomeMessageStatus().equals(WelcomeMessageStatus.DO_NOT_SEND)) {
                user.setWelcomeMessageStatus(WelcomeMessageStatus.NEED_TO_SEND);
            }
            userModified = true;
        }

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            user.setEmailVerified(true);
            userModified = true;
        }

        if (userRegistrationCommand.getFirstName() != null) {
            user.setFirstName(userRegistrationCommand.getFirstName());
        }
        if (userRegistrationCommand.getLastName() != null) {
            user.setLastName(userRegistrationCommand.getLastName());
        }
        if (userRegistrationCommand.getGender() != null) {
            user.setFirstName(userRegistrationCommand.getGender());
        }


        user.setUpdated(new Date());

        return userModified;
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
    public static class Summary {
        private boolean wasUserLoggedIn = false;
        private boolean wasUserRegistered = false;
        private boolean wasUserInSession = false;
        private boolean wasVerificationEmailSent = false;
        private User user;

        public boolean wasUserLoggedIn() {
            return wasUserLoggedIn;
        }

        public void setWasUserLoggedIn(boolean wasUserLoggedIn) {
            this.wasUserLoggedIn = wasUserLoggedIn;
        }

        public boolean wasUserRegistered() {
            return wasUserRegistered;
        }

        public void setWasUserRegistered(boolean wasUserRegistered) {
            this.wasUserRegistered = wasUserRegistered;
        }

        public boolean wasUserInSession() {
            return wasUserInSession;
        }

        public void setWasUserInSession(boolean wasUserInSession) {
            this.wasUserInSession = wasUserInSession;
        }

        public boolean wasVerificationEmailSent() {
            return wasVerificationEmailSent;
        }

        public void setWasVerificationEmailSent(boolean wasVerificationEmailSent) {
            this.wasVerificationEmailSent = wasVerificationEmailSent;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }

}