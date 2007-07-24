package gs.web.util.validator;

import gs.web.school.review.ParentReviewFormController;
import gs.web.school.review.ReviewCommand;
import junit.framework.TestCase;
import org.springframework.validation.BindException;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class AddParentReviewAjaxPageValidatorTest extends TestCase {
    ParentReviewFormController.AjaxPageValidator _validator;
    BindException _errors;
    ReviewCommand _command;

    public void setUp() {
        _validator = new ParentReviewFormController.AjaxPageValidator();
        _command = new ReviewCommand();
        _errors = new BindException(_command, "");

    }

    public void testSupports() throws Exception {
        assertTrue(_validator.supports(ReviewCommand.class));
    }

    public void testNothingSet() throws Exception {
        _validator.validate(_command, _errors);
        assertEquals(2, _errors.getErrorCount());
    }

    public void testNoPermission() throws Exception {
        _command.setComments("this school has a comment");
        _validator.validate(_command, _errors);
        assertEquals(1, _errors.getErrorCount());
    }

    public void testNoCommentAndRating() throws Exception {
        _command.setGivePermission(true);
        _validator.validate(_command, _errors);
        assertEquals(1, _errors.getErrorCount());
    }

    public void testHasNoErrors() throws Exception {
        _command.setGivePermission(true);
        _command.setComments("has comments");
        assertFalse(_errors.hasErrors());       
    }
}
