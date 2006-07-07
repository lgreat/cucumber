/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: HomeTagHandler.java,v 1.1 2006/07/07 20:09:26 apeterson Exp $
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
