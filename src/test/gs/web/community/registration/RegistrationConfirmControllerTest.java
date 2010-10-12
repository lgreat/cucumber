package gs.web.community.registration;

import gs.data.community.WelcomeMessageStatus;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.util.DigestUtil;
import gs.web.school.review.ReviewService;
import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.servlet.ModelAndView;

import org.springframework.validation.BindException;
import java.util.*;

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.createStrictMock;

/**
 * Provides testing for RegistrationConfirmController
 *
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class RegistrationConfirmControllerTest extends BaseControllerTestCase {
    private RegistrationConfirmController _controller;

    private IUserDao _userDao;

    private IReviewDao _reviewDao;

    private ExactTargetAPI _exactTargetAPI;

    private BindException _errors;

    private ReviewService _reviewService;

    protected void setUp() throws Exception {
        super.setUp();
        _exactTargetAPI = createStrictMock(ExactTargetAPI.class);
        _reviewService = createStrictMock(ReviewService.class);
        _controller = new RegistrationConfirmController();

        _userDao = createStrictMock(IUserDao.class);
        _reviewDao = createStrictMock(IReviewDao.class);
        _controller.setUserDao(_userDao);
        _controller.setReviewDao(_reviewDao);
        _controller.setViewName("/room/with/a/view");
        _controller.setExactTargetAPI(_exactTargetAPI);
        _controller.setReviewService(_reviewService);

        Map<String,String> map = new HashMap<String,String>();
        MapBindingResult mapBindingResult = new MapBindingResult(map, "emailVerificationLink");
        _errors = new BindException(mapBindingResult);
    }

    public void testBasics() {
        assertSame(_userDao, _controller.getUserDao());
        assertEquals("/room/with/a/view", _controller.getViewName());
    }

    public void testRegistrationConfirm() throws Exception {
        // 1) create user record with non-validated password
        User user = new User();
        user.setEmail("aroy@greatschools.org");
        user.setId(234);
        user.setPlaintextPassword("foobar");
        user.setEmailProvisional("foobar");

        assertTrue("Expect user to be provisional", user.isEmailProvisional());
        assertFalse("Expect user to be provisional", user.isEmailValidated());

        Date now = new Date();
        long dateSent = now.getTime() - 5000; // sent 5 seconds ago
        getRequest().setParameter("date", String.valueOf(dateSent));

        String actualHash = DigestUtil.hashStringInt(user.getEmail(), 234);
        actualHash = DigestUtil.hashString(actualHash + String.valueOf(dateSent));

        EmailVerificationLinkCommand emailVerificationLinkCommand = new EmailVerificationLinkCommand();
        emailVerificationLinkCommand.setHashPlusUserId(actualHash + "234");
        emailVerificationLinkCommand.setUser(user);

        _userDao.saveUser(user);

        replayMocks(_userDao);
        ModelAndView mAndV = _controller.handle(getRequest(), getResponse(), emailVerificationLinkCommand, _errors);
        verifyMocks(_userDao);

        assertNotNull(mAndV);
        assertEquals("redirect:/account/", mAndV.getViewName());
        assertTrue("Expect user to be validated", user.isEmailValidated());
    }


    public void testReviewsUpgraded() throws Exception {
        // 1) create user record with non-validated password
        User user = new User();
        user.setEmail("ssprouse@greatschools.org");
        user.setId(234);
        user.setPlaintextPassword("foobar");
        user.setEmailProvisional("foobar");

        School school = new School();
        school.setDatabaseState(State.CA);
        school.setId(1);

        Review review1 = new Review();
        review1.setStatus("p");
        review1.setSchool(school);

        Review review2 = new Review();
        review2.setStatus("u");
        review2.setSchool(school);

        List<Review> updatedReviews = new ArrayList<Review>();
        updatedReviews.add(review1);
        updatedReviews.add(review2);

        Date now = new Date();
        long dateSent = now.getTime() - 5000; // sent 5 seconds ago

        String actualHash = DigestUtil.hashStringInt(user.getEmail(), 234);
        actualHash = DigestUtil.hashString(actualHash + String.valueOf(dateSent));

        EmailVerificationLinkCommand emailVerificationLinkCommand = new EmailVerificationLinkCommand();
        emailVerificationLinkCommand.setHashPlusUserId(actualHash + "234");
        emailVerificationLinkCommand.setUser(user);
        emailVerificationLinkCommand.setDate(String.valueOf(dateSent));

        expect(_reviewService.upgradeProvisionalReviews(user)).andReturn(updatedReviews);
        _userDao.saveUser(user);

        replayAllMocks();
        ModelAndView mAndV = _controller.handle(getRequest(), getResponse(), emailVerificationLinkCommand, _errors);
        verifyAllMocks();

        assertNotNull(mAndV);
    }

    public void testWelcomeEmailUponEmailConfirmation() throws Exception {
        // 1) create user record with non-validated password
        User user = new User();
        user.setEmail("ssprouse@greatschools.org");
        user.setId(234);
        user.setPlaintextPassword("foobar");
        user.setEmailProvisional("foobar");

        School school = new School();
        school.setId(1);
        school.setDatabaseState(State.CA);

        List<Review> updatedReviews = new ArrayList<Review>();
        Review review1 = new Review();
        review1.setStatus("p");
        review1.setSchool(school);

        updatedReviews.add(review1);

        Date now = new Date();
        long dateSent = now.getTime() - 5000; // sent 5 seconds ago

        String actualHash = DigestUtil.hashStringInt(user.getEmail(), 234);
        actualHash = DigestUtil.hashString(actualHash + String.valueOf(dateSent));

        EmailVerificationLinkCommand emailVerificationLinkCommand = new EmailVerificationLinkCommand();
        emailVerificationLinkCommand.setHashPlusUserId(actualHash + "234");
        emailVerificationLinkCommand.setUser(user);
        emailVerificationLinkCommand.setDate(String.valueOf(dateSent));

        expect(_reviewService.upgradeProvisionalReviews(user)).andReturn(updatedReviews);

        User user2 = new User();
        user2.setWelcomeMessageStatus(WelcomeMessageStatus.NEVER_SEND);
        _userDao.saveUser(UserWelcomeMessageStatusMatcher.eqUser(user2));

        replayAllMocks();
        ModelAndView mAndV = _controller.handle(getRequest(), getResponse(), emailVerificationLinkCommand, _errors);
        verifyAllMocks();

        assertNotNull(mAndV);
    }

    private void setupForRedirect(User user) {
        user.setEmailProvisional("foobar");
        expect(_reviewService.upgradeProvisionalReviews(user)).andReturn(null);
        _userDao.saveUser(user);
    }

    public void testRedirectParam() throws Exception {
        User user = new User();
        user.setEmail("aroy@greatschools.org");
        user.setId(234);
        user.setPlaintextPassword("foobar");
        Date now = new Date();
        long dateSent = now.getTime() - 5000; // sent 5 seconds ago
        String actualHash = DigestUtil.hashStringInt(user.getEmail(), 234);
        actualHash = DigestUtil.hashString(actualHash + String.valueOf(dateSent));

        EmailVerificationLinkCommand emailVerificationLinkCommand = new EmailVerificationLinkCommand();
        emailVerificationLinkCommand.setHashPlusUserId(actualHash + "234");
        emailVerificationLinkCommand.setUser(user);
        emailVerificationLinkCommand.setDate(String.valueOf(dateSent));
        ModelAndView mAndV;

        getRequest().setParameter("redirect", "/path");
        setupForRedirect(user);
        replayAllMocks();
        mAndV = _controller.handle(getRequest(), getResponse(), emailVerificationLinkCommand, _errors);
        verifyAllMocks();
        assertEquals("redirect:/path", mAndV.getViewName());

        resetAllMocks();

        getRequest().setParameter("redirect", "/path?foo=bar");
        setupForRedirect(user);
        replayAllMocks();
        mAndV = _controller.handle(getRequest(), getResponse(), emailVerificationLinkCommand, _errors);
        verifyAllMocks();
        assertEquals("redirect:/path?foo=bar", mAndV.getViewName());

        resetAllMocks();

        getRequest().setParameter("redirect", "/path?foo=bar#anchor");
        setupForRedirect(user);
        replayAllMocks();
        mAndV = _controller.handle(getRequest(), getResponse(), emailVerificationLinkCommand, _errors);
        verifyAllMocks();
        assertEquals("redirect:/path?foo=bar#anchor", mAndV.getViewName());
    }

    public void testFindCorrectUpgradedReview() throws Exception {
        Review review = new Review();
        review.setStatus("u");

        Review review2 = new Review();
        review2.setStatus("p");

        Review review3 = new Review();
        review3.setStatus("d");

        List<Review> reviews = new ArrayList<Review>();

        reviews.add(review);
        reviews.add(review2);
        reviews.add(review3);

        Review upgradedReview = _controller.findCorrectUpgradedReview(reviews);

        assertTrue(upgradedReview == review2);
    }

    public void testFindPostedReviews() {
        Review review = new Review();
        review.setStatus("u");

        Review review2 = new Review();
        review2.setStatus("p");

        Review review3 = new Review();
        review3.setStatus("d");

        List<Review> reviews = new ArrayList<Review>();

        reviews.add(review);
        reviews.add(review2);
        reviews.add(review3);

        List<Review> postedReviews = _controller.findPostedReviews(reviews);

        assertTrue(postedReviews.size() == 1);
        assertTrue(postedReviews.contains(review2));
    }

    public void replayAllMocks() {
        replayMocks(_userDao, _reviewDao, _reviewService);
    }

    public void resetAllMocks() {
        resetMocks(_userDao, _reviewDao, _reviewService);
    }

    public void verifyAllMocks() {
        verifyMocks(_userDao, _reviewDao, _reviewService);
    }
}

class UserWelcomeMessageStatusMatcher implements IArgumentMatcher {

    private User expected;

    UserWelcomeMessageStatusMatcher(User user) {
        expected = user;
    }

    public static final User eqUser(User user) {
        EasyMock.reportMatcher(new UserWelcomeMessageStatusMatcher(user));
        return null;
    }

    public void appendTo(StringBuffer b) {
        b.append("eqUser(").append(expected).append(")");
    }

    public boolean matches(Object arg0) {
        if (!(arg0 instanceof User)) {
            return false;
        }

        User actual = (User) arg0;

        WelcomeMessageStatus status = expected.getWelcomeMessageStatus();
        if (status == null && actual.getWelcomeMessageStatus() != null) {
            return false;
        } else if (!status.equals(actual.getWelcomeMessageStatus())) {
            return false;
        }

        return true;
    }


}