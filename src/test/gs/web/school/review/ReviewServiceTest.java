package gs.web.school.review;

import gs.data.community.User;
import gs.data.school.review.*;
import gs.web.BaseTestCase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.easymock.EasyMock.*;

public class ReviewServiceTest extends BaseTestCase {

    IReviewDao _reviewDao = createStrictMock(IReviewDao.class);
    ITopicalSchoolReviewDao _topicalSchoolReviewDao = createStrictMock(ITopicalSchoolReviewDao.class);

    ReviewService _reviewService = new ReviewService();

    public void setUp() {
        _reviewService.setReviewDao(_reviewDao);
        _reviewService.setTopicalSchoolReviewDao(_topicalSchoolReviewDao);
    }

    public void testUpgradeProvisionalReviews() throws Exception {
        User user = new User();
        user.setId(99999);

        Review review1 = new Review();
        review1.setStatus("pu");
        review1.setUser(user);

        Review review2 = new Review();
        review2.setStatus("pd");
        review2.setUser(user);

        Review review3 = new Review();
        review3.setStatus("pp");
        review3.setUser(user);

        Review review4 = new Review();
        review4.setStatus("u");
        review4.setUser(user);

        Review review5 = new Review();
        review5.setStatus("p");
        review5.setUser(user);

        List<Review> reviews = new ArrayList<Review>();
        reviews.add(review1);
        reviews.add(review2);
        reviews.add(review3);
        reviews.add(review4);
        reviews.add(review5);

        expect(_reviewDao.findUserReviews(user)).andReturn(reviews);
        _reviewDao.saveReview(isA(Review.class));
        _reviewDao.saveReview(isA(Review.class));
        _reviewDao.saveReview(isA(Review.class));

        replay(_reviewDao);

        TopicalSchoolReview topicalReview1 = createTopicalReview(user, "pu", 1);
        TopicalSchoolReview topicalReview2 = createTopicalReview(user, "pd", 3);
        TopicalSchoolReview topicalReview3 = createTopicalReview(user, "pp", 3);
        TopicalSchoolReview topicalReview4 = createTopicalReview(user, "u", 4);
        TopicalSchoolReview topicalReview5 = createTopicalReview(user, "p", 5);
        TopicalSchoolReview topicalReview6 = createTopicalReview(user, "d", 6);

        List<TopicalSchoolReview> topicalReviews = new ArrayList<TopicalSchoolReview>(6);
        topicalReviews.add(topicalReview1);
        topicalReviews.add(topicalReview2);
        topicalReviews.add(topicalReview3);
        topicalReviews.add(topicalReview4);
        topicalReviews.add(topicalReview5);
        topicalReviews.add(topicalReview6);
        expect(_topicalSchoolReviewDao.findByMemberId(user.getId())).andReturn(topicalReviews);
        _topicalSchoolReviewDao.save(topicalReview1);
        _topicalSchoolReviewDao.save(topicalReview2);
        _topicalSchoolReviewDao.save(topicalReview3);
        replay(_topicalSchoolReviewDao);

        assertNull("Test review process date should be null", review1.getProcessDate());
        assertNull("Test review process date should be null", topicalReview1.getProcessDate());

        List<ISchoolReview> upgradedReviews = _reviewService.upgradeProvisionalReviews(user);
        
        verify(_reviewDao);
        verify(_topicalSchoolReviewDao);

        assertNotNull("Test review process date should have been set", review1.getProcessDate());
        assertEquals("Review status should have been upgraded.", "u", review1.getStatus());
        assertEquals("Review status should have been upgraded.", "d", review2.getStatus());
        assertEquals("Review status should have been upgraded.", "p", review3.getStatus());
        assertEquals("Review status should not have changed.", "u", review4.getStatus());
        assertEquals("Review status should not have changed.", "p", review5.getStatus());

        assertNotNull("Test review process date should have been set", topicalReview1.getProcessDate());
        assertEquals("Review status should have been upgraded.", "u", topicalReview1.getStatus());
        assertEquals("Review status should have been upgraded.", "d", topicalReview2.getStatus());
        assertEquals("Review status should have been upgraded.", "p", topicalReview3.getStatus());
        assertEquals("Review status should not have changed.", "u", topicalReview4.getStatus());
        assertEquals("Review status should not have changed.", "p", topicalReview5.getStatus());
        assertEquals("Review status should not have changed.", "d", topicalReview6.getStatus());

        assertTrue("Upgraded reviews list should have contained review:", upgradedReviews.contains(review1));
        assertTrue("Upgraded reviews list should have contained review:", upgradedReviews.contains(review2));
        assertTrue("Upgraded reviews list should have contained review:", upgradedReviews.contains(review3));
        assertTrue("Upgraded reviews list should have contained review:", upgradedReviews.contains(topicalReview1));
        assertTrue("Upgraded reviews list should have contained review:", upgradedReviews.contains(topicalReview2));
        assertTrue("Upgraded reviews list should have contained review:", upgradedReviews.contains(topicalReview3));
        assertEquals("Expect 6 upgraded reviews", 6, upgradedReviews.size());
    }

