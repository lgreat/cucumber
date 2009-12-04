package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * Generates middle-school page tag.
 *
 * @author Dave Roy <mailto:droy@greatschools.org>
 */
public class MiddleSchoolTagHandler extends LinkTagHandler {

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.MIDDLE_SCHOOL);
        return builder;
    }

}
