package gs.web.jsp.link.microsite;

import gs.web.jsp.link.LinkTagHandler;
import gs.web.util.UrlBuilder;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class StateStandardsTagHandler extends LinkTagHandler {
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.STATE_STANDARDS);
    }
}
