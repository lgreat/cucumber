/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: ArticleLibraryTagHandler.java,v 1.1 2006/04/27 22:53:47 apeterson Exp $
 */

package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * Generates My School List tag.
 *
 * @author Andrew Peterson <mailto:apeterson@greatschools.net>
 */
public class ArticleLibraryTagHandler extends LinkTagHandler {

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.ARTICLE_LIBRARY, getState());
        return builder;
    }

}
