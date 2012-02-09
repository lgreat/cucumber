package gs.web.jsp.link.school;

import gs.data.school.LevelCode;
import gs.web.util.UrlBuilder;

/**
 * @author aroy@greatschools.org
 */
public class EspDisplayTagHandler extends BaseSchoolTagHandler {
    @Override
    protected UrlBuilder createUrlBuilder() {
        //Preschool pages are hosted from a separate domain and therefore must use an absolute URL (GS-12127)
        if (getSchool().isPreschoolOnly()) {
            setAbsolute(true);
        }
        return new UrlBuilder(getSchool(), UrlBuilder.SCHOOL_PROFILE_ESP_DISPLAY);
    }
}
