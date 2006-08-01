package gs.web.community.registration;

import gs.data.community.User;
import gs.data.community.UserProfile;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class FollowUpCommand {
    private User _user;
    private UserProfile _userProfile;

    public FollowUpCommand() {
        _user = new User();
        _userProfile = new UserProfile();
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

    public boolean getPrivate() {
        return getUserProfile().isPrivate();
    }
}
