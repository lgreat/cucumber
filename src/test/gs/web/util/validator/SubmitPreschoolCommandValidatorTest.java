package gs.web.util.validator;

import gs.web.GsMockHttpServletRequest;
import gs.web.about.feedback.SubmitPreschoolCommand;
import org.springframework.validation.Errors;
import org.springframework.validation.BindException;

/**
 * @author Young Fan
 */
public class SubmitPreschoolCommandValidatorTest extends SubmitSchoolCommandValidatorTest {
    private static final String GOOD_LOWEST_AGE = "1";
    private static final String GOOD_HIGHEST_AGE = "Up";

    private SubmitPreschoolCommandValidator _validator;
    private GsMockHttpServletRequest _request;

    public void setUp() throws Exception {
        super.setUp();
        _validator = new SubmitPreschoolCommandValidator();
        _request = new GsMockHttpServletRequest();        
    }

    protected SubmitPreschoolCommand setupCommand() {
        SubmitPreschoolCommand command = (SubmitPreschoolCommand)super.setupCommand(new SubmitPreschoolCommand());        
        command.setLowestAge(GOOD_LOWEST_AGE);
        command.setHighestAge(GOOD_HIGHEST_AGE);
        return command;
    }

    public void testSuccess() {
        SubmitPreschoolCommand command = setupCommand();
        Errors errors = new BindException(command, "");
        _validator.validate(_request, command, errors);
        assertFalse(errors.toString(), errors.hasErrors());
    }

    public void testLowestAgeMissing() {
        SubmitPreschoolCommand command = setupCommand();
        command.setLowestAge("");
        Errors errors = new BindException(command, "");
        _validator.validate(_request, command, errors);
        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("lowestAge"));
        assertEquals(SubmitPreschoolCommandValidator.ERROR_LOWEST_AGE_SERVED_MISSING,
            errors.getFieldError("lowestAge").getDefaultMessage());
    }

    public void testHighestAgeMissing() {
        SubmitPreschoolCommand command = setupCommand();
        command.setHighestAge("");
        Errors errors = new BindException(command, "");
        _validator.validate(_request, command, errors);
        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("highestAge"));
        assertEquals(SubmitPreschoolCommandValidator.ERROR_HIGHEST_AGE_SERVED_MISSING,
            errors.getFieldError("highestAge").getDefaultMessage());
    }
}
