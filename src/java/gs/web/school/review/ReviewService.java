package gs.web.school.review;

import gs.data.community.User;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ReviewService {

    IReviewDao _reviewDao;

    public static enum ReviewUpgradeStatus {
        REVIEW_UPGRADED_NOT_PUBLISHED,
        REVIEW_UPGRADED_PUBLISHED,
        NO_REVIEW_UPGRADED
    }

    public static class ReviewUpgradeSummary {
        private List<Review> _upgradedReviews;
        private ReviewUpgradeStatus _status;
        private Review _firstPublishedReview;

        public ReviewUpgradeSummary() {
            _status  = ReviewUpgradeStatus.NO_REVIEW_UPGRADED;
            _upgradedReviews = new ArrayList<Review>();
        }

        public List<Review> getUpgradedReviews() {
            return _upgradedReviews;
        }

        public void setUpgradedReviews(List<Review> upgradedReviews) {
            _upgradedReviews = upgradedReviews;
        }

        public ReviewUpgradeStatus getStatus() {
            return _status;
        }

        public void setStatus(ReviewUpgradeStatus status) {
            _status = status;
        }

        public Review getFirstPublishedReview() {
            return _firstPublishedReview;
        }

        public void setFirstPublishedReview(Review firstPublishedReview) {
            _firstPublishedReview = firstPublishedReview;
        }
    }

    /**
     * Helper method that upgrades any provisional reviews for user to the associated non-provisional
     * status (e.g. pd --> d). Also builds a ReviewUpgradeSummary to encapsulate upgraded reviews and upgrade status
     *
     * @param user
     * @return ReviewUpgradeSummary which contains and describes upgraded reviews
     */
    public ReviewUpgradeSummary upgradeProvisionalReviewsAndSummarize(User user) {

        List<Review> upgradedReviews = upgradeProvisionalReviews(user);

        return createReviewUpgradeSummary(upgradedReviews);
    }

    /**
     * Upgrades any provisional reviews for user to the associated non-provisional status (e.g. pd --> d)
     *
     * @param user
     * @return List of upgraded reviews
     */
    public List<Review> upgradeProvisionalReviews(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Cannot upgrade reviews for null user.");
        }

        List<Review> userReviews = getReviewDao().findUserReviews(user);
        List<Review> upgradedReviews = new ArrayList<Review>();

        Review anUpgradedReview = null;
        for (Review review : userReviews) {
            String status = review.getStatus();
            if (StringUtils.length(status) > 1 && StringUtils.startsWith(status, "p")) {
                review.setStatus(StringUtils.substring(status, 1));
                _reviewDao.saveReview(review);
                upgradedReviews.add(review);
            }
        }

        return upgradedReviews;
    }

    protected ReviewUpgradeSummary createReviewUpgradeSummary(List<Review> upgradedReviews) {
        if (upgradedReviews == null) {
            throw new IllegalArgumentException("Cannot examine null upgraded reviews list");
        }

        ReviewUpgradeSummary summary = new ReviewUpgradeSummary();
        ReviewUpgradeStatus status;

        if (upgradedReviews.size() > 0) {
            summary.setUpgradedReviews(upgradedReviews);
            List<Review> publishedReviews = findPublishedReviews(upgradedReviews);

            if (publishedReviews.size() > 0) {
                summary.setFirstPublishedReview(publishedReviews.get(0));
                status = ReviewUpgradeStatus.REVIEW_UPGRADED_PUBLISHED;
            } else {
                status = ReviewUpgradeStatus.REVIEW_UPGRADED_NOT_PUBLISHED;
            }
        } else {
            status = ReviewUpgradeStatus.NO_REVIEW_UPGRADED;
        }

        summary.setStatus(status);

        return summary;
    }
    
    public List<Review> findPublishedReviews(List<Review> upgradedReviews) {
        if (upgradedReviews == null) throw new IllegalArgumentException("Cannot iterate null upgradedReviews list");

        List<Review> publishedReviews = new ArrayList<Review>();

        for (Review review : upgradedReviews) {
            if (Review.ReviewStatus.PUBLISHED.equals(review.getStatusAsEnum())) {
                publishedReviews.add(review);
            }
        }

        return publishedReviews;
    }

    public IReviewDao getReviewDao() {
        return _reviewDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }
}