    public void testUpgradeProvisionalReviewsWithHeldStatus() throws Exception {
        User user = new User();
        user.setId(99999);

        Review review1 = new Review();
        review1.setStatus("h");
        review1.setUser(user);

        Review review2 = new Review();
        review2.setStatus("ph");
        review2.setUser(user);

        List<Review> reviews = new ArrayList<Review>();
        reviews.add(review1);
        reviews.add(review2);

        expect(_reviewDao.findUserReviews(user)).andReturn(reviews);
        _reviewDao.saveReview(isA(Review.class));

        replay(_reviewDao);

        expect(_topicalSchoolReviewDao.findByMemberId(user.getId())).andReturn(new ArrayList<TopicalSchoolReview>());
        replay(_topicalSchoolReviewDao);

        try {
            _reviewService.upgradeProvisionalReviewsAndSummarize(user);
        } catch (IllegalArgumentException e) {
            fail("Should not have thrown exception: " + e.getMessage());
        }

        verify(_reviewDao);
        verify(_topicalSchoolReviewDao);
    }

    public void testSummaryGetFirstPublishedReview_regularReview() {
        User user = new User();
        user.setId(99999);

        Review review1 = new Review();
        review1.setStatus("pu");
        review1.setUser(user);
        review1.setPosted(getDateXDaysAgo(1));

        Review review2 = new Review();
        review2.setStatus("pd");
        review2.setUser(user);
        review2.setPosted(getDateXDaysAgo(1));

        Review review3 = new Review();
        review3.setStatus("pp");
        review3.setUser(user);
        review3.setPosted(getDateXDaysAgo(2));

        Review review4 = new Review();
        review4.setStatus("u");
        review4.setUser(user);
        review4.setPosted(getDateXDaysAgo(1));

        Review review5 = new Review();
        review5.setStatus("p");
        review5.setUser(user);
        review5.setPosted(getDateXDaysAgo(1));

        List<Review> reviews = new ArrayList<Review>();
        reviews.add(review1);
        reviews.add(review2);
        reviews.add(review3);
        reviews.add(review4);
        reviews.add(review5);

        expect(_reviewDao.findUserReviews(user)).andReturn(reviews);
        _reviewDao.saveReview(isA(Review.class));
        _reviewDao.saveReview(isA(Review.class));
        _reviewDao.saveReview(isA(Review.class));

        replay(_reviewDao);

        TopicalSchoolReview topicalReview1 = createTopicalReview(user, "pu", 1, getDateXDaysAgo(1));
        TopicalSchoolReview topicalReview2 = createTopicalReview(user, "pd", 3, getDateXDaysAgo(1));
        TopicalSchoolReview topicalReview3 = createTopicalReview(user, "pp", 3, getDateXDaysAgo(3));
        TopicalSchoolReview topicalReview4 = createTopicalReview(user, "u", 4, getDateXDaysAgo(1));
        TopicalSchoolReview topicalReview5 = createTopicalReview(user, "p", 5, getDateXDaysAgo(1));
        TopicalSchoolReview topicalReview6 = createTopicalReview(user, "d", 6, getDateXDaysAgo(1));

        List<TopicalSchoolReview> topicalReviews = new ArrayList<TopicalSchoolReview>(6);
        topicalReviews.add(topicalReview1);
        topicalReviews.add(topicalReview2);
        topicalReviews.add(topicalReview3);
        topicalReviews.add(topicalReview4);
        topicalReviews.add(topicalReview5);
        topicalReviews.add(topicalReview6);
        expect(_topicalSchoolReviewDao.findByMemberId(user.getId())).andReturn(topicalReviews);
        _topicalSchoolReviewDao.save(topicalReview1);
        _topicalSchoolReviewDao.save(topicalReview2);
        _topicalSchoolReviewDao.save(topicalReview3);
        replay(_topicalSchoolReviewDao);

        ReviewService.ReviewUpgradeSummary summary = _reviewService.upgradeProvisionalReviewsAndSummarize(user);
        verify(_reviewDao);
        verify(_topicalSchoolReviewDao);

        assertNotNull(summary);
        assertNotNull(summary.getUpgradedReviews());
        assertEquals(6, summary.getUpgradedReviews().size());
        assertEquals(ReviewService.ReviewUpgradeStatus.REVIEW_UPGRADED_PUBLISHED, summary.getStatus());
        assertNotNull(summary.getFirstPublishedReview());
        assertSame(review3, summary.getFirstPublishedReview());
    }

