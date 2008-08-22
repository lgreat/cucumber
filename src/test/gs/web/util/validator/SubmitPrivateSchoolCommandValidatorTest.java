package gs.web.util.validator;

import gs.web.GsMockHttpServletRequest;
import gs.web.BaseTestCase;
import gs.web.about.feedback.SubmitPreschoolCommand;
import gs.web.about.feedback.SubmitPrivateSchoolCommand;
import org.springframework.validation.Errors;
import org.springframework.validation.BindException;

/**
 * Tests the add private school command validator
 * @author Young Fan
 */
public class SubmitPrivateSchoolCommandValidatorTest extends SubmitSchoolCommandValidatorTest {
    private static final String GOOD_LOWEST_GRADE = "PK";
    private static final String GOOD_HIGHEST_GRADE = "UG";

    private SubmitPrivateSchoolCommandValidator _validator;
    private GsMockHttpServletRequest _request;

    public void setUp() throws Exception {
        super.setUp();
        _validator = new SubmitPrivateSchoolCommandValidator();
        _request = new GsMockHttpServletRequest();
    }

    protected SubmitPrivateSchoolCommand setupCommand() {
        SubmitPrivateSchoolCommand command = (SubmitPrivateSchoolCommand)super.setupCommand(new SubmitPrivateSchoolCommand());
        command.setLowestGrade(GOOD_LOWEST_GRADE);
        command.setHighestGrade(GOOD_HIGHEST_GRADE);
        return command;
    }

    public void testSuccess() {
        SubmitPrivateSchoolCommand command = setupCommand();
        Errors errors = new BindException(command, "");
        _validator.validate(_request, command, errors);
        assertFalse(errors.toString(), errors.hasErrors());
    }

    public void testLowestGradeMissing() {
        SubmitPrivateSchoolCommand command = setupCommand();
        command.setLowestGrade("");
        Errors errors = new BindException(command, "");
        _validator.validate(_request, command, errors);
        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("lowestGrade"));
        assertEquals(SubmitPrivateSchoolCommandValidator.ERROR_LOWEST_GRADE_OFFERED_MISSING,
            errors.getFieldError("lowestGrade").getDefaultMessage());
    }

    public void testHighestGradeMissing() {
        SubmitPrivateSchoolCommand command = setupCommand();
        command.setHighestGrade("");
        Errors errors = new BindException(command, "");
        _validator.validate(_request, command, errors);
        assertTrue(errors.hasErrors());
        assertTrue(errors.hasFieldErrors("highestGrade"));
        assertEquals(SubmitPrivateSchoolCommandValidator.ERROR_HIGHEST_GRADE_OFFERED_MISSING,
            errors.getFieldError("highestGrade").getDefaultMessage());
    }
}
