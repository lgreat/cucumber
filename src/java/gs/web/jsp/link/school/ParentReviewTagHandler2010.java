/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: ParentReviewTagHandler2010.java,v 1.1 2010/12/06 23:08:54 yfan Exp $
 */
package gs.web.jsp.link.school;

import gs.data.state.State;
import gs.web.jsp.link.LinkTagHandler;
import gs.web.util.UrlBuilder;

/**
 * School Profile Parent Reviews Page
 *
 * @author David Lee <mailto:dlee@greatschools.org>
 */
public class ParentReviewTagHandler2010 extends LinkTagHandler {
    // should be database state of school
    private State _state;
    private Integer _schoolId;

    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(_state, _schoolId, UrlBuilder.SCHOOL_PARENT_REVIEWS);
    }

    public State getState() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }

    public Integer getSchoolId() {
        return _schoolId;
    }

    public void setSchoolId(Integer schoolId) {
        _schoolId = schoolId;
    }
}
