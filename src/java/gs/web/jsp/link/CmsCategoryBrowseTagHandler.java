package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import gs.data.content.cms.ICmsCategoryDao;
import gs.data.content.cms.CmsCategory;
import gs.data.util.SpringUtil;
import org.apache.commons.lang.StringEscapeUtils;

public class CmsCategoryBrowseTagHandler extends LinkTagHandler {
    private String _language;
    private String _topicIDs;
    private String _gradeIDs;
    private String _subjectIDs;

    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.CMS_CATEGORY_BROWSE, _topicIDs, _gradeIDs, _subjectIDs, _language);
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
}
