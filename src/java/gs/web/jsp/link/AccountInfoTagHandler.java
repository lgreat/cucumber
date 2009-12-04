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
public class AccountInfoTagHandler extends LinkTagHandler {
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.ACCOUNT_INFO, null);
    }
}