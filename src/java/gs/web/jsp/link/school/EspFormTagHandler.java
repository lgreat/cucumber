package gs.web.jsp.link.school;

import gs.data.school.LevelCode;
import gs.web.util.UrlBuilder;

/**
 * @author aroy@greatschools.org
 */
public class EspFormTagHandler extends BaseSchoolTagHandler {
    private Integer _page;

    @Override
    protected UrlBuilder createUrlBuilder() {
        //Preschool pages are hosted from a separate domain and therefore must use an absolute URL (GS-12127)
        if (LevelCode.PRESCHOOL.equals(getSchool().getLevelCode())) {
            setAbsolute(true);
        }
        return new UrlBuilder(getSchool(), _page, UrlBuilder.SCHOOL_PROFILE_ESP_FORM);
    }

    public Integer getPage() {
        return _page;
    }

    public void setPage(Integer page) {
        _page = page;
    }
}
