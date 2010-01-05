/**
 * Copyright (c) 2006 GreatSchools.org. All Rights Reserved.
 */
package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * Provides handling for account info links.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class RecentActivityTagHandler extends LinkTagHandler {
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.RECENT_ACTIVITY, null);
    }
}