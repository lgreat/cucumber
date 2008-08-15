package gs.web.util.validator;

import org.springframework.validation.Errors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;

public class SubmitPrivateSchoolCommandValidator extends SubmitSchoolCommandValidator {
    public static final String BEAN_ID = "submitPrivateSchoolCommandValidator";
    protected final Log _log = LogFactory.getLog(getClass());

    public void validate(HttpServletRequest request, Object object, Errors errors) {
        super.validate(request, object, errors);
        // TODO-6868
    }
}
