/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: TestScoreTagHandler.java,v 1.3 2009/12/04 22:27:06 chriskimm Exp $
 */
package gs.web.jsp.link.school;

import gs.web.util.UrlBuilder;

/**
 * School Profile Test Scores Link
 *
 * @author David Lee <mailto:dlee@greatschools.org>
 */
public class TestScoreTagHandler extends BaseSchoolTagHandler {
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(getSchool(), UrlBuilder.SCHOOL_PROFILE_TEST_SCORE);
        return builder;
    }
}
