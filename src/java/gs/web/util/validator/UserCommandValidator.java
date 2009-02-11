package gs.web.util.validator;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.web.community.registration.UserCommand;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.Errors;

import javax.servlet.http.HttpServletRequest;

/**
 * Validates a UserCommand object, created for use in community registration
 * User: UrbanaSoft
 * Date: Jun 15, 2006
 * Time: 12:10:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserCommandValidator implements IRequestAwareValidator {

    protected final Log _log = LogFactory.getLog(getClass());
    public static final String BEAN_ID = "userValidator";
    private IUserDao _userDao;

    protected static final int SCREEN_NAME_MINIMUM_LENGTH = 6;
    protected static final int SCREEN_NAME_MAXIMUM_LENGTH = 14;
    protected static final int FIRST_NAME_MINIMUM_LENGTH = 2;
    protected static final int FIRST_NAME_MAXIMUM_LENGTH = 24;
    protected static final int EMAIL_MAXIMUM_LENGTH = 127;
    protected static final int PASSWORD_MINIMUM_LENGTH = 6;
    protected static final int PASSWORD_MAXIMUM_LENGTH = 14;
    protected static final String GENDER_MISSING =
            "Please select from one of the options.";
    protected static final String ERROR_FIRST_NAME_LENGTH =
            "Your first name must be 2-24 characters long.";
    protected static final String ERROR_SCREEN_NAME_LENGTH =
            "Your username must be 6-14 characters long.";
    protected static final String ERROR_SCREEN_NAME_BAD =
            "Your username may only contain letters and numbers.";
    protected static final String ERROR_SCREEN_NAME_TAKEN =
            "We're sorry, that username is already taken. Please try another username.";
    protected static final String ERROR_SCREEN_NAME_TAKEN_SHORT =
            "That username is taken. Please try another username.";
    protected static final String ERROR_EMAIL_MISSING =
            "Please enter your email address.";
    public static final String ERROR_EMAIL_LENGTH = "Your email must be less than 128 characters long.";

    protected static final String ERROR_PASSWORD_LENGTH =
            "Your password must be 6-14 characters long.";
    protected static final String ERROR_PASSWORD_MISMATCH =
            "The two password fields don't match.";
    protected static final String ERROR_STATE_MISSING =
            "Please select your state.";
    protected static final String ERROR_CITY_MISSING =
            "Please select your city, or select \"My city is not listed.\"";
    protected static final String ERROR_SCHOOL_CHOICE_STATE_MISSING =
            "Please select your state.";
    protected static final String ERROR_SCHOOL_CHOICE_CITY_MISSING =
            "Please select your city, or select \"My city is not listed.\"";
    protected static final String ERROR_NUM_CHILDREN_MISSING =
            "Please tell us the number of children you have in K-12 schools.";
    protected static final String ERROR_TERMS_MISSING =
            "Please read and accept our Terms of Use to join GreatSchools.";
    protected static final char[] FIRST_NAME_DISALLOWED_CHARACTERS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '<', '>', '&', '\\'
    };
    protected static final String ERROR_FIRST_NAME_BAD = "Please enter your name without numbers or symbols.";
    protected static final String ERROR_EMAIL_TAKEN = "The email address you entered has already been registered with GreatSchools.";
    protected static final String ERROR_EMAIL_TAKEN_SHORT = "That email address has already been registered.";

    public void validate(HttpServletRequest request, Object object, Errors errors) {
        UserCommand command = (UserCommand)object;

        User user = validateEmail(command, request, errors);
        if (user != null && errors.hasFieldErrors("email")) {
            // User exists but email failed validation?
            // This can only happen if they are already registered or are disabled
            return; // other errors are irrelevant
        }

        validateFirstName(command, errors);
        validateUsername(command, user, errors);
        validateGender(command, errors);
        validateNumSchoolChildren(command, errors);
        if ("u".equals(command.getGender()) || (command.getNumSchoolChildren() != null &&
                command.getNumSchoolChildren() == 0)) {
            // only validate terms of use if this is final page of registration
            // which happens if they don't list children
            if (!command.isChooserRegistration()) {
                validateTerms(command, errors);
            }
        }
        validatePassword(command, errors);
        if (command.isChooserRegistration()) {
            validateSchoolChoiceStateCity(command, errors);
        } else {
            validateStateCity(command, errors);
        }
    }

    protected User validateEmail(UserCommand command, HttpServletRequest request, Errors errors) {
        User user = null;
        String email = command.getEmail();
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
                    UrlBuilder builder = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, null);
                    builder.addParameter("email",email);
                    String loginUrl = builder.asFullUrl(request);
                    String errmsg;
                    if (command.isChooserRegistration()) {
                        errmsg = ERROR_EMAIL_TAKEN_SHORT + " <a href=\"" + loginUrl + "\">&nbsp;Log in&nbsp;&gt;</a>";
                    } else {
                        errmsg = ERROR_EMAIL_TAKEN + " <a href=\"" + loginUrl + "\">&nbsp;Log in&nbsp;&gt;</a>";
                    }
                    errors.rejectValue("email", null, errmsg);
                } else if (user.isEmailProvisional()) {
                    // let them register, just overwrite previous values
                }
            }
        }
        return user;
    }

    protected void validateSchoolChoiceStateCity(UserCommand command, Errors errors) {
        UserProfile userProfile = command.getUserProfile();
        if (userProfile.getSchoolChoiceState() == null) {
            errors.rejectValue("schoolChoiceState", null, ERROR_SCHOOL_CHOICE_STATE_MISSING);
            _log.info("Registration error: " + ERROR_SCHOOL_CHOICE_STATE_MISSING);
            return; // avoid NPEs
        }
        if (StringUtils.isEmpty(userProfile.getSchoolChoiceCity())) {
            errors.rejectValue("schoolChoiceCity", null, ERROR_SCHOOL_CHOICE_CITY_MISSING);
            _log.info("Registration error: " + ERROR_SCHOOL_CHOICE_CITY_MISSING);
        }
    }

    protected void validateStateCity(UserCommand command, Errors errors) {
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

    protected void validateTerms(UserCommand command, Errors errors) {
        if (!command.getTerms()) {
            errors.rejectValue("terms", null, ERROR_TERMS_MISSING);
            _log.info("Registration error: " + ERROR_TERMS_MISSING);
        }
    }

    protected void validateNumSchoolChildren(UserCommand command, Errors errors) {
        if (command.getNumSchoolChildren() == null || command.getNumSchoolChildren() == -1) {
            if (!"u".equals(command.getGender())) {
                errors.rejectValue("numSchoolChildren", null, ERROR_NUM_CHILDREN_MISSING);
                _log.info("Registration error: " + ERROR_NUM_CHILDREN_MISSING);
            }
        }
    }

    protected void validateGender(UserCommand command, Errors errors) {
        String gender = command.getGender();
        if (StringUtils.isEmpty(gender)) {
            errors.rejectValue("gender", null, GENDER_MISSING);
            _log.info("Registration error: " + GENDER_MISSING);
        } else if  (gender.length() > 1) {
            errors.rejectValue("gender", null, GENDER_MISSING);
            _log.info("Registration error: " + GENDER_MISSING);
        } else if (!("m".equals(gender) || "f".equals(gender) || "u".equals(gender))) {
            errors.rejectValue("gender", null, GENDER_MISSING);
            _log.info("Registration error: " + GENDER_MISSING);
        }
    }

    protected void validateUsername(UserCommand command, User user, Errors errors) {
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
                if (command.isChooserRegistration()) {
                    errors.rejectValue("screenName", null, ERROR_SCREEN_NAME_TAKEN_SHORT);
                    _log.info("Registration error: " + ERROR_SCREEN_NAME_TAKEN_SHORT);                    
                } else {
                    errors.rejectValue("screenName", null, ERROR_SCREEN_NAME_TAKEN);
                    _log.info("Registration error: " + ERROR_SCREEN_NAME_TAKEN);
                }
            }
        }
    }

    protected void validateFirstName(UserCommand command, Errors errors) {
        if (StringUtils.isEmpty(command.getFirstName()) ||
                command.getFirstName().length() > FIRST_NAME_MAXIMUM_LENGTH ||
                command.getFirstName().length() < FIRST_NAME_MINIMUM_LENGTH) {
            errors.rejectValue("firstName", null, ERROR_FIRST_NAME_LENGTH);
            _log.info("Registration error: " + ERROR_FIRST_NAME_LENGTH);
        } else if (!StringUtils.containsNone(command.getFirstName(), FIRST_NAME_DISALLOWED_CHARACTERS)) {
            errors.rejectValue("firstName", null, ERROR_FIRST_NAME_BAD);
            _log.info("Registration error: " + ERROR_FIRST_NAME_BAD);
        }
    }

    /**
     * Returns true if the screen name has no invalid characters.
     * @param sn screen name
     * @return true if screen name contains all valid characters or is null
     */
    protected boolean screenNameHasInvalidCharacters(String sn) {
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
