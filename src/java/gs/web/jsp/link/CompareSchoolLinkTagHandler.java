/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: CompareSchoolLinkTagHandler.java,v 1.1 2006/09/30 01:03:54 dlee Exp $
 */
package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * Compare spare link
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class CompareSchoolLinkTagHandler extends BaseSchoolProfileTagHandler {
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(getSchool(), UrlBuilder.COMPARE_SCHOOL);
        return builder;
    }
}
