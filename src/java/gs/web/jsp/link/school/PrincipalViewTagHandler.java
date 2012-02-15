/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: PrincipalViewTagHandler.java,v 1.4 2012/02/15 16:37:30 aroy Exp $
 */
package gs.web.jsp.link.school;

import gs.web.util.UrlBuilder;

/**
 * School Profile Principal View
 *
 * @author David Lee <mailto:dlee@greatschools.org>
 */
public class PrincipalViewTagHandler extends BaseSchoolTagHandler {
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(getSchool(), UrlBuilder.SCHOOL_PROFILE_ESP_DISPLAY);
        return builder;
    }
}
