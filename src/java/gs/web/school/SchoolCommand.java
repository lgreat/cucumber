package gs.web.school;

import gs.data.state.State;
import gs.data.school.School;
import gs.web.util.validator.SchoolIdValidator;
import gs.web.util.validator.StateValidator;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SchoolCommand implements SchoolIdValidator.ISchoolId, StateValidator.IState {
    
    protected int _schoolId;
    protected State _state;
    protected School _school;

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

    public School getSchool() {
        return _school;
    }

    public void setSchool(School school) {
        _school = school;
    }


}
