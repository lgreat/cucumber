/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SchoolProfileCensusTagHandler.java,v 1.1 2006/09/29 23:22:55 dlee Exp $
 */
package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * School Profile Census
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class SchoolProfileCensusTagHandler extends BaseSchoolProfileTagHandler{
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(getSchool(), UrlBuilder.SCHOOL_PROFILE_CENSUS);
        return builder;
    }
}
