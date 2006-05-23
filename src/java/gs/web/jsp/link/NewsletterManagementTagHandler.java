/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: NewsletterManagementTagHandler.java,v 1.2 2006/05/23 17:18:02 apeterson Exp $
 */
package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;

/**
 * Generate a tag to the page where the user manages their profile,
 * or right now, newsletters.
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

    public void setEmail(String email) {
        _email = email;
    }
}
