/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: CompareSchoolTagHandler.java,v 1.1 2006/10/12 23:58:04 dlee Exp $
 */
package gs.web.jsp.link.school;

import gs.web.util.UrlBuilder;

/**
 * Compare spare link
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class CompareSchoolTagHandler extends BaseSchoolTagHandler {
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(getSchool(), UrlBuilder.COMPARE_SCHOOL);
        return builder;
    }
}
