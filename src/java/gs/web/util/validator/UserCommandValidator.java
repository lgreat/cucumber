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

    public static final int SCREEN_NAME_MINIMUM_LENGTH = 6;
    public static final int SCREEN_NAME_MAXIMUM_LENGTH = 14;
    public static final int FIRST_NAME_MINIMUM_LENGTH = 2;
    public static final int FIRST_NAME_MAXIMUM_LENGTH = 24;
    protected static final int EMAIL_MAXIMUM_LENGTH = 127;
    protected static final int PASSWORD_MINIMUM_LENGTH = 6;
    protected static final int PASSWORD_MAXIMUM_LENGTH = 14;

    protected static final String GENDER_MISSING =
            "Please select from one of the options.";

    protected static final String ERROR_FIRST_NAME_LENGTH =
            "First name must be 2-24 characters long.";
    public static final char[] FIRST_NAME_DISALLOWED_CHARACTERS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '<', '>', '&', '\\'
    };
    protected static final String ERROR_FIRST_NAME_BAD =
            "Please remove the numbers or symbols.";

    protected static final String ERROR_LAST_NAME_LENGTH =
            "Last name must be 1-24 characters long.";
    protected static final String ERROR_LAST_NAME_INVALID_CHARACTERS =
            "Last name may contain only letters, numbers, spaces, and the following punctuation:, . - _ &";

    protected static final String ERROR_SCREEN_NAME_LENGTH =
            "Username must be 6-14 characters.";
    protected static final String ERROR_SCREEN_NAME_BAD =
            "Username may only contain letters and numbers.";
    protected static final String ERROR_SCREEN_NAME_TAKEN =
            "This username is already in use.";
    protected static final String ERROR_SCREEN_NAME_TAKEN_SHORT =
            "That username is taken. Please try another username.";
    protected static final String ERROR_EMAIL_MISSING =
            "Please enter your email address.";
    public static final String ERROR_EMAIL_LENGTH =
            "Your email must be less than 128 characters long.";
    protected static final String ERROR_EMAIL_TAKEN =
            "This email address is already registered.";
    protected static final String ERROR_EMAIL_TAKEN_SHORT =
            "This email address is already registered.";
    public static final String ERROR_EMAIL_MISMATCH =
            "Emails do not match.";

    protected static final String ERROR_PASSWORD_LENGTH =
            "Password should be 6-14 characters.";
    public static final String ERROR_PASSWORD_MISMATCH =
            "Passwords do not match.";
    protected static final String ERROR_STATE_MISSING =
            "Please select your state.";
    protected static final String ERROR_CITY_MISSING =
            "Please select your city, or select \"My city is not listed.\"";
    protected static final String ERROR_SCHOOL_CHOICE_STATE_MISSING =
            "Please select your state.";
    protected static final String ERROR_SCHOOL_CHOICE_CITY_MISSING =
            "Please select your city, or select \"My city is not listed.\"";
    protected static final String ERROR_SCHOOL_CHOICE_CITY_MISSING_SHORT =
            "Please select your city.";
    protected static final String ERROR_NUM_CHILDREN_MISSING =
            "Please tell us the number of children you have in K-12 schools.";
    protected static final String ERROR_TERMS_MISSING =
            "Please read and accept our Terms of Use to join GreatSchools.";
    protected static final String ERROR_FACEBOOK_USER =
            "This account is linked to a Facebook account. Please sign in using Facebook.";

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
        //validateNumSchoolChildren(command, errors);
        validateTerms(command, errors);
        validatePassword(command, errors);
        validateStateCity(command, errors);
    }

    public void validateEmailBasic(UserCommand command, Errors errors) {
        String email = command.getEmail();
        if (StringUtils.isEmpty(email)) {
            errors.rejectValue("email", null, ERROR_EMAIL_MISSING);
            _log.info("Registration error: " + ERROR_EMAIL_MISSING);
        } else if (email.length() > EMAIL_MAXIMUM_LENGTH) {
            errors.rejectValue("email", null, ERROR_EMAIL_LENGTH);
            _log.info("Registration error: " + ERROR_EMAIL_LENGTH);
        }
    }

    public User validateEmail(UserCommand command, HttpServletRequest request, Errors errors) {
        User user = null;
        String email = command.getEmail();

        validateEmailBasic(command, errors);

        if (!errors.hasFieldErrors("email")) {
            user = _userDao.findUserFromEmailIfExists(email);
            if (user != null) {
                if (user.getUserProfile() != null && !user.getUserProfile().isActive()) {
                    String errmsg = "The account associated with that email address has been disabled. " +
                            "Please <a href=\"http://" +
                            SessionContextUtil.getSessionContext(request).getSessionContextUtil().getCommunityHost(request) +
                            "/report/email-moderator\">contact us</a> for more information.";
                    errors.rejectValue("email", null, errmsg);
                    _log.info("Registration error: " + errmsg);
                } else if (user.isFacebookUser()) {
                    String errmsg = ERROR_FACEBOOK_USER;
                    errors.rejectValue("email", null, errmsg);
                } else if (user.isEmailValidated()) {
                    UrlBuilder builder = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, null);
                    builder.addParameter("email",email);
                    String loginUrl = builder.asFullUrl(request);
                    String errmsg;
                    if (command.isChooserRegistration()) {
                        errmsg = ERROR_EMAIL_TAKEN_SHORT + " <a class=\"launchSignInHover\" href=\"" + loginUrl + "\">&nbsp;Log in&nbsp;&gt;</a>";
                    } else {
                        errmsg = ERROR_EMAIL_TAKEN + " <a class=\"launchSignInHover\" href=\"" + loginUrl + "\">&nbsp;Log in&nbsp;&gt;</a>";
                    }
                    errors.rejectValue("email", null, errmsg);
                } else if (user.isEmailProvisional()) {
                    // let them register, just overwrite previous values
                }
            }
        }
        return user;
    }

    public void validateStateCity(UserCommand command, Errors errors) {
        UserProfile userProfile = command.getUserProfile();
        if (command.isChooserRegistration()) {
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
    }

    public void validateTerms(UserCommand command, Errors errors) {
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
        if (StringUtils.isBlank(gender)) {
            // gender not required
            return;
        } else if  (gender.length() > 1) {
            errors.rejectValue("gender", null, GENDER_MISSING);
            _log.info("Registration error: " + GENDER_MISSING);
        } else if (!("m".equals(gender) || "f".equals(gender) || "u".equals(gender))) {
            errors.rejectValue("gender", null, GENDER_MISSING);
            _log.info("Registration error: " + GENDER_MISSING);
        }
    }

    public void validateUsername(UserCommand command, User user, Errors errors) {
        // screen name must be 5-20 characters and alphanumeric only (no space)
        String sn = command.getScreenName();
        boolean snError = false;
        if (StringUtils.isEmpty(sn) ||
                sn.length() < SCREEN_NAME_MINIMUM_LENGTH ||
                sn.length() > SCREEN_NAME_MAXIMUM_LENGTH) {
            errors.rejectValue("screenName", null, ERROR_SCREEN_NAME_LENGTH);
            _log.info("Registration error: " + ERROR_SCREEN_NAME_LENGTH);
            snError = true;
        } else if (screenNameHasInvalidCharacters(sn,false)) {
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

    public void validateFirstName(UserCommand command, Errors errors) {
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

    public void validateLastName(UserCommand command, Errors errors) {
        if (StringUtils.isEmpty(command.getLastName()) ||
                command.getLastName().length() > 24 ||
                command.getLastName().length() < 0) {
            errors.rejectValue("lastName", null, ERROR_LAST_NAME_LENGTH);
        } else if (!command.getLastName().matches("[0-9a-zA-Z\\-\\_\\.\\,\\&\\s]*")) {
            errors.rejectValue("lastName", null, ERROR_LAST_NAME_INVALID_CHARACTERS);
        }
    }

    /**
     * Returns true if the screen name has no invalid characters.
     * @param sn screen name
     * @param cbicall since the username in Collegeboud shuold allow period
     * cbicall is used to verify if the method is being called from CB.
     * @return true if screen name contains all valid characters or is null
     */
    public boolean screenNameHasInvalidCharacters(String sn,boolean cbicall) {
        // valid characters are all alphanumeric, hyphen, underscore
        if(cbicall){
            return sn != null && !sn.matches("[0-9a-zA-Z\\-\\_\\.]*");
        }else{
            return sn != null && !sn.matches("[0-9a-zA-Z\\-\\_]*");
        }

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
        if (validatePasswordFormat(passwordValue, fieldName, errors)) {
            validatePasswordEquivalence(passwordValue, passwordConfirmValue, fieldName, errors);
        }
    }

    public boolean validatePasswordFormat(String passwordValue, String fieldName, Errors errors) {
        if (StringUtils.isEmpty(passwordValue) ||
                passwordValue.length() < PASSWORD_MINIMUM_LENGTH ||
                passwordValue.length() > PASSWORD_MAXIMUM_LENGTH) {
            errors.rejectValue(fieldName, null, ERROR_PASSWORD_LENGTH);
            _log.info("Registration error: " + ERROR_PASSWORD_LENGTH);
            return false;
        }
        return true;
    }

    public boolean validatePasswordEquivalence(String passwordValue, String passwordConfirmValue, String fieldName, Errors errors) {
        if (!StringUtils.equals(passwordValue, passwordConfirmValue)) {
            errors.rejectValue(fieldName, null, ERROR_PASSWORD_MISMATCH);
            _log.info("Registration error: " + ERROR_PASSWORD_MISMATCH);
            return false;
        }
        return true;
    }

    public boolean validateEmailEquivalence(String emailValue, String emailConfirmValue, String fieldName, Errors errors) {
        if (!StringUtils.equals(emailValue, emailConfirmValue)) {
            errors.rejectValue(fieldName, null, ERROR_EMAIL_MISMATCH);
            _log.info("Registration error: " + ERROR_EMAIL_MISMATCH);
            return false;
        }
        return true;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao _userDao) {
        this._userDao = _userDao;
    }
}
