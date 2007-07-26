package gs.web.school.review;

import gs.data.community.*;
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
public class AddParentReviewsControllerTest extends BaseControllerTestCase {
    AddParentReviewsController _controller;
    IReviewDao _reviewDao;
    IUserDao _userDao;
    ISubscriptionDao _subscriptionDao;
    User _user;
    School _school;
    ReviewCommand _command;
    BindException _errors;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new AddParentReviewsController();

        _reviewDao = createMock(IReviewDao.class);
        _userDao = createMock(IUserDao.class);
        _subscriptionDao = createMock(ISubscriptionDao.class);

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

        //new user so we add an entry into list member that indicates where we got their email from
        Subscription sub = new Subscription(_user, SubscriptionProduct.RATING, _school.getDatabaseState());
        _subscriptionDao.saveSubscription(sub);
        replay(_subscriptionDao);

        _controller.setUserDao(_userDao);
        _controller.setReviewDao(_reviewDao);
        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.onSubmit(_request, _response, _command, _errors);
        verify(_userDao);
        verify(_reviewDao);
        verify(_subscriptionDao);
    }

    public void testSubmitExistingUser() throws Exception {
        expect(_userDao.findUserFromEmailIfExists(_command.getEmail())).andReturn(_user);
        replay(_userDao);

        _reviewDao.removeReviews(_user, _school);
        _reviewDao.saveReview((Review) anyObject());
        replay(_reviewDao);

        replay(_subscriptionDao);
        _controller.setUserDao(_userDao);
        _controller.setReviewDao(_reviewDao);
        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.onSubmit(_request, _response, _command, _errors);
        verify(_userDao);
        verify(_reviewDao);
        verify(_subscriptionDao);
    }

    public void testErrorJson() throws Exception {
        _errors.reject("bad","this is a bad");
        _errors.reject("bad", "this is really bad");
        _controller.errorJSON(getResponse(), _errors);

        assertEquals("text/x-json", getResponse().getContentType());
        //{"status":true,"errors":["this is a bad","this is really bad"]}
        assertEquals("{\"status\":false,\"errors\":[\"this is a bad\",\"this is really bad\"]}", getResponse().getContentAsString());
    }

    public void testSuccessJson() throws Exception {
        _controller.successJSON(getResponse());
        assertEquals("text/x-json", getResponse().getContentType());
        //{"status":true,"errors":["this is a bad","this is really bad"]}
        assertEquals("{\"status\":true}", getResponse().getContentAsString());
    }
}
