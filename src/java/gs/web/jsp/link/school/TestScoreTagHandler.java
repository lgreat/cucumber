/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: TestScoreTagHandler.java,v 1.1 2006/10/12 23:58:04 dlee Exp $
 */
package gs.web.jsp.link.school;

import gs.web.util.UrlBuilder;

/**
 * School Profile Test Scores Link
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class TestScoreTagHandler extends BaseSchoolTagHandler {
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(getSchool(), UrlBuilder.SCHOOL_PROFILE_TEST_SCORE);
        return builder;
    }
}
