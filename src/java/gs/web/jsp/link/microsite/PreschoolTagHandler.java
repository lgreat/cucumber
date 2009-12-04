package gs.web.jsp.link.microsite;

import gs.web.jsp.link.LinkTagHandler;
import gs.web.util.UrlBuilder;

/**
 * Preschool Microsite
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.org>
 */
public class PreschoolTagHandler extends LinkTagHandler {
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.PRESCHOOL);
    }
}
