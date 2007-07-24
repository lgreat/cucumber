package gs.web.school.review;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import static org.easymock.EasyMock.*;
import org.springframework.validation.BindException;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class ParentReviewFormControllerTest extends BaseControllerTestCase {
    ParentReviewFormController _controller;
    IReviewDao _reviewDao;
    IUserDao _userDao;
    User _user;
    School _school;
    ReviewCommand _command;
    BindException _errors;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new ParentReviewFormController();

        _reviewDao = createMock(IReviewDao.class);
        _userDao = createMock(IUserDao.class);

        _school = new School();
        _school.setDatabaseState(State.CA);
        _school.setId(1);

        _request.setAttribute("school", _school);

        _user = new User();
        _user.setEmail("dlee@greatschools.net");
        _user.setId(1);

        _command = new ReviewCommand();
        _command.setEmail(_user.getEmail());
        _errors = new BindException(_command, "");
    }

    public void testSubmitNewUser() throws Exception {
        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(null);
        _userDao.saveUser((User) anyObject());
        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(_user);
        replay(_userDao);

        _reviewDao.saveReview((Review) anyObject());
        replay(_reviewDao);

        _controller.setUserDao(_userDao);
        _controller.setReviewDao(_reviewDao);
        _controller.onSubmit(_request, _response, _command, _errors);
        verify(_userDao);
        verify(_reviewDao);
    }

    public void testSubmitExistingUser() throws Exception {
        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(_user);
        replay(_userDao);

        _reviewDao.removeReviews(_user, _school);
        _reviewDao.saveReview((Review) anyObject());
        replay(_reviewDao);

        _controller.setUserDao(_userDao);
        _controller.setReviewDao(_reviewDao);
        _controller.onSubmit(_request, _response, _command, _errors);
        verify(_userDao);
        verify(_reviewDao);
    }
}
