/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: ParentReviewTagHandler.java,v 1.5 2011/09/21 12:52:02 yfan Exp $
 */
package gs.web.jsp.link.school;

import gs.data.school.LevelCode;
import gs.web.util.UrlBuilder;

/**
 * School Profile Parent Reviews Page
 *
 * @author David Lee <mailto:dlee@greatschools.org>
 */
public class ParentReviewTagHandler extends BaseSchoolTagHandler {
    private Integer _page;

    protected UrlBuilder createUrlBuilder() {
        //Preschool pages are hosted from a separate domain and therefore must use an absolute URL (GS-12127)
        if (LevelCode.PRESCHOOL.equals(getSchool().getLevelCode())) {
            setAbsolute(true);
        }
        UrlBuilder builder = new UrlBuilder(getSchool(), _page, UrlBuilder.SCHOOL_PARENT_REVIEWS);
        return builder;
    }

    public Integer getPage() {
        return _page;
    }

    public void setPage(Integer page) {
        _page = page;
    }
}
