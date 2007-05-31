package gs.web.jsp.link.school;

import gs.web.util.UrlBuilder;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class EspLoginTagHandler extends BaseSchoolTagHandler {
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(getSchool(), UrlBuilder.SCHOOL_PROFILE_ESP_LOGIN);
    }
}
