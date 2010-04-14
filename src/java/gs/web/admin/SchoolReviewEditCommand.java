package gs.web.admin;

import gs.data.school.review.Review;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class SchoolReviewEditCommand {
    private Review _review;
    private Integer _id;
    private String _note;
    private String _status;
    private boolean _cancel = false;

    public Review getReview() {
        return _review;
    }

    public void setReview(Review review) {
        _review = review;
    }

    public Integer getId() {
        return _id;
    }

    public void setId(Integer id) {
        _id = id;
    }

    public String getNote() {
        return _note;
    }

    public void setNote(String note) {
        _note = note;
    }

    public String getStatus() {
        return _status;
    }

    public void setStatus(String status) {
        _status = status;
    }

    public boolean isCancel() {
        return _cancel;
    }

    public void setCancel(boolean cancel) {
        _cancel = cancel;
    }
}
