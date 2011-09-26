package gs.web.jsp.link.school;

import gs.data.school.LevelCode;
import gs.web.util.UrlBuilder;

/**
 * @author chriskimm@greatschools.org
 */
public class SurveyResultsTagHandler extends BaseSchoolTagHandler {

    private String _level;
    private String _page;

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(getSchool(), UrlBuilder.SURVEY_RESULTS);
        //Preschool pages are hosted from a separate domain and therefore must use an absolute URL (GS-12127)
        if (LevelCode.PRESCHOOL.equals(getSchool().getLevelCode())) {
            setAbsolute(true);
        }

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
