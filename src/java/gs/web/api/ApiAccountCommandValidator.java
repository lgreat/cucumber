package gs.web.api;

import org.springframework.validation.Validator;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.apache.commons.lang.StringUtils;
import gs.data.api.ApiAccount;
import gs.data.util.email.EmailUtils;

/**
 * Created by chriskimm@greatschools.org
 */
public class ApiAccountCommandValidator implements Validator {

    // Error message - shown on spring form-bound pages
    public static final String REQUIRED_FIELD = "required";
    public static final String INVALID_EMAIL = "Invalid Address";
    public static final String EMAIL_CONFIRM_MISMATCH = "'Confirm Email' value must match Email of Contact";

    public boolean supports(Class aClass) {
        return ApiAccount.class.isAssignableFrom(aClass);
    }

    public void validate(Object o, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "field.required", REQUIRED_FIELD);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "phone", "field.required", REQUIRED_FIELD);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "website", "field.required", REQUIRED_FIELD);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "intendedUse", "field.required", REQUIRED_FIELD);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "organization", "field.required", REQUIRED_FIELD);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "industry", "field.required", REQUIRED_FIELD);

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

        if (!account.getTermsApproved()) {
            errors.rejectValue("termsApproved", "field.required", REQUIRED_FIELD);
        }
    }
}
