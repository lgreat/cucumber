package gs.web.compare;

import gs.data.school.review.Review;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ComparedSchoolRatingsStruct extends ComparedSchoolBaseStruct {
    private int _teacherRating = 0;
    private int _principalRating = 0;
    private int _parentRating = 0;
    private int _numRatings = 0;
    private int _numReviews = 0;
    private Review _recentReview;

    public int getTeacherRating() {
        return _teacherRating;
    }

    public void setTeacherRating(int teacherRating) {
        _teacherRating = teacherRating;
    }

    public int getPrincipalRating() {
        return _principalRating;
    }

    public void setPrincipalRating(int principalRating) {
        _principalRating = principalRating;
    }

    public int getParentRating() {
        return _parentRating;
    }

    public void setParentRating(int parentRating) {
        _parentRating = parentRating;
    }

    public int getNumRatings() {
        return _numRatings;
    }

    public void setNumRatings(int numRatings) {
        _numRatings = numRatings;
    }

    public int getNumReviews() {
        return _numReviews;
    }

    public void setNumReviews(int numReviews) {
        _numReviews = numReviews;
    }

    public Review getRecentReview() {
        return _recentReview;
    }

    public void setRecentReview(Review recentReview) {
        _recentReview = recentReview;
    }

    /* Convenience methods */

    public boolean getHasReviews() {
        return _numReviews > 0;
    }
}
