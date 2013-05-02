package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.community.WelcomeMessageStatus;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableDaoFactory;
import gs.web.util.PageHelper;
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
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Service
public class UserRegistrationOrLoginService {

    protected final Log _log = LogFactory.getLog(getClass());

    public static final String SPREADSHEET_ID_FIELD = "ip";

    private ITableDaoFactory _tableDaoFactory;

    private ITableDao _tableDao;

    @Autowired
    private LocalValidatorFactoryBean _validatorFactory;

    @Autowired
    private IUserDao _userDao;

    @Autowired
    @Qualifier("emailVerificationEmail")
    private EmailVerificationEmail _emailVerificationEmail;

    public User registerOrLoginUser(UserRegistrationCommand userRegistrationCommand, RegistrationBehavior registrationBehavior,
                                    BindingResult bindingResult,
                                    HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        if (isIPBlocked(request)) {
            _log.error("Ip is blocked while registering or logging in the user.");
            return null;
        }

        _validatorFactory.validate(userRegistrationCommand, bindingResult);

        if (bindingResult.hasErrors()) {
            _log.error("Validation Errors while registering or logging in user.");
        } else {
            User user = getUserDao().findUserFromEmailIfExists(userRegistrationCommand.getEmail());
            boolean userExists = (user != null);

            if (userExists) {
                if (user.isEmailValidated()) {
                    PageHelper.setMemberAuthorized(request, response, user, true);
                } else {
                    if (registrationBehavior.sendVerificationEmail()) {
                        sendValidationEmail(request, user, registrationBehavior.getRedirectUrl());
                    }
                }
            } else {
                try {
                    user = createNewUser(userRegistrationCommand, registrationBehavior);

                    getUserDao().saveUser(user);

                    setUsersPassword(user, userRegistrationCommand, registrationBehavior, userExists);

                    user.setUserProfile(createNewUserProfile(userRegistrationCommand, registrationBehavior, user));
                    user.getUserProfile().setUser(user);

                    getUserDao().updateUser(user);

                } catch (NoSuchAlgorithmException e) {
                    getUserDao().removeUser(user.getId());
                } catch (IllegalStateException e) {
                    getUserDao().removeUser(user.getId());
                }

                ThreadLocalTransactionManager.commitOrRollback();
                PageHelper.setMemberAuthorized(request, response, user);
            }

            return user;
        }
        return null;
    }

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

    public UserProfile createNewUserProfile(UserRegistrationCommand userRegistrationCommand, RegistrationBehavior registrationBehavior, User user) {
        UserProfile profile = new UserProfile();

        profile.setHow(userRegistrationCommand.getHow());

        if (userRegistrationCommand.getState() != null) {
            profile.setState(userRegistrationCommand.getState());
        }

        if (userRegistrationCommand.getCity() != null) {
            profile.setCity(userRegistrationCommand.getCity());
        }

        //TODO is this the only check required?
        if (StringUtils.isEmpty(userRegistrationCommand.getScreenName())) {
            profile.setScreenName("user" + user.getId());
        }else{
            profile.setScreenName(userRegistrationCommand.getScreenName());
        }

        Date now = new Date();
        profile.setCreated(now);

        return profile;
    }

    public void setUsersPassword(User user, UserRegistrationCommand userRegistrationCommand, RegistrationBehavior registrationBehavior,
                                 boolean userExists) throws Exception {
        setUsersPassword(user, userRegistrationCommand.getPassword(), registrationBehavior.requireEmailVerification(), userExists);
    }

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

    protected void sendValidationEmail(HttpServletRequest request, User user, String redirectUrl) {
        sendValidationEmail(request, user, redirectUrl, false);
    }

    protected void sendValidationEmail(HttpServletRequest request, User user, String redirectUrl,
                                       boolean schoolReviewFlow) {
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

    public void setPollFactory(ITableDaoFactory _tableDaoFactory) {
        _tableDao = _tableDaoFactory.getTableDao();
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }
}