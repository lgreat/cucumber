package gs.web.school;

import gs.web.util.validator.EmailValidator;
import gs.data.state.State;

public class EspRegistrationCommand implements EmailValidator.IEmail {
    private String _firstName;
    private String _lastName;
    private String _email;
    private String _screenName;
    private String _password;
    private String _confirmPassword;
    private String _webPageUrl;
    private String _jobTitle;
    private State _state;
    private String _city;
    private Integer _schoolId;
    private Boolean _optInMystat;
    private Boolean _optInPromos;

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

    public String getScreenName() {
        return _screenName;
    }

    public void setScreenName(String screenName) {
        _screenName = screenName;
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

    public Integer getSchoolId() {
        return _schoolId;
    }

    public void setSchoolId(Integer schoolId) {
        _schoolId = schoolId;
    }

    public String getJobTitle() {
        return _jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        _jobTitle = jobTitle;
    }

    public Boolean getOptInMystat() {
        return _optInMystat;
    }

    public void setOptInMystat(Boolean optInMystat) {
        this._optInMystat = optInMystat;
    }

    public Boolean getOptInPromos() {
        return _optInPromos;
    }

    public void setOptInPromos(Boolean optInPromos) {
        _optInPromos = optInPromos;
    }

}