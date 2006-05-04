package gs.web.util.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class SchoolIdValidator implements Validator {

    public static final String BEAN_ID = "schoolIdValidator";

    public static interface ISchoolId {
        int getSchoolId();
    }

    public boolean supports(Class aClass) {
        Class [] iFaces = aClass.getInterfaces();
        for (int i=0; i < iFaces.length; i++) {
            if (iFaces[i].equals(ISchoolId.class)) {
                return true;
            }
        }
        return false;
    }

    public void validate(Object object, Errors errors) {
        ISchoolId command = (ISchoolId)object;
        if (command.getSchoolId() == 0) {
            errors.rejectValue("schoolId", "invalid","School Id was not specified");
        }
    }
}