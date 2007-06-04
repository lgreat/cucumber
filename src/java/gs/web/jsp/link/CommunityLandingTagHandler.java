package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class CommunityLandingTagHandler  extends LinkTagHandler {
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.COMMUNITY_LANDING, null);
        return builder;
    }
}
