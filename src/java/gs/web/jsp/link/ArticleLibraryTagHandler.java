/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: ArticleLibraryTagHandler.java,v 1.2 2009/02/19 07:20:20 chriskimm Exp $
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
