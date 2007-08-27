package gs.web.jsp.link.school;

import gs.web.util.UrlBuilder;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class TakeSurveyTagHandler extends BaseSchoolTagHandler {
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(getSchool(), UrlBuilder.SCHOOL_TAKE_SURVEY);
        return builder;
    }
}
