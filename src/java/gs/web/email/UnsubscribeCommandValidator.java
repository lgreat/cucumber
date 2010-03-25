package gs.web.email;

import org.springframework.validation.Validator;
import org.springframework.validation.Errors;

/**
 * Created by IntelliJ IDEA.
 * User: eddie
 * Date: Mar 24, 2010
 * Time: 2:23:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class UnsubscribeCommandValidator  implements Validator {

    public static final String REQUIRED_FIELD = "required";

    public void validate(Object o, Errors errors) {
        //ValidationUtils.rejectIfEmptyOrWhitespace(errors, "intendedUse", "field.required", REQUIRED_FIELD);
    }

    public boolean supports(Class aClass) {
         return ManagementCommand.class.isAssignableFrom(aClass);
     }

}

