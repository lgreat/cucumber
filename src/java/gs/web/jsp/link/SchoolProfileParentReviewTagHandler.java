/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SchoolProfileParentReviewTagHandler.java,v 1.1 2006/09/29 23:22:55 dlee Exp $
 */
package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * School Profile Parent Reviews Page
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class SchoolProfileParentReviewTagHandler extends BaseSchoolProfileTagHandler {

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(getSchool(), UrlBuilder.SCHOOL_PARENT_REVIEWS);
        return builder;
    }
}
