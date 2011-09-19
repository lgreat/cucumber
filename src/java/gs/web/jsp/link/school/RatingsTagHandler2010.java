/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: RatingsTagHandler2010.java,v 1.2 2011/09/19 00:19:53 ssprouse Exp $
 */
package gs.web.jsp.link.school;

import gs.data.school.LevelCode;
import gs.data.state.State;
import gs.web.jsp.link.LinkTagHandler;
import gs.web.util.UrlBuilder;

/**
 * School Profile ratings page
 *
 * @author David Lee <mailto:dlee@greatschools.org>
 */
public class RatingsTagHandler2010 extends LinkTagHandler {
    // should be database state of school
    private State _state;
    private Integer _schoolId;
    private LevelCode _levelCode;

    protected UrlBuilder createUrlBuilder() {
        //Preschool pages are hosted from a separate domain and therefore must use an absolute URL (GS-12127)
        if (LevelCode.PRESCHOOL.equals(_levelCode)) {
            setAbsolute(true);
        }
        return new UrlBuilder(_state, _schoolId, _levelCode, UrlBuilder.SCHOOL_PROFILE_RATINGS);
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

    public LevelCode getLevelCode() {
        return _levelCode;
    }

    public void setLevelCode(LevelCode levelCode) {
        _levelCode = levelCode;
    }
}
