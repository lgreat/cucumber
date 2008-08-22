package gs.web.util.validator;

import org.springframework.validation.Errors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.*;
import org.apache.commons.validator.EmailValidator;

import javax.servlet.http.HttpServletRequest;

import gs.web.about.feedback.SubmitSchoolCommand;

public class SubmitSchoolCommandValidator implements IRequestAwareValidator {
    public static final String BEAN_ID = "submitSchoolCommandValidator";
    protected final Log _log = LogFactory.getLog(getClass());

    static final String ERROR_SUBMITTER_NAME_MISSING =
        "Please enter your name.";
    static final String ERROR_SUBMITTER_EMAIL_MISSING =
        "Please enter your email address.";
    static final String ERROR_SUBMITTER_EMAIL_INVALID =
        "Please enter a valid email address.";
    static final String ERROR_SUBMITTER_EMAIL_UNMATCHED =
        "Please re-enter your email address.";
    static final String ERROR_SUBMITTER_CONNECTION_TO_SCHOOL_MISSING =
        "Please enter your connection to the school.";

    static final String ERROR_SCHOOL_NAME_MISSING =
        "Please enter the school name.";
    static final String ERROR_STREET_ADDRESS_MISSING =
        "Please enter the physical street address.";
    static final String ERROR_CITY_MISSING =
        "Please enter the city.";
    static final String ERROR_STATE_MISSING =
        "Please enter the state.";
    static final String ERROR_ZIP_CODE_MISSING =
        "Please enter the zip code.";
    static final String ERROR_ZIP_CODE_INVALID =
        "Please enter a valid zip code.";
    static final String ERROR_COUNTY_MISSING =
        "Please enter the county.";

    static final String ERROR_NUM_STUDENTS_ENROLLED_MISSING =
        "Please enter the number of students enrolled.";
    static final String ERROR_PHONE_NUMBER_MISSING =
        "Please enter the phone number.";
    static final String ERROR_PHONE_NUMBER_INVALID =
        "Please enter a valid phone number.";
    static final String ERROR_FAX_NUMBER_INVALID =
        "Please enter a valid fax number.";
    static final String ERROR_SCHOOL_WEB_SITE_INVALID =
        "Please enter a valid school web site.";

