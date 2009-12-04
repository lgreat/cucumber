/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: HomeTagHandler.java,v 1.3 2009/12/04 22:27:02 chriskimm Exp $
 */

package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * Generates home page tag.
 *
 * @author Andrew Peterson <mailto:apeterson@greatschools.org>
 */
public class HomeTagHandler extends LinkTagHandler {

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.HOME, getState());
        return builder;
    }

}
