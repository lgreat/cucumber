/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: UserProfileTagHandler.java,v 1.2 2009/10/12 14:46:15 aroy Exp $
 */

package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import gs.data.community.User;

/**
 * Generates link to a user's profile page.
 *
 * @author Dave Roy <mailto:droy@greatschools.net>
 */
public class UserProfileTagHandler extends LinkTagHandler {
    private User _user;

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder;
        if (_user != null && _user.getUserProfile() != null) {
            builder = new UrlBuilder(_user, UrlBuilder.USER_PROFILE);
        } else {
            if (_user == null) {
                throw new IllegalArgumentException("UserProfileTagHandler requires a non-null community user");
            } else {
                throw new IllegalArgumentException("UserProfileTagHandler requires a valid community user: " + 
                        _user.getEmail() + " is missing a UserProfile");
            }
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