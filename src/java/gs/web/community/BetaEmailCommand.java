package gs.web.community;

import gs.web.util.validator.EmailValidator.IEmail;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class BetaEmailCommand implements IEmail {

    private String _email;

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        _email = email;
    }
}
