package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class FindASchoolTagHandler extends LinkTagHandler {
    @Override
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.FIND_A_SCHOOL);
    }
}
