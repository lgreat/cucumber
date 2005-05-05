/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: PaymentValidator.java,v 1.1 2005/05/05 02:17:03 apeterson Exp $
 */
package gs.web;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.Calendar;

/**
 * @author Andrew J. Peterson <mailto:apeterson@greatschools.net>
 */
public class PaymentValidator implements Validator {

    public boolean supports(Class clazz) {
        return PaymentCommand.class.isAssignableFrom(clazz);
    }

    public void validate(Object obj, Errors errors) {
        ValidationUtils.rejectIfEmpty(errors, "agree", "agree_to_terms");

        ValidationUtils.rejectIfEmpty(errors, "user.firstName", "field_required", new String[]{"First name"}, "{0} is required.");
        ValidationUtils.rejectIfEmpty(errors, "user.lastName", "field_required", new String[]{"Last name"}, "{0} is required.");
        ValidationUtils.rejectIfEmpty(errors, "user.email", "field_required", new String[]{"Email address"}, "{0} is required.");
        ValidationUtils.rejectIfEmpty(errors, "user.address.street", "field_required", new String[]{"Street"}, "{0} is required.");
        ValidationUtils.rejectIfEmpty(errors, "user.address.city", "field_required", new String[]{"City"}, "{0} is required.");
        ValidationUtils.rejectIfEmpty(errors, "user.address.stateAbbreviation", "field_required", new String[]{"State"}, "{0} is required.");
        ValidationUtils.rejectIfEmpty(errors, "user.address.zip", "field_required", new String[]{"Zip"}, "{0} is required.");

        ValidationUtils.rejectIfEmpty(errors, "creditCardNumber", "field_required", new String[]{"Credit card number"}, "{0} is required.");

        Object value = errors.getFieldValue("user.email");
        if (value == null ||
                StringUtils.countMatches(value.toString(), "@") != 1 ||
                StringUtils.countMatches(value.toString(), ".") < 1
        ) {
            errors.rejectValue("user.email", "email_address_format", null, "Email address must be 'name@company.org'.");
        }

        value = errors.getFieldValue("creditCardNumber");
        if (value == null ||
                value.toString().length() != 16 ||
                !StringUtils.isNumeric(value.toString())
        ) {
            errors.rejectValue("creditCardNumber", "credit_card_format", null, "Credit card number must be 16 digits.");
        }

        ValidationUtils.rejectIfEmpty(errors, "expirationMonth", "field_required", new String[]{"Expiration month"}, "{0} is required.");
        value = errors.getFieldValue("expirationMonth");
        if (value != null && !StringUtils.isEmpty(value.toString())) {
            final String strValue = value.toString();
            if (!StringUtils.isNumeric(strValue) ||
                    strValue.length() > 2 ||
                    Integer.parseInt(strValue) < 1 ||
                    Integer.parseInt(strValue) > 12
            ) {
                errors.rejectValue("expirationMonth", "credit_card_expiration", null, "Credit card expiration date must be <b>month/year</b>.");
            }
        }

        ValidationUtils.rejectIfEmpty(errors, "expirationYear", "field_required", new String[]{"Expiration year"}, "{0} is required.");
        value = errors.getFieldValue("expirationYear");
        if (value != null && !StringUtils.isEmpty(value.toString())) {
            final String strValue = value.toString();
            if (!StringUtils.isNumeric(strValue) ||
                    strValue.length() > 4
            ) {
                errors.rejectValue("expirationYear", "credit_card_expiration", null, "Credit card expiration date must be <b>month/year</b>.");
            } else {
                int year = Integer.parseInt(strValue);
                if (year < 2000) {
                    year += 2000;
                }
                Calendar c = Calendar.getInstance();
                final int firstValidYear = c.get(Calendar.YEAR);
                final int lastValidYear = firstValidYear + 10;
                if (year < firstValidYear || year > lastValidYear) {
                    errors.rejectValue("expirationYear", "credit_card_expiration", null, "Credit card expiration date must be <b>month/year</b>.");
                }
            }
        }

    }

}