    public void validate(HttpServletRequest request, Object object, Errors errors) {
        SubmitSchoolCommand command = (SubmitSchoolCommand)object;
        EmailValidator emv = EmailValidator.getInstance();

        // personal information

        if (StringUtils.isBlank(command.getSubmitterName())) {
            errors.rejectValue("submitterName", null, ERROR_SUBMITTER_NAME_MISSING);
        }

        if (StringUtils.isBlank(command.getSubmitterEmail())) {
            errors.rejectValue("submitterEmail", null, ERROR_SUBMITTER_EMAIL_MISSING);
        } else if (!emv.isValid(command.getSubmitterEmail())) {
            errors.rejectValue("submitterEmail", null, ERROR_SUBMITTER_EMAIL_INVALID);
        } else if (StringUtils.isBlank(command.getSubmitterEmailConfirm()) ||
            !command.getSubmitterEmail().equals(command.getSubmitterEmailConfirm())) {
            errors.rejectValue("submitterEmailConfirm", null, ERROR_SUBMITTER_EMAIL_UNMATCHED);
        }

        if (StringUtils.isBlank(command.getSubmitterConnectionToSchool())) {
            errors.rejectValue("submitterConnectionToSchool", null, ERROR_SUBMITTER_CONNECTION_TO_SCHOOL_MISSING);
        }

        // school information

        if (StringUtils.isBlank(command.getSchoolName())) {
            errors.rejectValue("schoolName", null, ERROR_SCHOOL_NAME_MISSING);
        }

        if (StringUtils.isBlank(command.getStreetAddress())) {
            errors.rejectValue("streetAddress", null, ERROR_STREET_ADDRESS_MISSING);
        }

        if (StringUtils.isBlank(command.getCity())) {
            errors.rejectValue("city", null, ERROR_CITY_MISSING);
        }

        if (command.getState() == null) {
            errors.rejectValue("state", null, ERROR_STATE_MISSING);
        }

        if (StringUtils.isBlank(command.getZipCode())) {
            errors.rejectValue("zipCode", null, ERROR_ZIP_CODE_MISSING);
        } else if (!command.getZipCode().matches("^[0-9]{5}(-[0-9]{4})?$")) {
            errors.rejectValue("zipCode", null, ERROR_ZIP_CODE_INVALID);
        }

        if (StringUtils.isBlank(command.getCounty())) {
            errors.rejectValue("county", null, ERROR_COUNTY_MISSING);
        }


        if (StringUtils.isBlank(command.getNumStudentsEnrolled())) {
            errors.rejectValue("numStudentsEnrolled", null, ERROR_NUM_STUDENTS_ENROLLED_MISSING);
        } else {
            boolean valid = true;
            try {
                int num = Integer.parseInt(command.getNumStudentsEnrolled());
                if (num <= 0) {
                    valid = false;
                }
            } catch (NumberFormatException e) {
                valid = false;
            }

            if (!valid) {
                errors.rejectValue("numStudentsEnrolled", null, ERROR_NUM_STUDENTS_ENROLLED_MISSING);
            }
        }

        if (StringUtils.isBlank(command.getPhoneNumber())) {
            errors.rejectValue("phoneNumber", null, ERROR_PHONE_NUMBER_MISSING);
        } else if (!isValidPhoneNumber(command.getPhoneNumber())) {
            errors.rejectValue("phoneNumber", null, ERROR_PHONE_NUMBER_INVALID);
        } else {
            command.setPhoneNumber(formatPhoneNumber(command.getPhoneNumber()));
        }

        if (!StringUtils.isBlank(command.getFaxNumber()) && !isValidPhoneNumber(command.getFaxNumber())) {
            errors.rejectValue("faxNumber", null, ERROR_FAX_NUMBER_INVALID);
        } else if (isValidPhoneNumber(command.getFaxNumber())) {
            command.setFaxNumber(formatPhoneNumber(command.getFaxNumber()));
        }

        if (!StringUtils.isBlank(command.getSchoolWebSite()) && !isValidUrl(command.getSchoolWebSite())) {
            if (isValidUrl("http://" + command.getSchoolWebSite())) {
                command.setSchoolWebSite("http://" + command.getSchoolWebSite());
            } else {
                errors.rejectValue("schoolWebSite", null, ERROR_SCHOOL_WEB_SITE_INVALID);
            }
        }
    }

    /**
     * @see <a href="http://www.dpawson.co.uk/xsl/sect2/N5846.html#d8714e307">Remove non-digit characters from a string</a>
     * @param phoneNumber
     * @return
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }

        return normalizePhoneNumber(phoneNumber).length() == 10;
    }

    public static String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            throw new IllegalArgumentException("Phone number must not be null");
        }
        StringBuilder normalized = new StringBuilder();
        for (int i = 0; i < phoneNumber.length(); i++) {
            if (Character.isDigit(phoneNumber.charAt(i))) {
                normalized.append(phoneNumber.charAt(i));
            }
        }
        return normalized.toString();
    }

    public static String formatPhoneNumber(String phoneNumber) {
        if (!isValidPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("Phone number must be 10 digits");
        }
        String normalized = normalizePhoneNumber(phoneNumber);
        StringBuilder formatted = new StringBuilder();

        formatted.append("(");
        formatted.append(normalized.substring(0,3));
        formatted.append(")");
        formatted.append(" ");
        formatted.append(normalized.substring(3,6));
        formatted.append("-");
        formatted.append(normalized.substring(6,10));

        return formatted.toString();
    }

    /**
     * @param webSite
     * @return
     */
    public static boolean isValidUrl(String webSite) {
        String[] schemes = {"http"};
        UrlValidator validator = new UrlValidator(schemes);
        return validator.isValid(webSite);
    }
}
