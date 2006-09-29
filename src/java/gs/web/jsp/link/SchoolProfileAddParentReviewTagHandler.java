/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SchoolProfileAddParentReviewTagHandler.java,v 1.1 2006/09/29 23:22:55 dlee Exp $
 */
package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * The purpose is ...
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class SchoolProfileAddParentReviewTagHandler extends BaseSchoolProfileTagHandler{
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(getSchool(), UrlBuilder.SCHOOL_PROFILE_ADD_PARENT_REVIEW);
        return builder;
    }
}
