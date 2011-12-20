package gs.web.school;

import gs.web.util.validator.EmailValidator;
import gs.data.state.State;

public class EspMembershipCommand implements EmailValidator.IEmail {
    private String _firstName;
    private String _lastName;
    private String _email;
    private String _userName;
    private String _password;
    private String _confirmPassword;
    private String _webPageUrl;
    private String _jobTitle;
    private State _state;
    private String _city;
    private Long _schoolId;


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

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        _email = email;
    }

    public String getUserName() {
        return _userName;
    }

    public void setUserName(String userName) {
        _userName = userName;
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

    public String getWebPageUrl() {
        return _webPageUrl;
    }

    public void setWebPageUrl(String webPageUrl) {
        _webPageUrl = webPageUrl;
    }

    public State getState() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }

    public String getCity() {
        return _city;
    }

    public void setCity(String city) {
        _city = city;
    }

    public Long getSchoolId() {
        return _schoolId;
    }

    public void setSchoolId(Long schoolId) {
        _schoolId = schoolId;
    }

    public String getJobTitle() {
        return _jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        _jobTitle = jobTitle;
    }
}