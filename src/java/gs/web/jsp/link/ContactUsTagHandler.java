package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

public class ContactUsTagHandler extends LinkTagHandler {
    private String _feedbackType;
    private String _cityName;
    private Integer _schoolId;

    public ContactUsTagHandler() {
        super();
        setRel("nofollow");
    }

    @Override
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.CONTACT_US, _feedbackType, _cityName, _schoolId);
    }

    public String getFeedbackType() {
        return _feedbackType;
    }

    public void setFeedbackType(String feedbackType) {
        _feedbackType = feedbackType;
    }

    public String getCityName() {
        return _cityName;
    }

    public void setCityName(String cityName) {
        _cityName = cityName;
    }

    public Integer getSchoolId() {
        return _schoolId;
    }

    public void setSchoolId(Integer schoolId) {
        _schoolId = schoolId;
    }
}
