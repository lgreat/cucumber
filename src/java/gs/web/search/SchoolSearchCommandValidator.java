package gs.web.search;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


public class SchoolSearchCommandValidator implements Validator{

    public boolean supports(Class aClass) {
        return aClass.equals(SchoolSearchCommand.class);
    }

    public void validate(Object o, Errors errors) {

        SchoolSearchCommand command = (SchoolSearchCommand) o;

        if (StringUtils.isBlank(command.getSearchString())) {
            errors.rejectValue("queryString", "queryString", "Query must not be empty");
        }

        //TODO: validate
    }
}
