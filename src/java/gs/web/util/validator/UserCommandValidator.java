package gs.web.util.validator;

import gs.data.community.IUserDao;
import gs.data.community.UserProfile;
import gs.data.community.User;
import gs.web.community.registration.UserCommand;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validates a UserCommand object, created for use in community registration
 * User: UrbanaSoft
 * Date: Jun 15, 2006
 * Time: 12:10:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserCommandValidator implements Validator {

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
    private static final String ERROR_FIRST_NAME_LENGTH =
            "We're sorry, your first name must be 2-24 characters long";
    private static final String ERROR_SCREEN_NAME_LENGTH =
            "We're sorry, your screen name must be 6-14 characters long";
    private static final String ERROR_EMAIL_MISSING =
            "We're sorry, you must enter your email address";
    private static final String ERROR_PASSWORD_LENGTH =
            "We're sorry, your password must be 6-14 characters long";
    private static final String ERROR_STATE_MISSING =
            "We're sorry, you must select your state";
    private static final String ERROR_CITY_MISSING =
            "We're sorry, you must select your city";
    private static final String ERROR_NUM_CHILDREN_MISSING =
            "We're sorry, you must select a number";
    private static final String ERROR_TERMS_MISSING =
            "We're sorry, you must accept the terms of service";

    public boolean supports(Class aClass) {
        return aClass == UserCommand.class;
    }

    public void validate(Object object, Errors errors) {
        UserCommand command = (UserCommand)object;

        String email = command.getEmail();
        // String confirmEmail = command.getConfirmEmail();

        if (email.length() > EMAIL_MAXIMUM_LENGTH) {
            errors.rejectValue("email", null, ERROR_EMAIL_MISSING);
        } else {
            User user = _userDao.findUserFromEmailIfExists(email);

            // verify confirm email matches email
//        if (confirmEmail != null && !confirmEmail.equals(email)) {
//            errors.rejectValue("email", null, "Please enter the same email into both fields.");
//        }

            if (user != null) {
                if (user.isEmailValidated()) {
                    errors.rejectValue("email", null,
                            "You have already registered with GreatSchools!");
                    return; // other errors are irrelevant
                } else if (user.isEmailProvisional()) {
                    errors.rejectValue("email", null,
                            "You have already registered with GreatSchools! Please check your " +
                                    "email and follow the instructions to validate your account.");
                    return; // other errors are irrelevant
                }
            }
        }

        if (StringUtils.isEmpty(command.getFirstName()) ||
                command.getFirstName().length() > FIRST_NAME_MAXIMUM_LENGTH ||
                command.getFirstName().length() < FIRST_NAME_MINIMUM_LENGTH) {
            errors.rejectValue("firstName", null, ERROR_FIRST_NAME_LENGTH);
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
        }
        if (!StringUtils.isAlphanumeric(sn)) { // this method is null-safe
            errors.rejectValue("screenName", null,
                    "Your screen name must consist of only letters and numbers.");
            snError = true;
        }
        // only bother checking the unique constraint if there is no other problem with the sn
        if (!snError && _userDao.findUserFromScreenNameIfExists(sn) != null) {
            errors.rejectValue("screenName", null,
                    "This screen name is already taken. Please try another.");
        }

        String gender = command.getGender();
        if (StringUtils.isEmpty(gender) ||
                (gender.length() > 1 || (!"m".equals(gender) &&
                        !"f".equals(gender) && !"u".equals(gender)))) {
            errors.rejectValue("gender", null,
                    "Please choose a value.");
        }

        if (command.getNumSchoolChildren() == null || command.getNumSchoolChildren().intValue() == -1) {
            if (!"u".equals(gender)) {
                errors.rejectValue("numSchoolChildren", null, ERROR_NUM_CHILDREN_MISSING);
            }
        }

        if ("u".equals(gender)) {
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
     * Checks the password field in the UserCommand object. The password must be between
     * 6 and 16 characters long, and must match the confirmPassword field.
     * @param command
     * @param errors
     */
    public void validatePassword(UserCommand command, Errors errors) {
        String password = command.getPassword();
        String confirmPassword = command.getConfirmPassword();

        if (StringUtils.isEmpty(password) ||
                password.length() < PASSWORD_MINIMUM_LENGTH ||
                password.length() > PASSWORD_MAXIMUM_LENGTH) {
            errors.rejectValue("password", null, ERROR_PASSWORD_LENGTH);
        } else if (StringUtils.isEmpty(confirmPassword) || !confirmPassword.equals(password)) {
            errors.rejectValue("password", null, "Please enter the same password into both fields.");
        }
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao _userDao) {
        this._userDao = _userDao;
    }
}
