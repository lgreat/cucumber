package gs.web.community.registration;

import gs.data.state.State;

import gs.data.validation.FieldMatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

@FieldMatch.List({
    @FieldMatch(first = "password", second = "confirmPassword", message = "Passwords do not match.")
})
public class UserRegistrationCommand  {
    protected final Log _log = LogFactory.getLog(getClass());

    @Email(message="Please enter a valid email address.")
    private String _email;

    @Size(min=2, max=14, message="Password should be 6-14 characters.")
    private String _password;

    @Size(min=2, max=14, message="Password should be 6-14 characters.")
    private String _confirmPassword;

    private State _state;

    @Size(min=2, max=24, message="First name must be 2-24 characters long.")
    @NotNull(message="First name must be 2-24 characters long.")
    private String _firstName;

    private String _lastName;
    private String _city;

    @NotNull(message="Username must be 6-14 characters.")
    @Size(min=6, max=14, message="Username must be 6-14 characters.")
    private String _screenName;

    private String _gender;
    private String facebookId;

    @AssertTrue
    private boolean _terms;

    @AssertTrue(message="Password should be 6-14 characters.")
    private boolean passwordOrFacebookId() {
        boolean valid = (_password != null || facebookId != null);
        return valid;
    }

    @NotNull(message="How field must be provided.")
    private String _how;

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        _email = email;
    }

    public String getPassword() {
        return _password;
    }

    public void setPassword(String password) {
        _password = password;
    }

    public String getConfirmPassword() {
        return _confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        _confirmPassword = confirmPassword;
    }

    public State getState() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }

    public String getFirstName() {
        return _firstName;
    }

    public void setFirstName(String firstName) {
        _firstName = firstName;
    }

    public String getLastName() {
        return _lastName;
    }

    public void setLastName(String lastName) {
        _lastName = lastName;
    }

    public String getCity() {
        return _city;
    }

    public void setCity(String city) {
        _city = city;
    }

    public String getScreenName() {
        return _screenName;
    }

    public void setScreenName(String screenName) {
        _screenName = screenName;
    }

    public String getGender() {
        return _gender;
    }

    public void setGender(String gender) {
        _gender = gender;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public boolean isTerms() {
        return _terms;
    }

    public void setTerms(boolean terms) {
        _terms = terms;
    }

    public String getHow() {
        return _how;
    }

    public void setHow(String how) {
        _how = how;
    }
}