package gs.web.school.review;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.school.School;
import gs.data.school.review.CategoryRating;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.school.SchoolPageInterceptor;
import static org.easymock.EasyMock.*;
import org.springframework.validation.BindException;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class UpdateRatingsControllerTest extends BaseControllerTestCase {
    private UpdateRatingsController _controller;
    private IReviewDao _reviewDao;
    private IUserDao _userDao;
    private User _user;
    private School _school;
    private ReviewCommand _command;
    private BindException _errors;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new UpdateRatingsController();
        _reviewDao = createMock(IReviewDao.class);
        _userDao = createMock(IUserDao.class);
        _controller.setReviewDao(_reviewDao);
        _controller.setUserDao(_userDao);

        _command = new ReviewCommand();
        _command.setEmail("dlee@greatschools.net");
        _errors = new BindException(_command, "");
        _user = new User();

        _school = new School();
        _school.setId(1);
        _school.setDatabaseState(State.CA);

        getRequest().setAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE, _school);
    }

    public void testUpdateCategoryRatingsFromCommand() throws Exception {
        Review review = new Review();
        review.setId(2);

        review.setPrincipal(null);
        review.setTeachers(null);
        review.setActivities(null);
        review.setParents(null);
        review.setSafety(null);
        review.setQuality(null);

        _command.setPrincipal(CategoryRating.RATING_1);
        _command.setTeacher(CategoryRating.RATING_2);
        _command.setActivities(CategoryRating.RATING_3);
        _command.setParent(CategoryRating.RATING_4);
        _command.setSafety(CategoryRating.RATING_5);
        _command.setOverall(CategoryRating.DECLINE_TO_STATE);

        Review r = _controller.updateCategoryRatingsFromCommand(review, _command);

        assertEquals(CategoryRating.RATING_1, r.getPrincipal());
        assertEquals(CategoryRating.RATING_2, r.getTeachers());
        assertEquals(CategoryRating.RATING_3, r.getActivities());
        assertEquals(CategoryRating.RATING_4, r.getParents());
        assertEquals(CategoryRating.RATING_5, r.getSafety());
        assertEquals("method does not touch the overall quality ratings", null, r.getQuality());
    }

    public void testInvalidUser() throws Exception {
        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(null);
        replay(_userDao);
        _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verify(_userDao);
    }

    public void testInvalidReview() throws Exception {
        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(_user);
        replay(_userDao);

        expect(_reviewDao.findReview(_user, _school)).andReturn(null);
        replay(_reviewDao);
        _controller.onSubmit(getRequest(), getResponse(), _command, _errors);

        verify(_userDao);
        verify(_reviewDao);
    }

    public void testUpdateReview() throws Exception {
        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(_user);
        replay(_userDao);

        Review review = new Review();

        expect(_reviewDao.findReview(_user, _school)).andReturn(review);
        _reviewDao.saveReview(review);
        replay(_reviewDao);
        _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        
        verify(_userDao);
        verify(_reviewDao);
    }
}
