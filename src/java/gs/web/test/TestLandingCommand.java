package gs.web.test;

import gs.data.state.State;
import gs.data.school.School;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class TestLandingCommand {

    public State _state;
    public School _school;

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
