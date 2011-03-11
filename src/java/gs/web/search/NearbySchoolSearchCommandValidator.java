package gs.web.search;

import gs.data.school.LevelCode;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Created by IntelliJ IDEA.
 * User: youngfan
 * Date: 3/2/11
 * Time: 1:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class NearbySchoolSearchCommandValidator implements Validator {

    private final static String ZIP_PATTERN = "^\\d{5}$";

    public boolean supports(Class aClass) {
        return aClass.equals(NearbySchoolSearchCommand.class);
    }

    public void validate(Object o, Errors errors) {
        NearbySchoolSearchCommand command = ((NearbySchoolSearchCommand)o);

        try {
            int distance = Integer.parseInt(command.getDistance());
            if (distance <= 0) {
                errors.rejectValue("distance", "distance", "Distance must be greater than 0");
            }
        } catch (NumberFormatException e) {
            errors.rejectValue("distance", "distance","Not a number");
        }

        if (command.getZipCode() == null || !command.getZipCode().matches(ZIP_PATTERN)) {
            errors.rejectValue("zipCode", "zipCode", "Zip code must be 5 digits");
        }

    }
}
