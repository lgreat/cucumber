package gs.web.school.review;

import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;

public class SchoolReviewValidatorTest extends TestCase {
    SchoolReviewValidator _validator;
    BindException _errors;
    ReviewCommand _command;

    public void setUp() {
        _validator = new SchoolReviewValidator();
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

    public void testNoComment() throws Exception {
        _validator.validate(_command, _errors);
        assertTrue(_errors.hasFieldErrors("comments"));
    }

    public void testCommentTooShort() throws Exception {
        _command.setComments("This comment has less than 15 words");
        _validator.validate(_command, _errors);
        assertTrue(_errors.hasFieldErrors("comments"));
    }
        
    public void testCommentTooLong() throws Exception {
        _command.setComments("This comment has more than 1200 characters. 012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789");
        _validator.validate(_command, _errors);
        assertTrue(_errors.hasFieldErrors("comments"));
    }

    public void testNoSchoolRating() throws Exception {
        _command.setOverallAsString("decline");
        _validator.validate(_command, _errors);
        assertTrue(_errors.hasFieldErrors("overallAsString"));
    }

    public void testPosterNotSpecified() throws Exception {
        _validator.validate(_command, _errors);
        assertTrue(_errors.hasFieldErrors("posterAsString"));
    }

    public void testHasNoErrors() throws Exception {
        _command.setComments("Correct comment with at least fifteen words Correct comment with at least fifteen words Correct comment with at least fifteen words");
        _command.setPosterAsString("principal");
        _command.setOverallAsString("1");
        _validator.validate(_command, _errors);
        assertFalse(_errors.hasErrors());
    }

}