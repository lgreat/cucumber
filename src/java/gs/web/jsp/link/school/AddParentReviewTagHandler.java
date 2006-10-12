/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: AddParentReviewTagHandler.java,v 1.1 2006/10/12 23:58:04 dlee Exp $
 */
package gs.web.jsp.link.school;

import gs.web.util.UrlBuilder;

/**
 * The purpose is ...
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class AddParentReviewTagHandler extends BaseSchoolTagHandler {
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(getSchool(), UrlBuilder.SCHOOL_PROFILE_ADD_PARENT_REVIEW);
        return builder;
    }
}
