/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: AddParentReviewTagHandler.java,v 1.2 2009/12/04 20:54:12 npatury Exp $
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
