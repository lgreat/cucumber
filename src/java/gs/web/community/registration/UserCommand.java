package gs.web.community.registration;

import gs.web.util.validator.EmailValidator;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.community.Student;
import gs.data.community.Subscription;
import gs.data.state.State;
import gs.data.school.School;
import gs.data.school.Grade;
import gs.data.geo.City;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.ArrayList;

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
    private List<Subscription> _subscriptions;
    private List<StudentCommand> _studentRows;
    private boolean _recontact;
    private boolean _terms = false;
    private boolean _newsletter = false;
    private boolean _partnerNewsletter = false;
    private boolean _chooserRegistration = false;

    public UserCommand() {
        _user = new User();
        _userProfile = new UserProfile();
        // TODO: add userprofile to user?
        
        _studentRows = new ArrayList<StudentCommand>();
        _subscriptions = new ArrayList<Subscription>();
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

    public boolean getPartnerNewsletter() {
        return _partnerNewsletter;
    }

    public void setPartnerNewsletter(boolean partnerNewsletter) {
        _partnerNewsletter = partnerNewsletter;
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

    public void setNumStudents(int num) {
        // ignore -- this is so JSTL treats this as a bean property
    }

    public List<Subscription> getSubscriptions() {
        return _subscriptions;
    }

    public void setSubscriptions(List<Subscription> subscriptions) {
        _subscriptions = subscriptions;
    }

    public void addSubscription(Subscription sub) {
        getSubscriptions().add(sub);
    }

    public int getNumSubscriptions() {
        return getSubscriptions().size();
    }

    public List<StudentCommand> getStudentRows() {
        return _studentRows;
    }

    public void setStudentRows(List<StudentCommand> studentRows) {
        _studentRows = studentRows;
    }

    public void addStudentRow(StudentCommand studentCommand) {
        _studentRows.add(studentCommand);
    }

    public int getNumStudentRows() {
        return _studentRows.size();
    }

    protected static class StudentCommand {
        private int _schoolIdSelected;
        private Grade _gradeSelected;
        private State _stateSelected;
        private String _citySelected;
        private List<City> _cities;
        private List<School> _schools;
        private boolean _locationOverride;

        public StudentCommand() {
            _schoolIdSelected = -1;
            _locationOverride = false;
        }
        
        public int getSchoolIdSelected() {
            return _schoolIdSelected;
        }

        public void setSchoolIdSelected(int schoolIdSelected) {
            _schoolIdSelected = schoolIdSelected;
        }

        public Grade getGradeSelected() {
            return _gradeSelected;
        }

        public void setGradeSelected(Grade gradeSelected) {
            _gradeSelected = gradeSelected;
        }

        public State getStateSelected() {
            return _stateSelected;
        }

        public void setStateSelected(State stateSelected) {
            _stateSelected = stateSelected;
        }

        public String getCitySelected() {
            return _citySelected;
        }

        public void setCitySelected(String citySelected) {
            _citySelected = citySelected;
        }

        public List<City> getCities() {
            return _cities;
        }

        public void setCities(List<City> cities) {
            _cities = cities;
        }

        public List<School> getSchools() {
            return _schools;
        }

        public void setSchools(List<School> schools) {
            _schools = schools;
        }

        public boolean isLocationOverride() {
            return _locationOverride;
        }

        public void setLocationOverride(boolean locationOverride) {
            _locationOverride = locationOverride;
        }

        public String toString() {
            StringBuffer str = new StringBuffer("StudentCommand{");
            if (_locationOverride) {
                str.append("state: " + _stateSelected + ", city: " + _citySelected + ", ");
            }
            str.append("grade: " + _gradeSelected + ", school: " + _schoolIdSelected);
            str.append("}");
            
            return str.toString();
        }
    }
}
