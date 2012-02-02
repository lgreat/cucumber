/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: SchoolOverviewTagHandler.java,v 1.3 2012/02/02 21:14:41 jkirton Exp $
 */
package gs.web.jsp.link.school;

import gs.data.school.LevelCode;
import gs.data.state.State;
import gs.data.util.Address;
import gs.web.util.UrlBuilder;

/**
 * School Overview Page
 *
 * @author Samson Sprouse <mailto:ssprouse@greatschools.org>
 */
public class SchoolOverviewTagHandler extends BaseSchoolTagHandler {
    private Boolean _showConfirmation;
    private Integer _schoolId;
    private State _databaseState;
    private String _name;
    private Address _physicalAddress;
    private String _levelCode;

    public SchoolOverviewTagHandler() {
        setAbsolute(true);
    }

    public Boolean getShowConfirmation() {
        return _showConfirmation;
    }

    public void setShowConfirmation(Boolean showConfirmation) {
        _showConfirmation = showConfirmation;
    }

    public Integer getSchoolId() {
        return _schoolId;
    }

    public void setSchoolId(Integer schoolId) {
        _schoolId = schoolId;
    }

    public State getDatabaseState() {
        return _databaseState;
    }

    public void setDatabaseState(State databaseState) {
        _databaseState = databaseState;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public Address getPhysicalAddress() {
        return _physicalAddress;
    }

    public void setPhysicalAddress(Address physicalAddress) {
        _physicalAddress = physicalAddress;
    }

    public String getLevelCode() {
        return _levelCode;
    }

    public void setLevelCode(String levelCode) {
        _levelCode = levelCode;
    }

    protected UrlBuilder createUrlBuilder() {
        if (_showConfirmation != null) {
            return new UrlBuilder(
                    UrlBuilder.SCHOOL_PROFILE,
                    getSchoolId(),
                    getDatabaseState(),
                    getName(),
                    getPhysicalAddress(), 
                    LevelCode.createLevelCode(getLevelCode()),
                    getShowConfirmation(),
                    null
            );
        } else {
            return new UrlBuilder(
                    UrlBuilder.SCHOOL_PROFILE,
                    getSchoolId(),
                    getDatabaseState(),
                    getName(),
                    getPhysicalAddress(), 
                    LevelCode.createLevelCode(getLevelCode()),
                    false,
                    null
            );
        }
    }
}
