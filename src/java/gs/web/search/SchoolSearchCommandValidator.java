package gs.web.search;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


public class SchoolSearchCommandValidator implements Validator {

    public boolean supports(Class aClass) {
        return aClass.equals(SchoolSearchCommand.class);
    }

    public void validate(Object o, Errors errors) {

        SchoolSearchCommand command = (SchoolSearchCommand) o;

        if (command.getSearchString() != null && StringUtils.trimToNull(command.getSearchString()) == null) {
            errors.rejectValue("searchString", "searchString", "Query must not be empty");
        }
    }
}
