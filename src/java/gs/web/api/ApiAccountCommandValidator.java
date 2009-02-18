package gs.web.api;

import org.springframework.validation.Validator;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.apache.commons.lang.StringUtils;
import gs.data.api.ApiAccount;
import gs.data.util.email.EmailUtils;

/**
 * Created by chriskimm@greatschools.net
 */
public class ApiAccountCommandValidator implements Validator {

    // Error message - shown on spring form-bound pages
    public static final String REQUIRED_FIELD = "required";
    public static final String INVALID_EMAIL = "Invalid Address";
    public static final String EMAIL_CONFIRM_MISMATCH = "Email mismatch";

    public boolean supports(Class aClass) {
        return ApiAccount.class.isAssignableFrom(aClass);
    }

    public void validate(Object o, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "field.required", REQUIRED_FIELD);

        ApiAccount account = (ApiAccount)o;
        String email = account.getEmail();
        if (StringUtils.isBlank(email)) {
            errors.rejectValue("email", "field.required", REQUIRED_FIELD);
        } else if (!EmailUtils.isValidEmail(email)) {
            errors.rejectValue("email", "invalid.email", INVALID_EMAIL);
        }

        String confirm = account.getConfirmEmail();
        if (StringUtils.isBlank(confirm)) {
            errors.rejectValue("confirmEmail", "field.required", REQUIRED_FIELD);
        } else if (email != null && !email.equals(confirm)) {
            errors.rejectValue("confirmEmail", "confirm.email.mismatch", EMAIL_CONFIRM_MISMATCH);
        }
    }
}