    public void testSummaryGetFirstPublishedReview_topicalReview() {
        User user = new User();
        user.setId(99999);

        Review review1 = new Review();
        review1.setStatus("pu");
        review1.setUser(user);
        review1.setPosted(getDateXDaysAgo(1));

        Review review2 = new Review();
        review2.setStatus("pd");
        review2.setUser(user);
        review2.setPosted(getDateXDaysAgo(1));

        Review review3 = new Review();
        review3.setStatus("pp");
        review3.setUser(user);
        review3.setPosted(getDateXDaysAgo(3));

        Review review4 = new Review();
        review4.setStatus("u");
        review4.setUser(user);
        review4.setPosted(getDateXDaysAgo(1));

        Review review5 = new Review();
        review5.setStatus("p");
        review5.setUser(user);
        review5.setPosted(getDateXDaysAgo(1));

        List<Review> reviews = new ArrayList<Review>();
        reviews.add(review1);
        reviews.add(review2);
        reviews.add(review3);
        reviews.add(review4);
        reviews.add(review5);

        expect(_reviewDao.findUserReviews(user)).andReturn(reviews);
        _reviewDao.saveReview(isA(Review.class));
        _reviewDao.saveReview(isA(Review.class));
        _reviewDao.saveReview(isA(Review.class));

        replay(_reviewDao);

        TopicalSchoolReview topicalReview1 = createTopicalReview(user, "pu", 1, getDateXDaysAgo(1));
        TopicalSchoolReview topicalReview2 = createTopicalReview(user, "pd", 3, getDateXDaysAgo(1));
        TopicalSchoolReview topicalReview3 = createTopicalReview(user, "pp", 3, getDateXDaysAgo(2));
        TopicalSchoolReview topicalReview4 = createTopicalReview(user, "u", 4, getDateXDaysAgo(1));
        TopicalSchoolReview topicalReview5 = createTopicalReview(user, "p", 5, getDateXDaysAgo(1));
        TopicalSchoolReview topicalReview6 = createTopicalReview(user, "d", 6, getDateXDaysAgo(1));

        List<TopicalSchoolReview> topicalReviews = new ArrayList<TopicalSchoolReview>(6);
        topicalReviews.add(topicalReview1);
        topicalReviews.add(topicalReview2);
        topicalReviews.add(topicalReview3);
        topicalReviews.add(topicalReview4);
        topicalReviews.add(topicalReview5);
        topicalReviews.add(topicalReview6);
        expect(_topicalSchoolReviewDao.findByMemberId(user.getId())).andReturn(topicalReviews);
        _topicalSchoolReviewDao.save(topicalReview1);
        _topicalSchoolReviewDao.save(topicalReview2);
        _topicalSchoolReviewDao.save(topicalReview3);
        replay(_topicalSchoolReviewDao);

        ReviewService.ReviewUpgradeSummary summary = _reviewService.upgradeProvisionalReviewsAndSummarize(user);
        verify(_reviewDao);
        verify(_topicalSchoolReviewDao);

        assertNotNull(summary);
        assertNotNull(summary.getUpgradedReviews());
        assertEquals(6, summary.getUpgradedReviews().size());
        assertEquals(ReviewService.ReviewUpgradeStatus.TOPICAL_REVIEW_UPGRADED_PUBLISHED, summary.getStatus());
        assertNotNull(summary.getFirstPublishedReview());
        assertSame(topicalReview3, summary.getFirstPublishedReview());
    }

    private static Date getDateXDaysAgo(int x) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -x);
        return cal.getTime();
    }

    private TopicalSchoolReview createTopicalReview(User user, String status, Integer id) {
        TopicalSchoolReview rval = new TopicalSchoolReview();
        rval.setId(id);
        rval.setStatus(status);
        rval.setUser(user);
        return rval;
    }

    private TopicalSchoolReview createTopicalReview(User user, String status, Integer id, Date posted) {
        TopicalSchoolReview rval = new TopicalSchoolReview();
        rval.setId(id);
        rval.setStatus(status);
        rval.setUser(user);
        rval.setCreated(posted);
        rval.setTopic(new ReviewTopic());
        return rval;
    }

}