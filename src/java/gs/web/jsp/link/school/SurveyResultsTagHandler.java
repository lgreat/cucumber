package gs.web.jsp.link.school;

import gs.web.util.UrlBuilder;

/**
 * @author chriskimm@greatschools.net
 */
public class SurveyResultsTagHandler extends BaseSchoolTagHandler {

    private String _level;
    private String _page;

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(getSchool(), UrlBuilder.SURVEY_RESULTS);

        if (_level != null) {
            builder.addParameter("level", _level);
        }

        if (_page != null) {
            builder.addParameter("page", _page);
        }

        return builder;
    }

    public String getLevel() {
        return _level;
    }

    public void setLevel(String level) {
        _level = level;
    }

    public String getPage() {
        return _page;
    }

    public void setPage(String page) {
        _page = page;
    }
}
