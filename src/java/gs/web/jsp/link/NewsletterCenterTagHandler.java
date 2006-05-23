/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: NewsletterCenterTagHandler.java,v 1.2 2006/05/23 17:18:02 apeterson Exp $
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

    public void setEmail(String email) {
        _email = email;
    }
}
