/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: MapSchoolTagHandler.java,v 1.1 2007/05/14 23:14:27 aroy Exp $
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
