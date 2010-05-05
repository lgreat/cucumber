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
import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    protected void setUp() throws Exception {
        super.setUp();
        _exactTargetAPI = createStrictMock(ExactTargetAPI.class);
        _controller = new RegistrationConfirmController();

        _userDao = createStrictMock(IUserDao.class);
        _reviewDao = createStrictMock(IReviewDao.class);
        _controller.setUserDao(_userDao);
        _controller.setReviewDao(_reviewDao);
        _controller.setViewName("/room/with/a/view");
        _controller.setExactTargetAPI(_exactTargetAPI);
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

        getRequest().setParameter("id", actualHash + "234");

        expect(_userDao.findUserFromId(234)).andReturn(user);
        _userDao.saveUser(user);

        replayMocks(_userDao);
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
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

        List<Review> reviews = new ArrayList<Review>();
        Review review1 = new Review();
        review1.setStatus("pp");

        Review review2 = new Review();
        review2.setStatus("pu");

        Review review3 = new Review();
        review3.setStatus("p");

        reviews.add(review1);
        reviews.add(review2);
        reviews.add(review3);

        Date now = new Date();
        long dateSent = now.getTime() - 5000; // sent 5 seconds ago
        getRequest().setParameter("date", String.valueOf(dateSent));

        String actualHash = DigestUtil.hashStringInt(user.getEmail(), 234);
        actualHash = DigestUtil.hashString(actualHash + String.valueOf(dateSent));

        getRequest().setParameter("id", actualHash + "234");

        expect(_userDao.findUserFromId(234)).andReturn(user);
        _userDao.saveUser(user);
        expect(_reviewDao.findUserReviews(user)).andReturn(reviews);

        _reviewDao.saveReview(review1);
        _reviewDao.saveReview(review2);

        replayAllMocks();
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
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

        List<Review> reviews = new ArrayList<Review>();
        Review review1 = new Review();
        review1.setStatus("pp");
        review1.setSchool(school);


        Review review2 = new Review();
        review2.setStatus("pu");
        review2.setSchool(school);

        Review review3 = new Review();
        review3.setStatus("p");
        review3.setSchool(school);

        reviews.add(review1);
        reviews.add(review2);
        reviews.add(review3);

        Date now = new Date();
        long dateSent = now.getTime() - 5000; // sent 5 seconds ago
        getRequest().setParameter("date", String.valueOf(dateSent));


        String actualHash = DigestUtil.hashStringInt(user.getEmail(), 234);
        actualHash = DigestUtil.hashString(actualHash + String.valueOf(dateSent));

        getRequest().setParameter("id", actualHash + "234");

        expect(_userDao.findUserFromId(234)).andReturn(user);
        expect(_reviewDao.findUserReviews(user)).andReturn(reviews);

        User user2 = new User();
        user2.setWelcomeMessageStatus(WelcomeMessageStatus.NEVER_SEND);
        _userDao.saveUser(UserWelcomeMessageStatusMatcher.eqUser(user2));

        _reviewDao.saveReview(review1);
        _reviewDao.saveReview(review2);

        replayAllMocks();
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();

        assertNotNull(mAndV);
    }

    private void setupForRedirect(User user) {
        user.setEmailProvisional("foobar");
        expect(_userDao.findUserFromId(234)).andReturn(user);
        _userDao.saveUser(user);
    }

    public void testRedirectParam() throws Exception {
        User user = new User();
        user.setEmail("aroy@greatschools.org");
        user.setId(234);
        user.setPlaintextPassword("foobar");
        Date now = new Date();
        long dateSent = now.getTime() - 5000; // sent 5 seconds ago
        getRequest().setParameter("date", String.valueOf(dateSent));
        String actualHash = DigestUtil.hashStringInt(user.getEmail(), 234);
        actualHash = DigestUtil.hashString(actualHash + String.valueOf(dateSent));
        getRequest().setParameter("id", actualHash + "234");
        ModelAndView mAndV;

        getRequest().setParameter("redirect", "/path");
        setupForRedirect(user);
        expect(_reviewDao.findUserReviews(user)).andReturn(null);
        replayAllMocks();
        mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();
        assertEquals("redirect:/path", mAndV.getViewName());

        resetAllMocks();

        getRequest().setParameter("redirect", "/path?foo=bar");
        setupForRedirect(user);
        expect(_reviewDao.findUserReviews(user)).andReturn(null);
        replayAllMocks();
        mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();
        assertEquals("redirect:/path?foo=bar", mAndV.getViewName());

        resetAllMocks();

        getRequest().setParameter("redirect", "/path?foo=bar#anchor");
        setupForRedirect(user);
        expect(_reviewDao.findUserReviews(user)).andReturn(null);
        replayAllMocks();
        mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();
        assertEquals("redirect:/path?foo=bar#anchor", mAndV.getViewName());
    }

    public void replayAllMocks() {
        replayMocks(_userDao, _reviewDao);
    }

    public void resetAllMocks() {
        resetMocks(_userDao, _reviewDao);
    }

    public void verifyAllMocks() {
        verifyMocks(_userDao, _reviewDao);
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