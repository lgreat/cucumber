package gs.web.community.registration;

import gs.web.util.validator.EmailValidator;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ForgotPasswordCommand implements EmailValidator.IEmail {
    private String _email;
    private boolean _msl;
    private String _referrer;

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

    public String getReferrer() {
        return _referrer;
    }

    public void setReferrer(String referrer) {
        _referrer = referrer;
    }
}
