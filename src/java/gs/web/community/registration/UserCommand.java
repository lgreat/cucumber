package gs.web.community.registration;

import gs.web.util.validator.EmailValidator;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.community.Subscription;
import gs.data.community.SubscriptionProduct;
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
 * This command is shared by many join flows. Not every field is used in every flow.
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
    private List<Subscription> _subscriptions;
    private List<StudentCommand> _studentRows;
    private boolean _recontact;
    private boolean _terms = false;
    private boolean _newsletter = false;
    private boolean _partnerNewsletter = false;
    private boolean _ldNewsletter = false;
    private boolean _brainDrainNewsletter = false;
//    private boolean _weeklyUpdatesNewsletter = false;
    private String _startweek;
    private boolean _chooserRegistration = false;    

    // following four fields used by SchoolInterruptRegistrationHoverController
    private boolean _mystat = true;
    private String _mystatSchoolName;
    private int _mystatSchoolId;
    private State _mystatSchoolState;

    // back to school tips
    private boolean _btsTip = false;
    private String btsTipVersion = null;

    private String facebookId = null;

    public boolean isBtsTip() {
        return _btsTip;
    }

    public void setBtsTip(boolean btsTip) {
        _btsTip = btsTip;
    }

    public String getBtsTipVersion() {
        return btsTipVersion;
    }

    public void setBtsTipVersion(String btsTipVersion) {
        this.btsTipVersion = btsTipVersion;
    }

    public List<NthGraderSubscription> getGradeNewsletters() {
        return _gradeNewsletters;
    }

    public void setGradeNewsletters(List<NthGraderSubscription> gradeNewsletters) {
        _gradeNewsletters = gradeNewsletters;
    }//following list is used by NthGraderHover for the grade by grade newsletters.
    private List<NthGraderSubscription> _gradeNewsletters;

    public UserCommand() {
        _user = new User();
        _userProfile = new UserProfile();
        
        _studentRows = new ArrayList<StudentCommand>();
        _subscriptions = new ArrayList<Subscription>();
        _gradeNewsletters = new ArrayList<NthGraderSubscription>();
    }

    public List getCityList() {
        return _cityList;
    }

    public void setCityList(List cityList) {
        _cityList = cityList;
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
            getUser().setId(Integer.parseInt(id));
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

    public String getCity() {
        return getUserProfile().getCity();
    }

    public void setCity(String city) {
        getUserProfile().setCity(city);
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

    public boolean getLdNewsletter() {
        return _ldNewsletter;
    }

    public void setLdNewsletter(boolean ldNewsletter) {
        _ldNewsletter = ldNewsletter;
    }

     public boolean getBrainDrainNewsletter() {
        return _brainDrainNewsletter;
    }

    public void setBrainDrainNewsletter(boolean brainDrainNewsletter) {
        _brainDrainNewsletter = brainDrainNewsletter;
    }

     public String getStartweek() {
        return _startweek;
    }

    public void setStartweek(String startweek) {
        this._startweek = startweek;
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

    public boolean isMystat() {
        return _mystat;
    }

    public void setMystat(boolean mystat) {
        _mystat = mystat;
    }

    public String getMystatSchoolName() {
        return _mystatSchoolName;
    }

    public void setMystatSchoolName(String mystatSchoolName) {
        _mystatSchoolName = mystatSchoolName;
    }

    public int getMystatSchoolId() {
        return _mystatSchoolId;
    }

    public void setMystatSchoolId(int mystatSchoolId) {
        _mystatSchoolId = mystatSchoolId;
    }

    public State getMystatSchoolState() {
        return _mystatSchoolState;
    }

    public void setMystatSchoolState(State mystatSchoolState) {
        _mystatSchoolState = mystatSchoolState;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public static class NthGraderSubscription{

        private boolean _checked;
        private SubscriptionProduct _subProduct;

        public NthGraderSubscription(boolean chk, SubscriptionProduct sub) {
            _checked = chk;
            _subProduct = sub;
        }

        public boolean getChecked() {
            return _checked;
        }

        public void setChecked(boolean checked) {
            _checked = checked;
        }

        public SubscriptionProduct getSubProduct() {
            return _subProduct;
        }
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
                str.append("state: ").append(_stateSelected).append(", city: ").append(_citySelected).append(", ");
            }
            str.append("grade: ").append(_gradeSelected).append(", school: ").append(_schoolIdSelected);
            str.append("}");
            
            return str.toString();
        }
    }
}