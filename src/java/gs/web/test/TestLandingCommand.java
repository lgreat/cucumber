package gs.web.test;

import gs.data.state.State;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class TestLandingCommand {
    public State _state;
    public Integer _school;

    public State getState() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }

    public Integer getSchool() {
        return _school;
    }

    public void setSchool(Integer school) {
        _school = school;
    }
}
