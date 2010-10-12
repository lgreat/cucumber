package gs.web.school.review;

import gs.data.community.User;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ReviewService {

    IReviewDao _reviewDao;

    /**
     * Upgrades any provisional reviews for user to the associated non-provisional status (e.g. pd --> d)
     *
     * @param user
     * @return List of upgraded reviews
     */
    public List<Review> upgradeProvisionalReviews(User user) {

        List<Review> userReviews = _reviewDao.findUserReviews(user);

        List<Review> upgradedReviews = new ArrayList<Review>();

        Review anUpgradedReview = null;
        if (userReviews != null && userReviews.size() > 0) {
            for (Review review : userReviews) {
                String status = review.getStatus();
                if (StringUtils.length(status) > 1 && StringUtils.startsWith(status, "p")) {
                    review.setStatus(StringUtils.substring(status, 1));
                    _reviewDao.saveReview(review);
                    upgradedReviews.add(review);
                }
            }
        }
        return userReviews;
    }

    public IReviewDao getReviewDao() {
        return _reviewDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }
}
