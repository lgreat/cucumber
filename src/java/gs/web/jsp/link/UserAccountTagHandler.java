/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: UserAccountTagHandler.java,v 1.1 2009/10/09 23:09:06 yfan Exp $
 */

package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import gs.data.community.Discussion;
import gs.data.community.User;

/**
 * Generates link to the My Account page.
 *
 * @author Young Fan <mailto:yfan@greatschools.net>
 */
public class UserAccountTagHandler extends LinkTagHandler {
    private User _user;

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder;
        if (_user != null) {
            builder = new UrlBuilder(_user, UrlBuilder.USER_ACCOUNT);
        } else {
            throw new RuntimeException("DiscussionTagHandler requires a discussion or (a discussion id and fullUri)");
        }

        return builder;
    }
}