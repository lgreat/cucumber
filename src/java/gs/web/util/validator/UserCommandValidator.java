package gs.web.util.validator;

import gs.data.community.IUserDao;
import gs.data.community.UserProfile;
import gs.data.community.User;
import gs.web.community.registration.UserCommand;
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

    private static final int SCREEN_NAME_MINIMUM_LENGTH = 6;
    private static final int SCREEN_NAME_MAXIMUM_LENGTH = 14;
    private static final int FIRST_NAME_MINIMUM_LENGTH = 2;
    private static final int FIRST_NAME_MAXIMUM_LENGTH = 24;
    //private static final int LAST_NAME_MAXIMUM_LENGTH = 64;
    private static final int EMAIL_MAXIMUM_LENGTH = 127;
    private static final int PASSWORD_MINIMUM_LENGTH = 6;
    private static final int PASSWORD_MAXIMUM_LENGTH = 14;
    private static final String GENDER_MISSING =
            "Please select from one of the options.";
    private static final String ERROR_FIRST_NAME_LENGTH =
            "Your first name must be 2-24 characters long.";
    private static final String ERROR_SCREEN_NAME_LENGTH =
            "Your username must be 6-14 characters long.";
    private static final String ERROR_SCREEN_NAME_BAD =
            "Your username may only contain letters and numbers.";
    private static final String ERROR_SCREEN_NAME_TAKEN =
            "We're sorry, that username is already taken. Please try another username.";
    private static final String ERROR_EMAIL_MISSING =
            "Please enter your email address.";
    public static final String ERROR_EMAIL_LENGTH = "Your email must be less than 128 characters long.";

//    private static final String ERROR_EMAIL_PROVISIONAL =
//            "You have already registered with GreatSchools! Please check your email and follow " +
//                    "the instructions to validate your account.";
    private static final String ERROR_PASSWORD_LENGTH =
            "Your password must be 6-14 characters long.";
    private static final String ERROR_PASSWORD_MISMATCH =
            "The two password fields don't match.";
    private static final String ERROR_STATE_MISSING =
            "Please select your state.";
    private static final String ERROR_CITY_MISSING =
            "Please select your city, or select \"My city is not listed.\"";
    private static final String ERROR_NUM_CHILDREN_MISSING =
            "Please tell us the number of children you have in K-12 schools.";
    private static final String ERROR_TERMS_MISSING =
            "Please read and accept our Terms of Use to join the community.";
    private static final char[] FIRST_NAME_DISALLOWED_CHARACTERS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '<', '>', '&', '\\'
    };
    private static final String ERROR_FIRST_NAME_BAD = "Please enter your name without numbers or symbols.";

    public void validate(HttpServletRequest request, Object object, Errors errors) {
        UserCommand command = (UserCommand)object;

        String email = command.getEmail();
        // String confirmEmail = command.getConfirmEmail();

        User user = null;

        if (StringUtils.isEmpty(email)) {
            errors.rejectValue("email", null, ERROR_EMAIL_MISSING);
        } else if (email.length() > EMAIL_MAXIMUM_LENGTH) {
            errors.rejectValue("email", null, ERROR_EMAIL_LENGTH);
        } else {
            user = _userDao.findUserFromEmailIfExists(email);

            // verify confirm email matches email
//        if (confirmEmail != null && !confirmEmail.equals(email)) {
//            errors.rejectValue("email", null, "Please enter the same email into both fields.");
//        }

            if (user != null) {
                if (user.isEmailValidated()) {
                    UrlBuilder builder = new UrlBuilder(UrlBuilder.FORGOT_PASSWORD, null,
                            user.getEmail());
                    String href = builder.asAnchor(request, "forget your password").asATag();
                    errors.rejectValue("email", null,
                            "The email address you entered has already been registered " +
                                    "with GreatSchools. Did you " + href + "?");
                    return; // other errors are irrelevant
                } else if (user.isEmailProvisional()) {
                    // let them register, just overwrite previous values
                    //errors.rejectValue("email", null, ERROR_EMAIL_PROVISIONAL);
                    //return; // other errors are irrelevant
                }
            }
        }

        if (StringUtils.isEmpty(command.getFirstName()) ||
                command.getFirstName().length() > FIRST_NAME_MAXIMUM_LENGTH ||
                command.getFirstName().length() < FIRST_NAME_MINIMUM_LENGTH) {
            errors.rejectValue("firstName", null, ERROR_FIRST_NAME_LENGTH);
        } else if (!StringUtils.containsNone(command.getFirstName(), FIRST_NAME_DISALLOWED_CHARACTERS)) {
            errors.rejectValue("firstName", null, ERROR_FIRST_NAME_BAD);
        }

//        if (StringUtils.isEmpty(command.getLastName())) {
//            errors.rejectValue("lastName", "missing_last_name", "Please enter your last name");
//        } else if (command.getLastName().length() > LAST_NAME_MAXIMUM_LENGTH) {
//            errors.rejectValue("lastName", "long_last_name",
//                    "Your last name can be no more than " + LAST_NAME_MAXIMUM_LENGTH +
//                            " characters long.");
//        }

        // screen name must be 5-20 characters and alphanumeric only (no space)
        String sn = command.getScreenName();
        boolean snError = false;
        if (StringUtils.isEmpty(sn) ||
                sn.length() < SCREEN_NAME_MINIMUM_LENGTH ||
                sn.length() > SCREEN_NAME_MAXIMUM_LENGTH) {
            errors.rejectValue("screenName", null, ERROR_SCREEN_NAME_LENGTH);
            snError = true;
        } else if (screenNameHasInvalidCharacters(sn)) {
            errors.rejectValue("screenName", null, ERROR_SCREEN_NAME_BAD);
            snError = true;
        }
        // only bother checking the unique constraint if there is no other problem with the sn
        if (!snError && _userDao.findUserFromScreenNameIfExists(sn) != null) {
            if (user == null || user.getUserProfile() == null ||
                    !StringUtils.equals(user.getUserProfile().getScreenName(), sn)) {
                errors.rejectValue("screenName", null, ERROR_SCREEN_NAME_TAKEN);
            }
        }

        String gender = command.getGender();
        if (StringUtils.isEmpty(gender) ||
                (gender.length() > 1 || (!"m".equals(gender) &&
                        !"f".equals(gender) && !"u".equals(gender)))) {
            errors.rejectValue("gender", null, GENDER_MISSING);
        }

        if (command.getNumSchoolChildren() == null || command.getNumSchoolChildren().intValue() == -1) {
            if (!"u".equals(gender)) {
                errors.rejectValue("numSchoolChildren", null, ERROR_NUM_CHILDREN_MISSING);
            }
        }

        if ("u".equals(gender) || (command.getNumSchoolChildren() != null &&
                command.getNumSchoolChildren().intValue() == 0)) {
            if (!command.getTerms()) {
                errors.rejectValue("terms", null, ERROR_TERMS_MISSING);
            }
        }

        validatePassword(command, errors);

        UserProfile userProfile = command.getUserProfile();
        if (userProfile.getState() == null) {
            errors.rejectValue("state", null, ERROR_STATE_MISSING);
            return; // avoid NPEs
        }
        if (StringUtils.isEmpty(userProfile.getCity())) {
            errors.rejectValue("city", null, ERROR_CITY_MISSING);
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
        } else if (StringUtils.isEmpty(passwordConfirmValue) || !passwordConfirmValue.equals(passwordValue)) {
            errors.rejectValue(fieldName, null, ERROR_PASSWORD_MISMATCH);
        }
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao _userDao) {
        this._userDao = _userDao;
    }
}
