package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * Generates elementary-school page tag.
 *
 * @author Dave Roy <mailto:droy@greatschools.org>
 */
public class ElementarySchoolTagHandler extends LinkTagHandler {

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.ELEMENTARY_SCHOOL);
        return builder;
    }

}
