/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: ParentReviewTagHandler.java,v 1.1 2006/10/12 23:58:04 dlee Exp $
 */
package gs.web.jsp.link.school;

import gs.web.util.UrlBuilder;

/**
 * School Profile Parent Reviews Page
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class ParentReviewTagHandler extends BaseSchoolTagHandler {

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(getSchool(), UrlBuilder.SCHOOL_PARENT_REVIEWS);
        return builder;
    }
}
