package gs.web.util.validator;

import org.springframework.validation.Errors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;

import gs.web.about.feedback.SubmitPrivateSchoolCommand;

public class SubmitPrivateSchoolCommandValidator extends SubmitSchoolCommandValidator {
    public static final String BEAN_ID = "submitPrivateSchoolCommandValidator";
    protected final Log _log = LogFactory.getLog(getClass());

    static final String ERROR_LOWEST_GRADE_OFFERED_MISSING =
        "Please select the lowest grade offered.";
    static final String ERROR_HIGHEST_GRADE_OFFERED_MISSING =
        "Please select the highest grade offered.";

    public void validate(HttpServletRequest request, Object object, Errors errors) {
        super.validate(request, object, errors);

        SubmitPrivateSchoolCommand command = (SubmitPrivateSchoolCommand)object;

        if (StringUtils.isBlank(command.getLowestGrade())) {
            errors.rejectValue("lowestGrade", null, ERROR_LOWEST_GRADE_OFFERED_MISSING);
        }

        if (StringUtils.isBlank(command.getHighestGrade())) {
            errors.rejectValue("highestGrade", null, ERROR_HIGHEST_GRADE_OFFERED_MISSING);
        }
    }
}
