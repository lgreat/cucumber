/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: CensusTagHandler.java,v 1.4 2011/09/19 00:19:52 ssprouse Exp $
 */
package gs.web.jsp.link.school;

import gs.data.school.LevelCode;
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

        //Preschool pages are hosted from a separate domain and therefore must use an absolute URL (GS-12127)
        if (LevelCode.PRESCHOOL.equals(school.getLevelCode())) {
            setAbsolute(true);
        }

        if (SchoolType.PRIVATE.equals (school.getType())) {
            return new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE_CENSUS_PRIVATE);            
        } else {
            return new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE_CENSUS);
        }
    }
}
