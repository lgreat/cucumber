package gs.web.school.review;

import gs.data.community.User;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.ITopicalSchoolReviewDao;
import gs.data.school.review.Review;
import gs.data.school.review.TopicalSchoolReview;
import gs.web.BaseTestCase;
import org.apache.commons.lang.StringUtils;
import org.easymock.IArgumentMatcher;

import java.util.ArrayList;
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

        expect(_topicalSchoolReviewDao.findByMemberId(user.getId())).andReturn(new ArrayList<TopicalSchoolReview>());
        replay(_topicalSchoolReviewDao);

        assertNull("Test review process date should be null", review1.getProcessDate());

        List<Review> upgradedReviews = _reviewService.upgradeProvisionalReviews(user);
        
        verify(_reviewDao);
        verify(_topicalSchoolReviewDao);

        assertNotNull("Test review process date should have been set", review1.getProcessDate());
        assertEquals("Review status should have been upgraded.", "u", review1.getStatus());
        assertEquals("Review status should have been upgraded.", "d", review2.getStatus());
        assertEquals("Review status should have been upgraded.", "p", review3.getStatus());
        assertEquals("Review status should not have changed.", "u", review4.getStatus());
        assertEquals("Review status should not have changed.", "p", review5.getStatus());

        assertTrue("Upgraded reviews list should have contained review:", upgradedReviews.contains(review1));
        assertTrue("Upgraded reviews list should have contained review:", upgradedReviews.contains(review2));
        assertTrue("Upgraded reviews list should have contained review:", upgradedReviews.contains(review3));
        assertTrue(upgradedReviews.size() == 3);
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

    public void testUpgradeProvisionalTopicalReviews() {
        User user = new User();
        user.setId(99999);

        expect(_reviewDao.findUserReviews(user)).andReturn(new ArrayList<Review>());
        List<TopicalSchoolReview> topicalReviews = new ArrayList<TopicalSchoolReview>();
        // test all the various statuses
        topicalReviews.add(createTopicalReview(user, "pp", 1));
        topicalReviews.add(createTopicalReview(user, "pd", 2));
        topicalReviews.add(createTopicalReview(user, "ph", 3));
        topicalReviews.add(createTopicalReview(user, "pu", 4));
        topicalReviews.add(createTopicalReview(user, "p", 5));
        topicalReviews.add(createTopicalReview(user, "d", 6));
        topicalReviews.add(createTopicalReview(user, "h", 7));
        topicalReviews.add(createTopicalReview(user, "u", 8));

        expect(_topicalSchoolReviewDao.findByMemberId(user.getId())).andReturn(topicalReviews);
        // expect only the provisional ones to be modified to their non-provisional versions and saved
        _topicalSchoolReviewDao.save(reviewWithStatusAndIdEq("p", 1));
        _topicalSchoolReviewDao.save(reviewWithStatusAndIdEq("d", 2));
        _topicalSchoolReviewDao.save(reviewWithStatusAndIdEq("h", 3));
        _topicalSchoolReviewDao.save(reviewWithStatusAndIdEq("u", 4));

        replay(_reviewDao);
        replay(_topicalSchoolReviewDao);
        _reviewService.upgradeProvisionalReviewsAndSummarize(user);
        verify(_reviewDao);
        verify(_topicalSchoolReviewDao);
    }

    private TopicalSchoolReview createTopicalReview(User user, String status, Integer id) {
        TopicalSchoolReview rval = new TopicalSchoolReview();
        rval.setId(id.longValue());
        rval.setStatus(status);
        rval.setUser(user);
        return rval;
    }

    private static TopicalSchoolReview reviewWithStatusAndIdEq(String status, Integer id) {
        reportMatcher(new TopicalSchoolReviewMatcher(status, id));
        return null;
    }

    private static class TopicalSchoolReviewMatcher implements IArgumentMatcher {
        private String _expectedStatus;
        private String _actualStatus;
        private Long _expectedId;
        private Long _actualId;

        public TopicalSchoolReviewMatcher(String status, Integer id) {
            _expectedStatus = status;
            _expectedId = id.longValue();
        }

        public boolean matches(Object argument) {
            if (!(argument instanceof TopicalSchoolReview)) {
                return false;
            }
            TopicalSchoolReview actualReview = (TopicalSchoolReview) argument;
            _actualStatus = actualReview.getStatus();
            _actualId = actualReview.getId();
            return StringUtils.equals(_expectedStatus, actualReview.getStatus()) && _expectedId.equals(actualReview.getId());
        }

        public void appendTo(StringBuffer buffer) {
            buffer.append("reviewWithStatusEq(");
            buffer.append("expected TopicalSchoolReview with status \"").append(_expectedStatus).append("\" ");
            buffer.append("and id ").append(_expectedId).append(", ");
            buffer.append("instead got status \"").append(_actualStatus).append("\" and id ").append(_actualId);
            buffer.append(")");
        }
    }
}
