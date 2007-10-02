package gs.web.jsp.link.school;

import gs.web.util.UrlBuilder;

public class StartSurveyTagHandler extends BaseSchoolTagHandler {

    private String _cpn;

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(getSchool(), UrlBuilder.SCHOOL_START_SURVEY);

        if (_cpn != null) {
            builder.addParameter("cpn", _cpn);
        }
        
        return builder;
    }

    public String getCpn() {
        return _cpn;
    }

    public void setCpn(String cpn) {
        this._cpn = cpn;
    }
}
