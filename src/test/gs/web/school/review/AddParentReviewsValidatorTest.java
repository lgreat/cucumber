package gs.web.school.review;

import junit.framework.TestCase;
import org.springframework.validation.BindException;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class AddParentReviewsValidatorTest extends TestCase {
    AddParentReviewsValidator _validator;
    BindException _errors;
    ReviewCommand _command;

    public void setUp() {
        _validator = new AddParentReviewsValidator();
        _command = new ReviewCommand();
        _errors = new BindException(_command, "");
    }

    public void testSupports() throws Exception {
        assertTrue(_validator.supports(ReviewCommand.class));
    }

    public void testNothingSet() throws Exception {
        _validator.validate(_command, _errors);
        assertEquals(3, _errors.getErrorCount());
    }

    public void testNoPermission() throws Exception {
        _validator.validate(_command, _errors);
        assertTrue(_errors.hasFieldErrors("givePermission"));
    }

    public void testNoCommentAndRating() throws Exception {
        _validator.validate(_command, _errors);
        assertTrue(_errors.hasFieldErrors("comments"));
    }

    public void testNoCommentAndNullRating() throws Exception {
        _command.setOverallAsString("0");
        _validator.validate(_command, _errors);
        assertTrue(_errors.hasFieldErrors("comments"));
    }

    public void testPosterNotSpecified() throws Exception {
        _validator.validate(_command, _errors);
        assertTrue(_errors.hasFieldErrors("posterAsString"));
    }

    public void testEmailDoesNotMatch() throws Exception {
        _command.setEmail("dlee@greatschool.net");
        _command.setConfirmEmail("dlee@greatschools.ne");
        _validator.validate(_command, _errors);
        assertTrue(_errors.hasFieldErrors("confirmEmail"));
    }

    public void testHasNoErrors() throws Exception {
        _command.setGivePermission(true);
        _command.setComments("has comments");
        _command.setPosterAsString("principal");
        _validator.validate(_command, _errors);
        assertFalse(_errors.hasErrors());
    }
}
