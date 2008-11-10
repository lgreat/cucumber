package gs.web.community.registration.popup;

import gs.web.util.validator.EmailValidator;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ForgotPasswordHoverCommand implements EmailValidator.IEmail {
    private String _email;
    private boolean _msl;

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        _email = email;
    }

    public boolean isMsl() {
        return _msl;
    }

    public void setMsl(boolean msl) {
        _msl = msl;
    }
}
