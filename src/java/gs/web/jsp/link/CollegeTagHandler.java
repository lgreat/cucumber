package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * Generates college page tag.
 *
 * @author Dave Roy <mailto:droy@greatschools.org>
 */
public class CollegeTagHandler extends LinkTagHandler {

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.COLLEGE);
        return builder;
    }

}
