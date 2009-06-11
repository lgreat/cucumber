package gs.web.email;

import org.springframework.validation.Validator;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

/**
 * Created by IntelliJ IDEA.
 * User: eddie
 * Date: May 26, 2009
 * Time: 1:27:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class ManagementCommandValidator  implements Validator {

    public static final String REQUIRED_FIELD = "required";

    public void validate(Object o, Errors errors) {
        //ValidationUtils.rejectIfEmptyOrWhitespace(errors, "intendedUse", "field.required", REQUIRED_FIELD);
    }

    public boolean supports(Class aClass) {
         return ManagementCommand.class.isAssignableFrom(aClass);
     }

}
