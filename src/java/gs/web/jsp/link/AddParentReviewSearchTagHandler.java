/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: AddParentReviewSearchTagHandler.java,v 1.3 2009/12/04 22:15:09 npatury Exp $
 */
package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * Url info for parent review search
 *
 * @author greatschools.org>
 */
public class AddParentReviewSearchTagHandler extends LinkTagHandler {
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.ADD_PARENT_REVIEW_SEARCH, getState());
        return builder;
    }
}
