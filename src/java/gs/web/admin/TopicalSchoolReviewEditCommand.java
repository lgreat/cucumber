package gs.web.admin;

import gs.data.school.review.TopicalSchoolReview;

/**
 * @author aroy@greatschools.org
 */
public class TopicalSchoolReviewEditCommand extends ReportedReviewEditCommand {
    private TopicalSchoolReview _review;

    public TopicalSchoolReview getReview() {
        return _review;
    }

    public void setReview(TopicalSchoolReview review) {
        _review = review;
    }
}
