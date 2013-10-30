package gs.web.school.review;

import gs.data.community.User;
import gs.data.school.review.*;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ReviewService {

    IReviewDao _reviewDao;
    ITopicalSchoolReviewDao _topicalSchoolReviewDao;

    public static enum ReviewUpgradeStatus {
        REVIEW_UPGRADED_NOT_PUBLISHED,
        REVIEW_UPGRADED_PUBLISHED,
        NO_REVIEW_UPGRADED
    }

    public static class ReviewUpgradeSummary {
        private List<ISchoolReview> _upgradedReviews;
        private ReviewUpgradeStatus _status;
        private ISchoolReview _firstPublishedReview;

        public ReviewUpgradeSummary() {
            _status  = ReviewUpgradeStatus.NO_REVIEW_UPGRADED;
            _upgradedReviews = new ArrayList<ISchoolReview>();
        }

        public List<ISchoolReview> getUpgradedReviews() {
            return _upgradedReviews;
        }

        public void setUpgradedReviews(List<ISchoolReview> upgradedReviews) {
            _upgradedReviews = upgradedReviews;
        }

        public ReviewUpgradeStatus getStatus() {
            return _status;
        }

        public void setStatus(ReviewUpgradeStatus status) {
            _status = status;
        }

        public ISchoolReview getFirstPublishedReview() {
            return _firstPublishedReview;
        }

        public void setFirstPublishedReview(ISchoolReview firstPublishedReview) {
            _firstPublishedReview = firstPublishedReview;
        }
    }

    /**
     * Helper method that upgrades any provisional reviews for user to the associated non-provisional
     * status (e.g. pd --> d). Also builds a ReviewUpgradeSummary to encapsulate upgraded reviews and upgrade status
     *
     * @param user user to upgrade reviews for
     * @return ReviewUpgradeSummary which contains and describes upgraded reviews
     */
    public ReviewUpgradeSummary upgradeProvisionalReviewsAndSummarize(User user) {

        List<ISchoolReview> upgradedReviews = upgradeProvisionalReviews(user);

        return createReviewUpgradeSummary(upgradedReviews);
    }

    /**
     * Upgrades any provisional reviews for user to the associated non-provisional status (e.g. pd --> d)
     *
     * @param user user to upgrade reviews for
     * @return List of upgraded reviews
     */
    public List<ISchoolReview> upgradeProvisionalReviews(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Cannot upgrade reviews for null user.");
        }

        List<Review> userReviews = getReviewDao().findUserReviews(user);
        List<ISchoolReview> upgradedReviews = new ArrayList<ISchoolReview>();
        List<TopicalSchoolReview> topicalReviews = getTopicalSchoolReviewDao().findByMemberId(user.getId());

        for (Review review : userReviews) {
            String status = review.getStatus();
            if (StringUtils.length(status) > 1 && StringUtils.startsWith(status, "p")) {
                review.setStatus(StringUtils.substring(status, 1));
                review.setProcessDate(new Date());
                _reviewDao.saveReview(review);
                upgradedReviews.add(review);
            }
        }
        for (TopicalSchoolReview review: topicalReviews) {
            String status = review.getStatus();
            if (StringUtils.length(status) > 1 && StringUtils.startsWith(status, "p")) {
                review.setStatus(StringUtils.substring(status, 1));
                review.setProcessDate(new Date());
                _topicalSchoolReviewDao.save(review);
                upgradedReviews.add(review);
            }
        }

        Collections.sort(upgradedReviews, Collections.reverseOrder(ISchoolReview.GENERIC_DATE_POSTED_COMPARATOR));

        return upgradedReviews;
    }

    protected ReviewUpgradeSummary createReviewUpgradeSummary(List<ISchoolReview> upgradedReviews) {
        if (upgradedReviews == null) {
            throw new IllegalArgumentException("Cannot examine null upgraded reviews list");
        }

        ReviewUpgradeSummary summary = new ReviewUpgradeSummary();
        ReviewUpgradeStatus status;

        if (upgradedReviews.size() > 0) {
            summary.setUpgradedReviews(upgradedReviews);
            List<ISchoolReview> publishedReviews = findPublishedReviews(upgradedReviews);

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
    
    public List<ISchoolReview> findPublishedReviews(List<ISchoolReview> upgradedReviews) {
        if (upgradedReviews == null) throw new IllegalArgumentException("Cannot iterate null upgradedReviews list");

        List<ISchoolReview> publishedReviews = new ArrayList<ISchoolReview>();

        for (ISchoolReview review : upgradedReviews) {
            if (StringUtils.equals(Review.ReviewStatus.PUBLISHED.getStatusCode(), review.getStatus())) {
                System.out.println("Adding " + review.getClass().getName() + ":" + review.getId() + ":" + review.getPosted());
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

    public ITopicalSchoolReviewDao getTopicalSchoolReviewDao() {
        return _topicalSchoolReviewDao;
    }

    public void setTopicalSchoolReviewDao(ITopicalSchoolReviewDao topicalSchoolReviewDao) {
        _topicalSchoolReviewDao = topicalSchoolReviewDao;
    }
}
