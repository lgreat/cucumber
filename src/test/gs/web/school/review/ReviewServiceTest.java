package gs.web.school.review;

import gs.data.community.User;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import gs.web.BaseTestCase;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.*;

public class ReviewServiceTest extends BaseTestCase {

    IReviewDao _reviewDao = createStrictMock(IReviewDao.class);

    ReviewService _reviewService = new ReviewService();

    public void setUp() {
        _reviewService.setReviewDao(_reviewDao);
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

        assertNull("Test review process date should be null", review1.getProcessDate());

        List<Review> upgradedReviews = _reviewService.upgradeProvisionalReviews(user);
        
        verify(_reviewDao);

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
}
