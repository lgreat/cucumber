package gs.web.util.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import gs.web.community.registration.UserCommand;
import gs.data.community.IUserDao;

/**
 * Validates a UserCommand object, created for use in community registration
 * User: UrbanaSoft
 * Date: Jun 15, 2006
 * Time: 12:10:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserCommandValidator implements Validator {

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

        String password = command.getPassword();
        String confirmPassword = command.getConfirmPassword();
        String email = command.getEmail();

        if (_userDao.findUserFromEmailIfExists(email) != null) {
            errors.rejectValue("email", "existing_user", "This email already exists");
        }

        if (password == null) {
            errors.rejectValue("password", "empty_password", "Please enter a password.");
            return; // to prevent NPE's
        }

        if (password.length() < 6 || password.length() > 16) {
            errors.rejectValue("password", "bad_length_password", "Please choose a password between 6 and 16 characters long.");
        }

        if (confirmPassword == null || !confirmPassword.equals(password)) {
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
