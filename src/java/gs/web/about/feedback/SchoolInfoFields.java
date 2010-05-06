package gs.web.about.feedback;

import gs.data.state.State;

/**
 * @author Dave Roy <mailto:droy@greatschools.net>
 */
public class SchoolInfoFields {
    private String _infoType = "";
    private String _referenceUrl;
    private String _relationship;
    private String _comment;

    public String getInfoType() {
        return _infoType;
    }

    public void setInfoType(String infoType) {
        _infoType = infoType;
    }

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
