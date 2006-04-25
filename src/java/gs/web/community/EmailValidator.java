package gs.web.community;

import org.springframework.validation.Validator;
import org.springframework.validation.Errors;
import org.apache.commons.lang.StringUtils;
import java.util.regex.Pattern;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class EmailValidator implements Validator {

    public static final String BEAN_ID = "betaEmailValidator";
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^\\w+([_\\.-]\\w+)*@(\\w+([_\\.-]\\w+)*)");

    public boolean supports(Class aClass) {
        return aClass.equals(BetaEmailCommand.class);
    }

    public void validate(Object object, Errors errors) {
        BetaEmailCommand command = (BetaEmailCommand)object;
        if (StringUtils.isBlank(command.getEmail()) ||
                !EMAIL_PATTERN.matcher(command.getEmail()).matches()) {
            errors.rejectValue("email", "invalid", "Please enter a valid email address");
        }
    }
}
