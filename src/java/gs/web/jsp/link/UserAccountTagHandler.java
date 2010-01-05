/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: UserAccountTagHandler.java,v 1.4 2010/01/05 22:51:23 yfan Exp $
 */

package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import gs.data.community.Discussion;
import gs.data.community.User;

/**
 * Generates link to the My Account page.
 *
 * @author Young Fan <mailto:yfan@greatschools.org>
 */
public class UserAccountTagHandler extends LinkTagHandler {

    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.USER_ACCOUNT, null);
    }
}