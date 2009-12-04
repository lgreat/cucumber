/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: CensusTagHandler.java,v 1.3 2009/12/04 22:27:06 chriskimm Exp $
 */
package gs.web.jsp.link.school;

import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.web.util.UrlBuilder;

/**
 * School Profile Census
 *
 * @author David Lee <mailto:dlee@greatschools.org>
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
