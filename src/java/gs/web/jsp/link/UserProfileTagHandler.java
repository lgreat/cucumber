/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: UserProfileTagHandler.java,v 1.1 2009/10/08 23:28:26 droy Exp $
 */

package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import gs.data.community.Discussion;
import gs.data.community.User;

/**
 * Generates link to a discussion.
 *
 * @author Dave Roy <mailto:droy@greatschools.net>
 */
public class UserProfileTagHandler extends LinkTagHandler {
    private User _user;

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder;
        if (_user != null) {
            builder = new UrlBuilder(_user, UrlBuilder.USER_PROFILE);
        } else {
            throw new RuntimeException("DiscussionTagHandler requires a discussion or (a discussion id and fullUri)");
        }

        return builder;
    }

    public User getUser() {
        return _user;
    }

    public void setUser(User user) {
        _user = user;
    }
}