package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * Generates high-school page tag.
 *
 * @author Dave Roy <mailto:droy@greatschools.org>
 */
public class HighSchoolTagHandler extends LinkTagHandler {

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.HIGH_SCHOOL);
        return builder;
    }

}
