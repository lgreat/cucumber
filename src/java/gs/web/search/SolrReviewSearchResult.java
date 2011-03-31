package gs.web.search;

import gs.data.state.State;

import java.util.Date;

public class SolrReviewSearchResult implements IReviewResult {

    private Integer _id;

    private Integer _schoolId;

    private State _schoolDatabaseState;

    private String _schoolName;

    private String _schoolAddress;

    private String _url;

    private Date _postedDate;

    private Integer _rating;

    private String _submitterName;

    private String _comments;

    public Integer getId() {
        return _id;
    }

    public void setId(Integer id) {
        _id = id;
    }

    public Integer getSchoolId() {
        return _schoolId;
    }

    public void setSchoolId(Integer schoolId) {
        _schoolId = schoolId;
    }

    public State getSchoolDatabaseState() {
        return _schoolDatabaseState;
    }

    public void setSchoolDatabaseState(State schoolDatabaseState) {
        _schoolDatabaseState = schoolDatabaseState;
    }

    public String getSchoolName() {
        return _schoolName;
    }

    public void setSchoolName(String schoolName) {
        _schoolName = schoolName;
    }

    public String getSchoolAddress() {
        return _schoolAddress;
    }

    public void setSchoolAddress(String schoolAddress) {
        _schoolAddress = schoolAddress;
    }

    public String getUrl() {
        return _url;
    }

    public void setUrl(String url) {
        _url = url;
    }

    public Date getPostedDate() {
        return _postedDate;
    }

    public void setPostedDate(Date postedDate) {
        _postedDate = postedDate;
    }

    public Integer getRating() {
        return _rating;
    }

    public void setRating(Integer rating) {
        _rating = rating;
    }

    public String getSubmitterName() {
        return _submitterName;
    }

    public void setSubmitterName(String submitterName) {
        _submitterName = submitterName;
    }

    public String getComments() {
        return _comments;
    }

    public void setComments(String comments) {
        _comments = comments;
    }
}
