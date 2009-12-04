/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: HomeTagHandler.java,v 1.2 2009/12/04 20:54:11 npatury Exp $
 */

package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * Generates home page tag.
 *
 * @author Andrew Peterson <mailto:apeterson@greatschools.net>
 */
public class HomeTagHandler extends LinkTagHandler {

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.HOME, getState());
        return builder;
    }

}
