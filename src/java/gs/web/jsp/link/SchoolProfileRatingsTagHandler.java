/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SchoolProfileRatingsTagHandler.java,v 1.1 2006/09/29 23:22:55 dlee Exp $
 */
package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * School Profile ratings page
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class SchoolProfileRatingsTagHandler extends BaseSchoolProfileTagHandler {
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(getSchool(), UrlBuilder.SCHOOL_PROFILE_RATINGS);
        return builder;
    }
}
