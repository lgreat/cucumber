package gs.web.about.feedback;

import gs.data.state.State;

/**
 * @author Dave Roy <mailto:droy@greatschools.net>
 */
public class DistrictInfoFields {
    private String _districtName;
//    private State _state;
    private String _referenceUrl;
    private String _relationship;
    private String _comment;

    public String getDistrictName() {
        return _districtName;
    }

    public void setDistrictName(String districtName) {
        _districtName = districtName;
    }

//    public State getState() {
//        return _state;
//    }
//
//    public void setState(State state) {
//        _state = state;
//    }

    public String getReferenceUrl() {
        return _referenceUrl;
    }

    public void setReferenceUrl(String referenceUrl) {
        this._referenceUrl = referenceUrl;
    }

    public String getRelationship() {
        return _relationship;
    }

    public void setRelationship(String relationship) {
        _relationship = relationship;
    }

    public String getComment() {
        return _comment;
    }

    public void setComment(String comment) {
        _comment = comment;
    }
}