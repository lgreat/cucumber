/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: CensusTagHandler.java,v 1.1 2006/10/12 23:58:04 dlee Exp $
 */
package gs.web.jsp.link.school;

import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.web.util.UrlBuilder;

/**
 * School Profile Census
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class CensusTagHandler extends BaseSchoolTagHandler {
    protected UrlBuilder createUrlBuilder() {
        School school = getSchool();
        if (SchoolType.PRIVATE.equals (school.getType())) {
            return new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE_CENSUS_PRIVATE);            
        } else {
            return new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE_CENSUS);
        }
    }
}
