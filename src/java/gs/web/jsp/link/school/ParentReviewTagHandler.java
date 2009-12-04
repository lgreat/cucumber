/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: ParentReviewTagHandler.java,v 1.3 2009/12/04 22:27:06 chriskimm Exp $
 */
package gs.web.jsp.link.school;

import gs.web.util.UrlBuilder;

/**
 * School Profile Parent Reviews Page
 *
 * @author David Lee <mailto:dlee@greatschools.org>
 */
public class ParentReviewTagHandler extends BaseSchoolTagHandler {

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(getSchool(), UrlBuilder.SCHOOL_PARENT_REVIEWS);
        return builder;
    }
}
