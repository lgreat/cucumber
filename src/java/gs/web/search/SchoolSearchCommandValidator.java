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

        //setting the page size to 0 will disable paging and return all results.
        //we do not want to allow the user to do this, therefore validate that it is at least 1.
        if (command.getPageSize() < 1 || command.getPageSize() > SchoolSearchController.MAX_PAGE_SIZE) {
            errors.rejectValue("pageSize", "pageSize", "Invalid page size");
        }

        if (command.getStart() < 0) {
            errors.rejectValue("start", "start", "Invalid result offset");
        }
        
    }
}
