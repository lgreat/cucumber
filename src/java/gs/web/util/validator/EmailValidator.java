package gs.web.util.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class EmailValidator implements Validator {
    public static interface IEmail {
        String getEmail();
    }
    public static final String BEAN_ID = "emailValidator";

    public boolean supports(Class aClass) {
        Class [] iFaces = aClass.getInterfaces();
        for (int i=0; i < iFaces.length; i++) {
            if (iFaces[i].equals(IEmail.class)) {
                return true;
            }
        }
        return false;
    }

    public void validate(Object object, Errors errors) {
        IEmail command = (IEmail)object;
        org.apache.commons.validator.EmailValidator emv = org.apache.commons.validator.EmailValidator.getInstance();

        if (!emv.isValid(command.getEmail())) {
            errors.rejectValue("email", "invalid", "Please enter a valid email address");
        }
    }
}
