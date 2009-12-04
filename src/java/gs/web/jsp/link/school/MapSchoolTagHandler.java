/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: MapSchoolTagHandler.java,v 1.2 2009/12/04 20:54:12 npatury Exp $
 */
package gs.web.jsp.link.school;

import gs.web.util.UrlBuilder;

/**
 * Map School page
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class MapSchoolTagHandler extends BaseSchoolTagHandler {
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(getSchool(), UrlBuilder.SCHOOL_MAP);
    }
}
