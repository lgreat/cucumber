package gs.web.util.validator;

import gs.data.community.IUserDao;
import gs.data.community.UserProfile;
import gs.data.community.User;
import gs.data.geo.IGeoDao;
import gs.data.geo.ICity;
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
    private IGeoDao _geoDao;
    private static final int SCREEN_NAME_MINIMUM_LENGTH = 5;
    private static final int SCREEN_NAME_MAXIMUM_LENGTH = 20;
    private static final int FIRST_NAME_MAXIMUM_LENGTH = 64;
    private static final int LAST_NAME_MAXIMUM_LENGTH = 64;

    public boolean supports(Class aClass) {
        return aClass == UserCommand.class;
    }

    public void validate(Object object, Errors errors) {
        UserCommand command = (UserCommand)object;

        String email = command.getEmail();
        String confirmEmail = command.getConfirmEmail();

        User user = _userDao.findUserFromEmailIfExists(email);

        // Don't allow people to pick existing emails unless they've been redirected here
        // (redirection is detected by checking id)
        // per request, this rule is no longer enforced
        if (command.getUser().getId() == null && user != null) {
            //errors.rejectValue("email", "existing_user", "This email already exists, please choose a different one.");
        }

        if (command.getUser().getId() == null) {
            // verify confirm email matches email
            if (!confirmEmail.equals(email)) {
                errors.rejectValue("email", "mismatched_email", "Please enter the same email into both fields.");
            }
        }

        if (user != null) {
            if (user.isEmailValidated()) {
                errors.rejectValue("email", "user_already_registered",
                        "You have already registered with GreatSchools!");
                return; // other errors are irrelevant
            } else if (user.isEmailProvisional()) {
                errors.rejectValue("email", "user_already_registered",
                        "You have already registered with GreatSchools! Please check your " +
                                "email and follow the instructions to validate your account.");
                return; // other errors are irrelevant
            }
        }

        if (StringUtils.isEmpty(command.getFirstName())) {
            errors.rejectValue("firstName", "missing_first_name", "Please enter your first name");
        } else if (command.getFirstName().length() > FIRST_NAME_MAXIMUM_LENGTH) {
            errors.rejectValue("firstName", "long_first_name",
                    "Your first name can be no more than " + FIRST_NAME_MAXIMUM_LENGTH +
                            " characters long.");
        }

        if (StringUtils.isEmpty(command.getLastName())) {
            errors.rejectValue("lastName", "missing_last_name", "Please enter your last name");
        } else if (command.getLastName().length() > LAST_NAME_MAXIMUM_LENGTH) {
            errors.rejectValue("lastName", "long_last_name",
                    "Your last name can be no more than " + LAST_NAME_MAXIMUM_LENGTH +
                            " characters long.");
        }

        // screen name must be 5-20 characters and alphanumeric only (no space)
        String sn = command.getScreenName();
        boolean snError = false;
        if (StringUtils.isEmpty(sn)) {
            errors.rejectValue("screenName", "missing_screen_name", "Please enter your screen name");
            snError = true;
        } else if (sn.length() < SCREEN_NAME_MINIMUM_LENGTH) {
            errors.rejectValue("screenName", "short_screen_name",
                    "Your screen name must be at least " + SCREEN_NAME_MINIMUM_LENGTH +
                            " characters long.");
            snError = true;
        } else if (sn.length() > SCREEN_NAME_MAXIMUM_LENGTH) {
            errors.rejectValue("screenName", "long_screen_name",
                    "Your screen name can be no more than " + SCREEN_NAME_MAXIMUM_LENGTH +
                            " characters long.");
            snError = true;
        }
        if (!StringUtils.isAlphanumeric(sn)) { // this method is null-safe
            errors.rejectValue("screenName", "bad_screen_name",
                    "Your screen name must consist of only letters and numbers.");
            snError = true;
        }
        // only bother checking the unique constraint if there is no other problem with the sn
        if (!snError && _userDao.findUserFromScreenNameIfExists(sn) != null) {
            errors.rejectValue("screenName", "screen_name_exists",
                    "This screen name is already taken. Please try another.");
        }

        validatePassword(command, errors);

        UserProfile userProfile = command.getUserProfile();
        if (userProfile.getState() == null) {
            errors.rejectValue("state", "no_state", "You must specify a state");
            return; // avoid NPEs
        }
        if (StringUtils.isEmpty(userProfile.getCity())) {
            errors.rejectValue("city", "no_city", "You must specify a city");
        } else {
            ICity city = _geoDao.findCity(userProfile.getState(), userProfile.getCity());
            if (city == null) {
                errors.rejectValue("city", "bad_city", "That city does not exist in " +
                        userProfile.getState().getLongName());
            }
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

        if (StringUtils.isEmpty(password)) {
            errors.rejectValue("password", "empty_password", "Please enter a password.");
            return; // to prevent NPE's
        }

        if (password.length() < 6 || password.length() > 16) {
            errors.rejectValue("password", "bad_length_password", "Please choose a password between 6 and 16 characters long.");
        }

        if (StringUtils.isEmpty(confirmPassword) || !confirmPassword.equals(password)) {
            errors.rejectValue("password", "mismatched_password", "Please enter the same password into both fields.");
        }
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao _userDao) {
        this._userDao = _userDao;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }
}
