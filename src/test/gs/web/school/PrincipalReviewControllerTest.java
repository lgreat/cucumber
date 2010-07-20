package gs.web.school;

import gs.data.community.*;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.SchoolDaoHibernate;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import gs.data.school.review.ReviewDaoHibernate;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.BaseTestCase;
import gs.web.school.review.ReviewCommand;
import org.easymock.internal.matchers.Any;
import org.springframework.validation.BindException;

import java.util.HashSet;
import java.util.Set;

import static org.easymock.EasyMock.*;

public class PrincipalReviewControllerTest extends BaseControllerTestCase {
    private IReviewDao _reviewDao;
    private ISchoolDao _schoolDao;
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private PrincipalReviewController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _userDao = createStrictMock(IUserDao.class);
        _reviewDao = createStrictMock(IReviewDao.class);
        _schoolDao = createStrictMock(ISchoolDao.class);
        _subscriptionDao = createStrictMock(ISubscriptionDao.class);
        _controller = new PrincipalReviewController();
        _controller.setReviewDao(_reviewDao);
        _controller.setSchoolDao(_schoolDao);
        _controller.setUserDao(_userDao);
        _controller.setSubscriptionDao(_subscriptionDao);
    }

    public void testUserExistsWithNoSubscriptions() throws Exception {
        getRequest().setParameter("state", "ca");
        getRequest().setParameter("id", "1");
        ReviewCommand reviewCommand = new ReviewCommand();
        reviewCommand.setComments("abc");
        reviewCommand.setEmail("ssprouse@greatschools.org");
        reviewCommand.setFirstName("samson");
        reviewCommand.setLastName("sprouse");
        BindException errors = new BindException(reviewCommand, "");

        School school = new School();
        school.setId(1);
        school.setDatabaseState(State.CA);

        User user = new User();
        user.setId(99999);
        user.setEmail(reviewCommand.getEmail());

        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(school);
        expect(_userDao.findUserFromEmailIfExists("ssprouse@greatschools.org")).andReturn(user);
        _userDao.updateUser(isA(User.class));
        _subscriptionDao.saveSubscription(isA(Subscription.class));
        _subscriptionDao.saveSubscription(isA(Subscription.class));
        _userDao.updateUser(isA(User.class));
        _reviewDao.saveReview(isA(Review.class));
        replay(_schoolDao);
        replay(_userDao);
        replay(_reviewDao);
        replay(_subscriptionDao);

        _controller.onSubmit(getRequest(), getResponse(), reviewCommand, errors);

        Set<Subscription> subscriptions = user.getSubscriptions();
        boolean hasRatingSubscription = false;
        boolean hasOneFreeSchoolSubscription = false;


        for (Subscription s : subscriptions) {
            if (SubscriptionProduct.RATING.equals(s.getProduct())) {
                hasRatingSubscription = true;
            } else if (SubscriptionProduct.ONE_FREE_SCHOOL.equals(s.getProduct())) {
                hasOneFreeSchoolSubscription = true;
            }
        }

        verify(_userDao);
        verify(_reviewDao);
        verify(_schoolDao);
        verify(_subscriptionDao);
        assertTrue("user should now have a subscription for Rating", hasRatingSubscription);
        assertTrue("user should now have a one free school subscription", hasOneFreeSchoolSubscription);
    }


    public void testUserExistsWithExistingSubscriptions() throws Exception {
        getRequest().setParameter("state", "ca");
        getRequest().setParameter("id", "1");
        ReviewCommand reviewCommand = new ReviewCommand();
        reviewCommand.setComments("abc");
        reviewCommand.setEmail("ssprouse@greatschools.org");
        reviewCommand.setFirstName("samson");
        reviewCommand.setLastName("sprouse");
        BindException errors = new BindException(reviewCommand, "");

        School school = new School();
        school.setId(1);
        school.setDatabaseState(State.CA);

        User user = new User();
        user.setId(99999);
        user.setEmail(reviewCommand.getEmail());

        Set<Subscription> subscriptions = new HashSet<Subscription>();
        Subscription subscription = new Subscription();
        subscription.setId(1);
        subscription.setUser(user);
        subscription.setProduct(SubscriptionProduct.ONE_FREE_SCHOOL);
        subscription.setState(school.getDatabaseState());
        subscription.setSchoolId(school.getId());
        subscriptions.add(subscription);
        user.setSubscriptions(subscriptions);

        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(school);
        expect(_userDao.findUserFromEmailIfExists("ssprouse@greatschools.org")).andReturn(user);
        _subscriptionDao.removeSubscription(isA(Integer.class));
        _userDao.updateUser(isA(User.class));
        _subscriptionDao.saveSubscription(isA(Subscription.class));
        _subscriptionDao.saveSubscription(isA(Subscription.class));
        _userDao.updateUser(isA(User.class));
        _reviewDao.saveReview(isA(Review.class));
        replay(_schoolDao);
        replay(_userDao);
        replay(_reviewDao);
        replay(_subscriptionDao);

        _controller.onSubmit(getRequest(), getResponse(), reviewCommand, errors);

        subscriptions = user.getSubscriptions();
        boolean hasRatingSubscription = false;
        boolean hasOneFreeSchoolSubscription = false;


        for (Subscription s : subscriptions) {
            if (SubscriptionProduct.RATING.equals(s.getProduct())) {
                hasRatingSubscription = true;
            } else if (SubscriptionProduct.ONE_FREE_SCHOOL.equals(s.getProduct())) {
                hasOneFreeSchoolSubscription = true;
            }
        }

        verify(_userDao);
        verify(_reviewDao);
        verify(_schoolDao);
        verify(_subscriptionDao);
        assertTrue("user should now have a subscription for Rating", hasRatingSubscription);
        assertTrue("user should now have a one free school subscription", hasOneFreeSchoolSubscription);
    }

    public void testNewUser() throws Exception {
        getRequest().setParameter("state", "ca");
        getRequest().setParameter("id", "1");
        ReviewCommand reviewCommand = new ReviewCommand();
        reviewCommand.setComments("abc");
        reviewCommand.setEmail("ssprouse@greatschools.org");
        reviewCommand.setFirstName("samson");
        reviewCommand.setLastName("sprouse");
        BindException errors = new BindException(reviewCommand, "");

        School school = new School();
        school.setId(1);
        school.setDatabaseState(State.CA);

        User user = new User();
        user.setId(99999);
        user.setEmail(reviewCommand.getEmail());

        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(school);
        expect(_userDao.findUserFromEmailIfExists("ssprouse@greatschools.org")).andReturn(null);
        _userDao.saveUser(isA(User.class));

        expect(_userDao.findUserFromEmail("ssprouse@greatschools.org")).andReturn(user);
        _subscriptionDao.saveSubscription(isA(Subscription.class));
        _subscriptionDao.saveSubscription(isA(Subscription.class));
        _userDao.updateUser(isA(User.class));
        _reviewDao.saveReview(isA(Review.class));
        replay(_schoolDao);
        replay(_userDao);
        replay(_reviewDao);
        replay(_subscriptionDao);

        _controller.onSubmit(getRequest(), getResponse(), reviewCommand, errors);

        Set<Subscription> subscriptions = user.getSubscriptions();
        boolean hasRatingSubscription = false;
        boolean hasOneFreeSchoolSubscription = false;

        for (Subscription s : subscriptions) {
            if (SubscriptionProduct.RATING.equals(s.getProduct())) {
                hasRatingSubscription = true;
            } else if (SubscriptionProduct.ONE_FREE_SCHOOL.equals(s.getProduct())) {
                hasOneFreeSchoolSubscription = true;
            }
        }

        verify(_userDao);
        verify(_reviewDao);
        verify(_schoolDao);
        verify(_subscriptionDao);
        assertTrue("user should now have a subscription for Rating", hasRatingSubscription);
        assertTrue("user should now have a one free school subscription", hasOneFreeSchoolSubscription);
    }

    public void testValidCookieExistsThrowsNoNPE() throws Exception {

        School school = new School();
        school.setId(378927894);
        school.setDatabaseState(State.CA);

        expect(_schoolDao.getPrincipalCredentials(school)).andReturn(null);
        replay(_schoolDao);

        boolean result = _controller.validCookieExists(getRequest(), school);

        verify(_schoolDao);
        assertFalse("validCookieExists should have returned false for invalid school", result);
    }


}
