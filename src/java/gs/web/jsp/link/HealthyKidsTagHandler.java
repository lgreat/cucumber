package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * Healthy Kids Microsite
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class HealthyKidsTagHandler extends LinkTagHandler {
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.HEALTHY_KIDS, getState());
    }
}
