package gs.web.jsp.link.school;

import gs.web.util.UrlBuilder;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class EspTagHandler extends BaseSchoolTagHandler {
    @Override
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(getSchool(), UrlBuilder.SCHOOL_PROFILE_ESP);
    }
}
