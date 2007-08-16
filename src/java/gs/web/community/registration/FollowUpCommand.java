package gs.web.community.registration;

import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.community.Student;
import gs.data.community.Subscription;
import gs.data.state.State;
import gs.data.geo.City;
import gs.data.school.School;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class FollowUpCommand {
    protected final Log _log = LogFactory.getLog(getClass());
    private User _user;
    private UserProfile _userProfile;
    private List<Student> _students;
    private String _marker;
    private List<String> _schoolNames;
    private List<String> _cityNames;
    private List<List<City>> _cityList;
    private List<String> _previousSchoolNames;
    private List<Subscription> _subscriptions;
    private String _recontact;
    private List<List<School>> _schools;
    private boolean _terms = true;
    private String _redirect;
    private boolean _newsletter = true;
    private boolean _beta = false;
    private State _state;

    public FollowUpCommand() {
        _user = new User();
        _userProfile = new UserProfile();
        _students = new ArrayList<Student>();
        _schoolNames = new ArrayList<String>();
        _schools = new ArrayList<List<School>>();
        _cityList = new ArrayList<List<City>>();
        _cityNames = new ArrayList<String>();
        _previousSchoolNames = new ArrayList<String>();
        _subscriptions = new ArrayList<Subscription>();
    }

    public String getRedirect() {
        return _redirect;
    }

    public void setRedirect(String redirect) {
        _redirect = redirect;
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

    public void setUserProfile(UserProfile userProfile) {
        _userProfile = userProfile;
    }

    public void setId(Integer id) {
        getUser().setId(id);
        getUserProfile().setId(id);
    }

    public Integer getId() {
        return getUser().getId();
    }

    public List<String> getCityNames() {
        return _cityNames;
    }

    public void setCityNames(List<String> cityNames) {
        _cityNames = cityNames;
    }

    public void addCityName(String cityName) {
        _cityNames.add(cityName);
    }

    public List<List<City>> getCityList() {
        return _cityList;
    }

    public void addCityList(List<City> list) {
        getCityList().add(list);
    }

    public void setCityList(List<List<City>> cityList) {
        _cityList = cityList;
    }

    public void setAboutMe(String aboutMe) {
        getUserProfile().setAboutMe(aboutMe);
    }

    public String getAboutMe() {
        return getUserProfile().getAboutMe();
    }

    public void setPrivate(boolean isPrivate) {
        getUserProfile().setPrivate(isPrivate);
    }

    public boolean isPrivate() {
        return getPrivate();
    }

    public boolean getPrivate() {
        return getUserProfile().isPrivate();
    }

    public String getMarker() {
        return _marker;
    }

    public void setMarker(String marker) {
        _marker = marker;
    }

    public List<Student> getStudents() {
        return _students;
    }

    public void setStudents(List<Student> students) {
        _students = students;
    }

    public void addStudent(Student student) {
        getStudents().add(student);
    }

    public int getNumStudents() {
        return getStudents().size();
    }

    public void setNumStudents(int num) {
        // ignore -- this is so JSTL treats this as a bean property
    }

    public List<String> getSchoolNames() {
        return _schoolNames;
    }

    public void setSchoolNames(List<String> schoolNames) {
        _schoolNames = schoolNames;
    }

    public void addSchoolName(String name) {
        getSchoolNames().add(name);
    }

    public List getInterestsAsList() {
        return Arrays.asList(getUserProfile().getInterestsAsArray());
    }

    public String getOtherInterest() {
        return getUserProfile().getOtherInterest();
    }

    public void setOtherInterest(String other) {
        getUserProfile().setOtherInterest(other);
    }

    public void addSubscription(Subscription sub) {
        getSubscriptions().add(sub);
    }

    public List<Subscription> getSubscriptions() {
        return _subscriptions;
    }

    public void setSubscriptions(List<Subscription> subscriptions) {
        _subscriptions = subscriptions;
    }

    public int getNumSubscriptions() {
        return getSubscriptions().size();
    }

    public List<String> getPreviousSchoolNames() {
        return _previousSchoolNames;
    }

    public void setPreviousSchoolNames(List<String> previousSchoolNames) {
        _previousSchoolNames = previousSchoolNames;
    }

    public void addPreviousSchoolName(String name) {
        getPreviousSchoolNames().add(name);
    }

    public String getRecontact() {
        return _recontact;
    }

    public void setRecontact(String recontact) {
        _recontact = recontact;
    }

    public void addSchools(List<School> schools) {
        _schools.add(schools);
    }

    public List<List<School>> getSchools() {
        return _schools;
    }

    public boolean getTerms() {
        return _terms;
    }

    public void setTerms(boolean terms) {
        _terms = terms;
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

    public void setState(State state) {
        _state = state;
    }

    public State getState() {
        return _state;
    }
}
