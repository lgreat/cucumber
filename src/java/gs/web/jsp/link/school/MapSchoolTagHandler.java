/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: MapSchoolTagHandler.java,v 1.4 2011/09/19 00:19:52 ssprouse Exp $
 */
package gs.web.jsp.link.school;

import gs.data.school.LevelCode;
import gs.web.util.UrlBuilder;

/**
 * Map School page
 *
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class MapSchoolTagHandler extends BaseSchoolTagHandler {

    protected UrlBuilder createUrlBuilder() {
        //Preschool pages are hosted from a separate domain and therefore must use an absolute URL (GS-12127)
        if (LevelCode.PRESCHOOL.equals(getSchool().getLevelCode())) {
            setAbsolute(true);
        }
        return new UrlBuilder(getSchool(), UrlBuilder.SCHOOL_MAP);
    }
}
