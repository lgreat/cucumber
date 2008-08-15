package gs.web.util.validator;

import org.springframework.validation.Errors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;

import gs.web.about.feedback.SubmitPreschoolCommand;

public class SubmitPreschoolCommandValidator extends SubmitSchoolCommandValidator {
    public static final String BEAN_ID = "submitPreschoolCommandValidator";
    protected final Log _log = LogFactory.getLog(getClass());

    private static final String ERROR_LOWEST_AGE_SERVED_MISSING =
        "Please select the lowest age served.";
    private static final String ERROR_HIGHEST_AGE_SERVED_MISSING =
        "Please select the highest age served.";

    public void validate(HttpServletRequest request, Object object, Errors errors) {
        super.validate(request, object, errors);

        SubmitPreschoolCommand command = (SubmitPreschoolCommand)object;

        if (StringUtils.isBlank(command.getLowestAge())) {
            errors.rejectValue("lowestAge", null, ERROR_LOWEST_AGE_SERVED_MISSING);
        }

        if (StringUtils.isBlank(command.getHighestAge())) {
            errors.rejectValue("highestAge", null, ERROR_HIGHEST_AGE_SERVED_MISSING);
        }
    }
}
