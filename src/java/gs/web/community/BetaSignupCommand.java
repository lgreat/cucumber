package gs.web.community;

import gs.web.util.validator.EmailValidator.IEmail;
import gs.data.state.State;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class BetaSignupCommand implements IEmail {

    private String _email;
    private State _state;

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        _email = email;
    }

    public State getState() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }
}
