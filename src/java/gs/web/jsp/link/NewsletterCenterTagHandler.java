/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: NewsletterCenterTagHandler.java,v 1.3 2008/07/31 18:17:26 thuss Exp $
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

    public NewsletterCenterTagHandler() {
        super();
        setRel("nofollow");
    }

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
