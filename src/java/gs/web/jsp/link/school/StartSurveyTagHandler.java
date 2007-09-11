package gs.web.jsp.link.school;

import gs.web.util.UrlBuilder;

public class StartSurveyTagHandler extends BaseSchoolTagHandler {
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(getSchool(), UrlBuilder.SCHOOL_START_SURVEY);
        return builder;
    }
}
