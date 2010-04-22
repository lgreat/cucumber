package gs.web.community.registration;

import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import gs.web.BaseControllerTestCase;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.util.DigestUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.easymock.EasyMock.*;

/**
 * Provides testing for RegistrationConfirmController
 *
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class RegistrationConfirmControllerTest extends BaseControllerTestCase {
    private RegistrationConfirmController _controller;

    private IUserDao _userDao;

    private IReviewDao _reviewDao;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new RegistrationConfirmController();

        _userDao = createStrictMock(IUserDao.class);
        _reviewDao = createStrictMock(IReviewDao.class);
        _controller.setUserDao(_userDao);
        _controller.setReviewDao(_reviewDao);
        _controller.setViewName("/room/with/a/view");
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

        reviews.add(review1);
        reviews.add(review2);

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

        List<Review> reviews = null;

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
        verifyMocks(_userDao,_reviewDao);
    }
}
