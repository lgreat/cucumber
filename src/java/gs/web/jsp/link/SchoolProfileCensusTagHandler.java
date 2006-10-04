/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SchoolProfileCensusTagHandler.java,v 1.2 2006/10/04 00:34:51 chriskimm Exp $
 */
package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import gs.data.school.School;
import gs.data.school.SchoolType;

/**
 * School Profile Census
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class SchoolProfileCensusTagHandler extends BaseSchoolProfileTagHandler{
    protected UrlBuilder createUrlBuilder() {
        School school = getSchool();
        if (SchoolType.PRIVATE.equals (school.getType())) {
            return new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE_CENSUS_PRIVATE);            
        } else {
            return new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE_CENSUS);
        }
    }
}
