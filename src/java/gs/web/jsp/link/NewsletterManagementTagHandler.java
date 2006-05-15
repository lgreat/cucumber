/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: NewsletterManagementTagHandler.java,v 1.1 2006/05/15 21:55:51 dlee Exp $
 */
package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;

/**
 * The purpose is ...
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class NewsletterManagementTagHandler extends LinkTagHandler {
    private String _email;

    protected UrlBuilder createUrlBuilder() {

        UrlBuilder builder = new UrlBuilder(UrlBuilder.NEWSLETTER_MANAGEMENT, getState());

        if (StringUtils.isNotEmpty(_email)) {
            builder.setParameter("email", _email);
        }

        return builder;
    }

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        _email = email;
    }
}
