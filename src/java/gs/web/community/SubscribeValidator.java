/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SubscribeValidator.java,v 1.1 2005/06/06 18:03:38 apeterson Exp $
 */
package gs.web.community;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.Calendar;

/**
 * @author Andrew J. Peterson <mailto:apeterson@greatschools.net>
 */
public class SubscribeValidator implements Validator {

    public boolean supports(Class clazz) {
        return SubscribeCommand.class.isAssignableFrom(clazz);
    }

    public void validate(Object obj, Errors errors) {
        ValidationUtils.rejectIfEmpty(errors, "agree", "error_agree_to_terms");

        ValidationUtils.rejectIfEmpty(errors, "user.firstName", "error_field_required", new String[]{"First name"}, "{0} is required.");
        ValidationUtils.rejectIfEmpty(errors, "user.lastName", "error_field_required", new String[]{"Last name"}, "{0} is required.");

        ValidationUtils.rejectIfEmpty(errors, "user.address.state", "error_field_required", new String[]{"State"}, "{0} is required.");
        ValidationUtils.rejectIfEmpty(errors, "user.address.street", "error_field_required", new String[]{"Street"}, "{0} is required.");
        ValidationUtils.rejectIfEmpty(errors, "user.address.city", "error_field_required", new String[]{"City"}, "{0} is required.");


        Object value = errors.getFieldValue("user.address.zip");
        if (value == null || // empty or null
                (value.toString().length() != 5 && value.toString().length() != 9) || // wrong length
                !StringUtils.isNumeric(value.toString())
        ) {
            errors.rejectValue("user.address.zip", "error_zip_format", null, "The zip code must be a five digit number.");
        }

        value = errors.getFieldValue("user.email");
        if (value == null ||
                StringUtils.countMatches(value.toString(), "@") != 1 ||
                StringUtils.countMatches(value.toString(), ".") < 1
        ) {
            errors.rejectValue("user.email", "error_email_address_format", null, "Email address is required and must be 'name@company.org'.");
        }

        value = errors.getFieldValue("creditCardNumber");
        if (value == null ||
                value.toString().length() < 15 ||
                value.toString().length() > 16 ||
                !StringUtils.isNumeric(value.toString())
        ) {
            errors.rejectValue("creditCardNumber", "error_credit_card_format", null, "Credit card number invalid.");
        }

        ValidationUtils.rejectIfEmpty(errors, "expirationMonth", "error_field_required", new String[]{"Expiration month"}, "{0} is required.");
        value = errors.getFieldValue("expirationMonth");
        if (value != null && !StringUtils.isEmpty(value.toString())) {
            final String strValue = value.toString();
            if (!StringUtils.isNumeric(strValue) ||
                    strValue.length() > 2 ||
                    Integer.parseInt(strValue) < 1 ||
                    Integer.parseInt(strValue) > 12
            ) {
                errors.rejectValue("expirationMonth", "error_credit_card_expiration", null, "Credit card expiration date must be <b>month/year</b>.");
            }
        }

        ValidationUtils.rejectIfEmpty(errors, "expirationYear", "error_field_required", new String[]{"Expiration year"}, "{0} is required.");
        value = errors.getFieldValue("expirationYear");
        if (value != null && !StringUtils.isEmpty(value.toString())) {
            final String strValue = value.toString();
            if (!StringUtils.isNumeric(strValue) ||
                    strValue.length() > 4
            ) {
                errors.rejectValue("expirationYear", "error_credit_card_expiration", null, "Credit card expiration date must be <b>month/year</b>.");
            } else {
                int year = Integer.parseInt(strValue);
                if (year < 2000) {
                    year += 2000;
                }
                Calendar c = Calendar.getInstance();
                final int firstValidYear = c.get(Calendar.YEAR);
                final int lastValidYear = firstValidYear + 10;
                if (year < firstValidYear || year > lastValidYear) {
                    errors.rejectValue("expirationYear", "error_credit_card_expiration", null, "Credit card expiration date must be <b>month/year</b>.");
                }
            }
        }

    }

}
