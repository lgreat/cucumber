package gs.web.community.registration;

import gs.web.util.validator.EmailValidator;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.state.State;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * @author <a href="aroy@greatschools.net">Anthony Roy</a>
 */
public class UserCommand implements EmailValidator.IEmail {
    protected final Log _log = LogFactory.getLog(getClass());

    private String _confirmPassword;
    private String _password;
    private String _confirmEmail;
    private User _user;
    private UserProfile _userProfile;
    private String _redirectUrl;
    private String _referrer;
    private List _cityList;
    private List _schoolChoiceCityList;
    private boolean _recontact;
    private boolean _terms = true;
    private boolean _newsletter = true;
    private boolean _beta = false;
    private boolean _parentAdvisorNewsletter;

    private boolean _chooserRegistration = false;

    public UserCommand() {
        _user = new User();
        _userProfile = new UserProfile();
    }

    public List getCityList() {
        return _cityList;
    }

    public void setCityList(List cityList) {
        _cityList = cityList;
    }

    public List getSchoolChoiceCityList() {
        return _schoolChoiceCityList;
    }

    public void setSchoolChoiceCityList(List cityList) {
        _schoolChoiceCityList = cityList;
    }

    public String getRedirectUrl() {
        return _redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this._redirectUrl = redirectUrl;
    }

    public String getConfirmPassword() {
        return _confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        _confirmPassword = confirmPassword;
    }

    /**
     * NOT a passthrough to the user object!!
     * @return password
     */
    public String getPassword() {
        return _password;
    }

    /**
     * NOT a passthrough to the user object!!
     * @param password
     */
    public void setPassword(String password) {
        _password = password;
    }

    public User getUser() {
        return _user;
    }

    public void setUser(User user) {
        _user = user;
    }

    public UserProfile getUserProfile() {
        return _userProfile;
    }

    public void setUserProfile(UserProfile user) {
        _userProfile = user;
    }

    /**
     * Pass through method to getUser().getEmail
     * @return email
     */
    public String getEmail() {
        return getUser().getEmail();
    }

    /**
     * Pass through method to getUser().setEmail
     * @param email
     */
    public void setEmail(String email) {
        getUser().setEmail(StringUtils.trim(email));
    }

    public String getId() {
        return String.valueOf(getUser().getId());
    }

    public void setId(String id) {
        try {
            getUser().setId(new Integer(Integer.parseInt(id)));
        } catch (NumberFormatException _nfe) {
            // ignore - this is an expected case for new users
        }
    }

    public String getConfirmEmail() {
        return _confirmEmail;
    }

    public void setConfirmEmail(String confirmEmail) {
        _confirmEmail = confirmEmail;
    }

    public String getFirstName() {
        return getUser().getFirstName();
    }

    public void setFirstName(String firstName) {
        getUser().setFirstName(firstName);
    }

    public String getLastName() {
        return getUser().getLastName();
    }

    public void setLastName(String lastName) {
        getUser().setLastName(lastName);
    }

    public State getState() {
        return getUserProfile().getState();
    }

    public void setState(State state) {
        getUserProfile().setState(state);
    }

    public State getSchoolChoiceState() {
        return getUserProfile().getSchoolChoiceState();
    }

    public void setSchoolChoiceState(State state) {
        getUserProfile().setSchoolChoiceState(state);
    }

    public String getCity() {
        return getUserProfile().getCity();
    }

    public void setCity(String city) {
        getUserProfile().setCity(city);
    }

    public String getSchoolChoiceCity() {
        return getUserProfile().getSchoolChoiceCity();
    }

    public void setSchoolChoiceCity(String city) {
        getUserProfile().setSchoolChoiceCity(city);
    }

    public Integer getNumSchoolChildren() {
        return getUserProfile().getNumSchoolChildren();
    }

    public void setNumSchoolChildren(Integer numChildren) {
        getUserProfile().setNumSchoolChildren(numChildren);
    }

    public Integer getNumPreKChildren() {
        return getUserProfile().getNumPreKChildren();
    }

    public void setNumPreKChildren(Integer numYoungChildren) {
        getUserProfile().setNumPreKChildren(numYoungChildren);
    }

    public String getScreenName() {
        return getUserProfile().getScreenName();
    }

    public void setScreenName(String screenName) {
        getUserProfile().setScreenName(screenName);
    }

    public boolean isRecontact() {
        return _recontact;
    }

    public void setRecontact(boolean recontact) {
        _recontact = recontact;
    }

    public String getGender() {
        return getUser().getGender();
    }

    public void setGender(String gender) {
        getUser().setGender(gender);
    }

    public void setTerms(boolean b) {
        _terms = b;
    }

    public boolean getTerms() {
        return _terms;
    }

    public boolean getNewsletter() {
        return _newsletter;
    }

    public void setNewsletter(boolean newsletter) {
        _newsletter = newsletter;
    }

    public boolean isBeta() {
        return _beta;
    }

    public void setBeta(boolean beta) {
        _beta = beta;
    }

    public String getReferrer() {
        return _referrer;
    }

    public void setReferrer(String referrer) {
        _referrer = referrer;
    }

    public boolean isChooserRegistration() {
        return _chooserRegistration;
    }

    public void setChooserRegistration(boolean chooserRegistration) {
        _chooserRegistration = chooserRegistration;
    }
    
    public boolean getParentAdvisorNewsletter() {
        return _parentAdvisorNewsletter;
    }

    public void setParentAdvisorNewsletter(boolean parentAdvisorNewsletter) {
        _parentAdvisorNewsletter = parentAdvisorNewsletter;
    }    

}
