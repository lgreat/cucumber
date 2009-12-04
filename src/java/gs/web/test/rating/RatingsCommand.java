/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: RatingsCommand.java,v 1.9 2009/12/04 22:27:13 chriskimm Exp $
 */
package gs.web.test.rating;

import gs.data.school.School;
import gs.data.state.State;
import gs.web.util.validator.SchoolIdValidator;
import gs.web.util.validator.StateValidator;

/**
 * Ratings
 *
 * @author David Lee <mailto:dlee@greatschools.org>
 */
public final class RatingsCommand implements SchoolIdValidator.ISchoolId, StateValidator.IState {
    private int _id;
    private int _schoolId;
    private State _state;
    private IRatingsDisplay _ratingsDisplay;
    private School _school;
    private IRatingsDisplay.IRowGroup.IRow.ICell _overallRating;
    private int _ratingYear;


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

    public IRatingsDisplay.IRowGroup.IRow.ICell getOverallRating() {
        return _overallRating;
    }

    public void setOverallRating(IRatingsDisplay.IRowGroup.IRow.ICell overallRating) {
        _overallRating = overallRating;
    }


    public int getRatingYear() {
        return _ratingYear;
    }

    public void setRatingYear(int ratingYear) {
        _ratingYear = ratingYear;
    }
}
