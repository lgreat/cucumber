/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: RatingsCommand.java,v 1.5 2006/10/02 23:02:42 dlee Exp $
 */
package gs.web.test.rating;

import gs.data.school.School;
import gs.data.state.State;
import gs.web.util.validator.SchoolIdValidator;
import gs.web.util.validator.StateValidator;

/**
 * Ratings
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public final class RatingsCommand implements SchoolIdValidator.ISchoolId, StateValidator.IState {
    private int _id;
    private int _schoolId;
    private State _state;
    private Integer _overallRating;
    private IRatingsDisplay _ratingsDisplay;
    private School _school;


    public int getId() {
        return _id;
    }

    public void setId(int id) {
        _id = id;
        _schoolId = id;
    }

    public int getSchoolId() {
        return _schoolId;
    }

    public void setSchoolId(int schoolId) {
        _schoolId = schoolId;
    }

    public State getState() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }

    public Integer getOverallRating() {
        return _overallRating;
    }

    public void setOverallRating(Integer overallRating) {
        _overallRating = overallRating;
    }

    public IRatingsDisplay getRatingsDisplay() {
        return _ratingsDisplay;
    }

    public void setRatingsDisplay(IRatingsDisplay ratingsDisplay) {
        _ratingsDisplay = ratingsDisplay;
    }

    public School getSchool() {
        return _school;
    }

    public void setSchool(School school) {
        _school = school;
    }

}
