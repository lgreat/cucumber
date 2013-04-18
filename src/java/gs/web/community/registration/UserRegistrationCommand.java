package gs.web.community.registration;

import gs.data.state.State;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UserRegistrationCommand  {
    protected final Log _log = LogFactory.getLog(getClass());

    private String _email;
    private String _password;
    private String _confirmPassword;
    private State _state;
    private String _firstName;
    private String _lastName;
    private String _city;
    private String _screenName;
    private String _gender;
    private String facebookId;
    private boolean _terms;

    public UserRegistrationCommand() {
    }

    public String getConfirmPassword() {
        return _confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        _confirmPassword = confirmPassword;
    }

    public String getPassword() {
        return _password;
    }

    public void setPassword(String password) {
        _password = password;
    }

    public void setTerms(boolean b) {
        _terms = b;
    }

    public boolean getTerms() {
        return _terms;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

}