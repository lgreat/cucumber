/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: NewsletterCenterTagHandler.java,v 1.1 2006/04/27 22:53:47 apeterson Exp $
 */

package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;

/**
 * Generates Newsletter Center tag.
 *
 * @author Andrew Peterson <mailto:apeterson@greatschools.net>
 */
public class NewsletterCenterTagHandler extends LinkTagHandler {

    private String _email;


    protected UrlBuilder createUrlBuilder() {

        UrlBuilder builder = new UrlBuilder(UrlBuilder.NEWSLETTER_CENTER, getState());

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
