package gs.web.util.validator;

import gs.web.about.feedback.AddEditSchoolOrDistrictCommand;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.*;
import org.springframework.validation.Errors;
import org.apache.commons.validator.EmailValidator;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by IntelliJ IDEA.
 * User: eddie
 * Date: 12/21/11
 * Time: 12:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddEditSchoolOrDistrictCommandValidator implements IRequestAwareValidator  {
    public static final String BEAN_ID = "addEditSchoolOrDistrictCommandValidator";
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
    static final String ERROR_ADD_EDIT =
            "Please choose whether you wish to add or edit.";
    static final String ERROR_GRADES =
            "Please choose at least one grade.";

    static final String ERROR_SCHOOLTYPE_MISSING =
            "Please select if the school is public, private or charter.";

    public void validate(HttpServletRequest request, Object object, Errors errors) {
        AddEditSchoolOrDistrictCommand command = (AddEditSchoolOrDistrictCommand)object;
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
        if (StringUtils.isBlank(command.getAddEdit())) {
            errors.rejectValue("addEdit", null, ERROR_ADD_EDIT);
        }
        if (StringUtils.isBlank(command.getGrades())) {
            //errors.rejectValue("grades", null, ERROR_GRADES);
        }

        if (command.getSchoolType() == null ) {
            //errors.rejectValue("schoolType", null, ERROR_SCHOOLTYPE_MISSING);
        }


    }

}
