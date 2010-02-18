package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class AdvisorsTagHandler extends LinkTagHandler {
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.ADVISORS);
    }
}
