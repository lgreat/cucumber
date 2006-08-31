package gs.web.community.registration;

import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.community.Student;
import gs.data.community.Subscription;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class FollowUpCommand {
    private User _user;
    private UserProfile _userProfile;
    private List _students;
    private String _marker;
    private List _schoolNames;
    private List _subscriptions;

    public FollowUpCommand() {
        _user = new User();
        _userProfile = new UserProfile();
        _students = new ArrayList();
        _schoolNames = new ArrayList();
        _subscriptions = new ArrayList();
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

    public List getStudents() {
        return _students;
    }

    public void setStudents(List students) {
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

    public List getSchoolNames() {
        return _schoolNames;
    }

    public void setSchoolNames(List schoolNames) {
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

    public List getSubscriptions() {
        return _subscriptions;
    }

    public void setSubscriptions(List subscriptions) {
        _subscriptions = subscriptions;
    }

    public int getNumSubscriptions() {
        return getSubscriptions().size();
    }
}
