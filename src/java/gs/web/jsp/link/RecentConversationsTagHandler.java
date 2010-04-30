package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

public class RecentConversationsTagHandler {
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.RECENT_CONVERSATIONS);
        return builder;
    }
}
