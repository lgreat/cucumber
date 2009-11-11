/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: UserProfileTagHandler.java,v 1.3 2009/11/11 19:50:51 yfan Exp $
 */

package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import gs.data.community.User;
import org.apache.commons.lang.StringUtils;

/**
 * Generates link to a user's profile page.
 *
 * @author Dave Roy <mailto:droy@greatschools.net>
 */
public class UserProfileTagHandler extends LinkTagHandler {
    private User _user;
    private String _username;

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder;
        if (StringUtils.isNotBlank(_username)) {
            builder = new UrlBuilder(_username, UrlBuilder.USER_PROFILE);
        } else if (_user != null && _user.getUserProfile() != null) {
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

    public String getUsername() {
        return _username;
    }

    public void setUsername(String username) {
        _username = username;
    }
}