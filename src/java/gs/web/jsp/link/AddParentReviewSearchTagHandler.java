/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: AddParentReviewSearchTagHandler.java,v 1.2 2009/12/04 20:54:11 npatury Exp $
 */
package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * Url info for parent review search
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class AddParentReviewSearchTagHandler extends LinkTagHandler {
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.ADD_PARENT_REVIEW_SEARCH, getState());
        return builder;
    }
}
