package gs.web.jsp.link.school;

import gs.web.util.UrlBuilder;

/**
 * @author chriskimm@greatschools.net
 */
public class StartSurveyResultsTagHandler extends BaseSchoolTagHandler {

    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(getSchool(), UrlBuilder.START_SURVEY_RESULTS);
    }
}
