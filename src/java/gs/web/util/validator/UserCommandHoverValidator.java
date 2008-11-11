package gs.web.util.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.web.community.registration.UserCommand;
import gs.web.community.registration.popup.LoginHoverController;
import gs.web.util.context.SessionContextUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class UserCommandHoverValidator implements IRequestAwareValidator {
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String BEAN_ID = "userValidator";
    private IUserDao _userDao;

    private static final int SCREEN_NAME_MINIMUM_LENGTH = 6;
    private static final int SCREEN_NAME_MAXIMUM_LENGTH = 14;
    private static final int EMAIL_MAXIMUM_LENGTH = 127;
    private static final int PASSWORD_MINIMUM_LENGTH = 6;
    private static final int PASSWORD_MAXIMUM_LENGTH = 14;
    private static final String ERROR_SCREEN_NAME_LENGTH =
            "Your username must be 6-14 characters long.";
    private static final String ERROR_SCREEN_NAME_BAD =
            "Your username may only contain letters and numbers.";
    private static final String ERROR_SCREEN_NAME_TAKEN =
            "We're sorry, that username is already taken. Please try another username.";
    private static final String ERROR_EMAIL_MISSING =
            "Please enter your email address.";
    public static final String ERROR_EMAIL_LENGTH = "Your email must be less than 128 characters long.";

    private static final String ERROR_PASSWORD_LENGTH =
            "Your password must be 6-14 characters long.";
    private static final String ERROR_PASSWORD_MISMATCH =
            "The two password fields don't match.";
    private static final String ERROR_STATE_MISSING =
            "Please select your state.";
    private static final String ERROR_CITY_MISSING =
            "Please select your city, or select \"My city is not listed.\"";
    private static final String ERROR_EMAIL_TAKEN = "The email address you entered has already been registered with GreatSchools.";
    private static final String ERROR_TERMS_MISSING =
            "Please read and accept our Terms of Use to join GreatSchools.";    

    public void validate(HttpServletRequest request, Object object, Errors errors) {
        UserCommand command = (UserCommand)object;

        String email = command.getEmail();

        User user = null;

        if (StringUtils.isEmpty(email)) {
            errors.rejectValue("email", null, ERROR_EMAIL_MISSING);
            _log.info("Registration error: " + ERROR_EMAIL_MISSING);
        } else if (email.length() > EMAIL_MAXIMUM_LENGTH) {
            errors.rejectValue("email", null, ERROR_EMAIL_LENGTH);
            _log.info("Registration error: " + ERROR_EMAIL_LENGTH);
        } else {
            user = _userDao.findUserFromEmailIfExists(email);

            if (user != null) {
                if (user.getUserProfile() != null && !user.getUserProfile().isActive()) {
                    String errmsg = "The account associated with that email address has been disabled. " +
                            "Please <a href=\"http://" +
                            SessionContextUtil.getSessionContext(request).getSessionContextUtil().getCommunityHost(request) +
                            "/report/email-moderator\">contact us</a> for more information.";
                    errors.rejectValue("email", null, errmsg);
                    _log.info("Registration error: " + errmsg);
                } else if (user.isEmailValidated()) {
                    String loginUrl = LoginHoverController.BEAN_ID + "?email=" + email;
                    String errmsg = ERROR_EMAIL_TAKEN + " <a href=\"" + loginUrl + "\">&nbsp;Sign in&nbsp;&gt;</a>";
                    errors.rejectValue("email", null, errmsg);
                    return; // other errors are irrelevant
                }
            }
        }

        // screen name must be 5-20 characters and alphanumeric only (no space)
        String sn = command.getScreenName();
        boolean snError = false;
        if (StringUtils.isEmpty(sn) ||
                sn.length() < SCREEN_NAME_MINIMUM_LENGTH ||
                sn.length() > SCREEN_NAME_MAXIMUM_LENGTH) {
            errors.rejectValue("screenName", null, ERROR_SCREEN_NAME_LENGTH);
            _log.info("Registration error: " + ERROR_SCREEN_NAME_LENGTH);
            snError = true;
        } else if (screenNameHasInvalidCharacters(sn)) {
            errors.rejectValue("screenName", null, ERROR_SCREEN_NAME_BAD);
            _log.info("Registration error: " + ERROR_SCREEN_NAME_BAD);
            snError = true;
        }
        // only bother checking the unique constraint if there is no other problem with the sn
        if (!snError && _userDao.findUserFromScreenNameIfExists(sn) != null) {
            if (user == null || user.getUserProfile() == null ||
                    !StringUtils.equals(user.getUserProfile().getScreenName(), sn)) {
                errors.rejectValue("screenName", null, ERROR_SCREEN_NAME_TAKEN);
                _log.info("Registration error: " + ERROR_SCREEN_NAME_TAKEN);
            }
        }

        if (!command.getTerms()) {
            errors.rejectValue("terms", null, ERROR_TERMS_MISSING);
            _log.info("Registration error: " + ERROR_TERMS_MISSING);
        }

        validatePassword(command, errors);

        UserProfile userProfile = command.getUserProfile();
        if (userProfile.getState() == null) {
            errors.rejectValue("state", null, ERROR_STATE_MISSING);
            _log.info("Registration error: " + ERROR_STATE_MISSING);
            return; // avoid NPEs
        }
        if (StringUtils.isEmpty(userProfile.getCity())) {
            errors.rejectValue("city", null, ERROR_CITY_MISSING);
            _log.info("Registration error: " + ERROR_CITY_MISSING);
        }
    }

    /**
     * Returns true if the screen name has no invalid characters.
     * @param sn screen name
     * @return true if screen name contains all valid characters or is null
     */
    private boolean screenNameHasInvalidCharacters(String sn) {
        // valid characters are all alphanumeric, hyphen, underscore
        return sn != null && !sn.matches("[0-9a-zA-Z\\-\\_]*");
    }

    /**
     * Checks the password field in the UserCommand object. The password must be between
     * 6 and 16 characters long, and must match the confirmPassword field.
     * @param command
     * @param errors
     */
    public void validatePassword(UserCommand command, Errors errors) {
        String password = command.getPassword();
        String confirmPassword = command.getConfirmPassword();

        validatePasswordFields(password, confirmPassword, "password", errors);
    }

    public void validatePasswordFields(String passwordValue, String passwordConfirmValue, String fieldName, Errors errors) {
        if (StringUtils.isEmpty(passwordValue) ||
                passwordValue.length() < PASSWORD_MINIMUM_LENGTH ||
                passwordValue.length() > PASSWORD_MAXIMUM_LENGTH) {
            errors.rejectValue(fieldName, null, ERROR_PASSWORD_LENGTH);
            _log.info("Registration error: " + ERROR_PASSWORD_LENGTH);
        } else if (StringUtils.isEmpty(passwordConfirmValue) || !passwordConfirmValue.equals(passwordValue)) {
            errors.rejectValue(fieldName, null, ERROR_PASSWORD_MISMATCH);
            _log.info("Registration error: " + ERROR_PASSWORD_MISMATCH);
        }
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao _userDao) {
        this._userDao = _userDao;
    }

}
