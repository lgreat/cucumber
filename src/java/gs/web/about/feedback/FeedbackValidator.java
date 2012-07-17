package gs.web.about.feedback;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.apache.commons.validator.EmailValidator;

/**
 * @author Dave Roy <mailto:droy@greatschools.net>
 */
public class FeedbackValidator implements Validator {
    private static String FIELD_REQUIRED_MSG = "This is a required field.";

    public boolean supports(Class aClass) {
        return aClass.equals(ContactUsCommand.class);
    }

    public void validate(Object object, Errors errors) {
        ContactUsCommand command = (ContactUsCommand) object;

        switch (command.getFeedbackType()) {
            case incorrectSchoolDistrictInfo_incorrectSchool:
                validateIncorrectSchoolInfo(command, errors);
                break;
            case incorrectSchoolDistrictInfo_incorrectDistrict:
                validateIncorrectDistrictInfo(command, errors);
                break;
            case schoolRatingsReviews:
                validateSchoolRatingsReviews(command, errors);
                break;
            case gsRatings:
                validateGsRatings(command, errors);
                break;
            case esp:
                validateEsp(command, errors);
                break;
            case join:
            case newsletters:
            case advertising:
            case licensing:
            case other:
                validateGeneral(command, errors);
                break;
            default:
                throw new RuntimeException ("No feedback type in ContactUsCommand: " + command.getFeedbackType());
        }

    }

    protected void validateIncorrectSchoolInfo(ContactUsCommand command, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "submitterName", null, FIELD_REQUIRED_MSG);
        if (errors.getFieldErrors("submitterEmail").size() > 0) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "submitterEmail", null, FIELD_REQUIRED_MSG);
        }
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "schoolId", null, FIELD_REQUIRED_MSG);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "cityName", null, FIELD_REQUIRED_MSG);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "state", null, FIELD_REQUIRED_MSG);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "schoolInfoFields.infoType", null, FIELD_REQUIRED_MSG);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "schoolInfoFields.relationship", null, FIELD_REQUIRED_MSG);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "schoolInfoFields.comment", null, FIELD_REQUIRED_MSG);

        if (errors.getFieldErrors("submitterEmail").size() == 0 && !getEmailValidator().isValid(command.getSubmitterEmail())) {
            errors.rejectValue("submitterEmail", null, "Please enter a valid email address.");
        }
    }

    protected void validateIncorrectDistrictInfo(ContactUsCommand command, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "submitterName", null, FIELD_REQUIRED_MSG);
        if (errors.getFieldErrors("submitterEmail").size() > 0) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "submitterEmail", null, FIELD_REQUIRED_MSG);
        }
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "districtInfoFields.districtName", null, FIELD_REQUIRED_MSG);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "state", null, FIELD_REQUIRED_MSG);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "districtInfoFields.relationship", null, FIELD_REQUIRED_MSG);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "districtInfoFields.comment", null, FIELD_REQUIRED_MSG);

        if (errors.getFieldErrors("submitterEmail").size() == 0 && !getEmailValidator().isValid(command.getSubmitterEmail())) {
            errors.rejectValue("submitterEmail", null, "Please enter a valid email address.");
        }
    }

    protected void validateSchoolRatingsReviews(ContactUsCommand command, Errors errors) {
        validateGeneralWithSchoolSelection(command, errors);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "schoolRatingsReviewsFields.comment", null, FIELD_REQUIRED_MSG);
    }

    protected void validateGsRatings(ContactUsCommand command, Errors errors) {
        validateGeneralWithSchoolSelection(command, errors);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "gsRatingsFields.comment", null, FIELD_REQUIRED_MSG);
    }

    private void validateGeneralWithSchoolSelection(ContactUsCommand command, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "submitterName", null, FIELD_REQUIRED_MSG);
        if (errors.getFieldErrors("submitterEmail").size() > 0) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "submitterEmail", null, FIELD_REQUIRED_MSG);
        }
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "schoolId", null, FIELD_REQUIRED_MSG);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "cityName", null, FIELD_REQUIRED_MSG);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "state", null, FIELD_REQUIRED_MSG);

        if (errors.getFieldErrors("submitterEmail").size() == 0 && !getEmailValidator().isValid(command.getSubmitterEmail())) {
            errors.rejectValue("submitterEmail", null, "Please enter a valid email address.");
        }
    }

    protected void validateEsp(ContactUsCommand command, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "submitterName", null, FIELD_REQUIRED_MSG);
        if (errors.getFieldErrors("submitterEmail").size() > 0) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "submitterEmail", null, FIELD_REQUIRED_MSG);
        }
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "schoolId", null, FIELD_REQUIRED_MSG);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "cityName", null, FIELD_REQUIRED_MSG);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "state", null, FIELD_REQUIRED_MSG);
//        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "espFields.comment", null, FIELD_REQUIRED_MSG);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "espFields.title", null, FIELD_REQUIRED_MSG);

        if (errors.getFieldErrors("submitterEmail").size() == 0 && !getEmailValidator().isValid(command.getSubmitterEmail())) {
            errors.rejectValue("submitterEmail", null, "Please enter a valid email address.");
        }

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "espFields.phone", null, FIELD_REQUIRED_MSG);
        String phone = command.getEspFields().getPhone();
        if (!StringUtils.isBlank(phone) && !phone.replaceAll("\\D", "").matches("\\d{10}")) {
            errors.rejectValue("espFields.phone", null, "Please enter a valid phone number.");
        }
    }

    protected void validateGeneral(ContactUsCommand command, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "submitterName", null, FIELD_REQUIRED_MSG);
        if (errors.getFieldErrors("submitterEmail").size() > 0) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "submitterEmail", null, FIELD_REQUIRED_MSG);
        }
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "generalFields.comment", null, FIELD_REQUIRED_MSG);

        if (errors.getFieldErrors("submitterEmail").size() == 0 && !getEmailValidator().isValid(command.getSubmitterEmail())) {
            errors.rejectValue("submitterEmail", null, "Please enter a valid email address.");
        }
    }

    protected EmailValidator getEmailValidator() {
        return EmailValidator.getInstance();
    }
}
