/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: PrincipalViewTagHandler.java,v 1.2 2009/12/04 20:54:12 npatury Exp $
 */
package gs.web.jsp.link.school;

import gs.web.util.UrlBuilder;

/**
 * School Profile Principal View
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class PrincipalViewTagHandler extends BaseSchoolTagHandler {
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(getSchool(), UrlBuilder.SCHOOL_PROFILE_PRINCIPAL_VIEW);
        return builder;
    }
}
