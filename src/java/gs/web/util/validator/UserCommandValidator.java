package gs.web.util.validator;

import gs.data.community.IUserDao;
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

    public boolean supports(Class aClass) {
        return aClass == UserCommand.class;
    }

    /**
     * Checks the password field in the UserCommand object. The password must be between
     * 6 and 16 characters long, and must match the confirmPassword field.
     * @param object
     * @param errors
     */
    public void validate(Object object, Errors errors) {
        UserCommand command = (UserCommand)object;

        String email = command.getEmail();
        String confirmEmail = command.getConfirmEmail();

        // Don't allow people to pick existing emails unless they've been redirected here
        // (redirection is detected by checking id)
        if (command.getUser().getId() == null &&_userDao.findUserFromEmailIfExists(email) != null) {
            errors.rejectValue("email", "existing_user", "This email already exists, please choose a different one.");
        }

        if (command.getUser().getId() == null) {
            // verify confirm email matches email
            if (!confirmEmail.equals(email)) {
                errors.rejectValue("email", "mismatched_email", "Please enter the same email into both fields.");
            }
        }

        validatePassword(command, errors);
    }
     
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
}
