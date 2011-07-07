package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

public class CmsWorksheetGalleryTagHandler extends LinkTagHandler {
    private String _language;
    private String _topicIDs;
    private String _gradeIDs;
    private String _subjectIDs;
    // Video gallery is viewed within the context of a topic center
    private Long _topicCenterId;
    private String _topicCenterUrl;

    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.CMS_WORKSHEET_GALLERY, _topicCenterId, _topicCenterUrl, _topicIDs, _gradeIDs, _subjectIDs, _language);
    }

    public String getTopicIDs() {
        return _topicIDs;
    }

    public void setTopicIDs(String topicIDs) {
        _topicIDs = topicIDs;
    }

    public String getGradeIDs() {
        return _gradeIDs;
    }

    public void setGradeIDs(String gradeIDs) {
        _gradeIDs = gradeIDs;
    }

    public String getSubjectIDs() {
        return _subjectIDs;
    }

    public void setSubjectIDs(String subjectIDs) {
        _subjectIDs = subjectIDs;
    }

    public String getLanguage() {
        return _language;
    }

    public void setLanguage(String language) {
        _language = language;
    }

    public Long getTopicCenterId() {
        return _topicCenterId;
    }

    public void setTopicCenterId(Long topicCenterId) {
        _topicCenterId = topicCenterId;
    }
    
    public String getTopicCenterUrl() {
        return _topicCenterUrl;
    }

    public void setTopicCenterUrl(String topicCenterUrl) {
        _topicCenterUrl = topicCenterUrl;
    }
}
