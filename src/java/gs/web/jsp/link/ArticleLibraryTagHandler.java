/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: ArticleLibraryTagHandler.java,v 1.3 2009/12/04 20:54:11 npatury Exp $
 */

package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * @author Andrew Peterson <mailto:apeterson@greatschools.net>
 */
public class ArticleLibraryTagHandler extends LinkTagHandler {

    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.ARTICLE_LIBRARY, getState());
    }
}
