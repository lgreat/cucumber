package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

public class CmsWorksheetGalleryTagHandler extends LinkTagHandler {
    private String _language;
    private String _grade;
    private String _subject;

    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.CMS_WORKSHEET_GALLERY, null, _grade, _subject, _language);
    }

    public String getSubject() {
        return _subject;
    }

    public void setSubject(String subject) {
        _subject = subject;
    }

    public String getLanguage() {
        return _language;
    }

    public void setLanguage(String language) {
        _language = language;
    }

    public String getGrade() {
        return _grade;
    }

    public void setGrade(String grade) {
        _grade = grade;
    }
}
