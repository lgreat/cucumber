/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: ArticleLibraryTagHandler.java,v 1.4 2009/12/04 22:15:09 npatury Exp $
 */

package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * @author greatschools.org>
 */
public class ArticleLibraryTagHandler extends LinkTagHandler {

    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.ARTICLE_LIBRARY, getState());
    }
}
