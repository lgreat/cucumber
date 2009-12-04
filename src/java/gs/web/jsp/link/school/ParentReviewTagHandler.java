/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: ParentReviewTagHandler.java,v 1.2 2009/12/04 20:54:12 npatury Exp $
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
