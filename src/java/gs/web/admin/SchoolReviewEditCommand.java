package gs.web.admin;

import gs.data.school.review.Review;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class SchoolReviewEditCommand extends ReportedReviewEditCommand{
    private Review _review;

    public Review getReview() {
        return _review;
    }

    public void setReview(Review review) {
        _review = review;
    }
}
