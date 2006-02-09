package gs.web.status;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class LoginValidator implements Validator {

    private final Log _log = LogFactory.getLog(getClass());

    public boolean supports(Class clazz) {
        return clazz.equals(Identity.class);
    }

    public void validate(Object obj, Errors errors) {
        Identity identity = (Identity) obj;
        if (identity == null) {
            errors.rejectValue("username", "error_invalid_username", null,
                    "Value required.");
        } else {
            _log.info("Validating user credentials for: " + identity.getUsername());
            if (identity.getUsername().equals("gsadmin") == false) {
                _log.info("incorrect username:" + identity.getUsername());
                errors.rejectValue("username", "error_invalid_username", null, "Incorrect Username.");
            } else {
                if (identity.getPassword().equals("!nd8x") == false) {
                    _log.info("incorrect password:" + identity.getPassword());
                    errors.rejectValue("password", "error_invalid_password", null, "Incorrect Password.");
                }
            }

        }
    }

}