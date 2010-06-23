/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: UserProfileTagHandler.java,v 1.7 2010/06/23 21:48:10 aroy Exp $
 */

package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import gs.data.community.User;
import gs.web.util.UrlUtil;
import org.apache.commons.lang.StringUtils;

import javax.servlet.jsp.JspException;

/**
 * Generates link to a user's profile page.
 *
 * @author Dave Roy <mailto:droy@greatschools.org>
 */
public class UserProfileTagHandler extends LinkTagHandler {
    private User _user;
    private String _username;
    private boolean anonymous = false;

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder;
        if (StringUtils.isNotBlank(_username)) {
            builder = new UrlBuilder(_username, UrlBuilder.USER_PROFILE);
        } else if (_user != null && _user.getUserProfile() != null) {
            builder = new UrlBuilder(_user, UrlBuilder.USER_PROFILE);
        } else {
            // aroy: stop this from crashing on dev servers, where the DB is often out of sync
            if (getSessionContext() != null && UrlUtil.isDevEnvironment(getSessionContext().getHostName())) {
                return new UrlBuilder("user_not_on_this_server", UrlBuilder.USER_PROFILE);
            }
            if (_user == null) {
                throw new IllegalArgumentException("UserProfileTagHandler requires a non-null community user");
            } else {
                throw new IllegalArgumentException("UserProfileTagHandler requires a valid community user: " + 
                        _user.getEmail() + " is missing a UserProfile");
            }
        }

        return builder;
    }

    public int doStartTag() throws JspException {
        if (!anonymous) {
            return super.doStartTag();
        }

        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() throws JspException {
        if (!anonymous) {
            return super.doEndTag();
        }

        return EVAL_PAGE;
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

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }
}