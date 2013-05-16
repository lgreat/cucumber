package gs.web.community.registration;

import gs.data.community.User;

/**
 * A structure to reflect if a user was obtained from the session,or was logged in or was a new user created.
 */
public class UserStateStruct {
    private boolean isUserLoggedIn = false;
    private boolean isUserRegistered = false;
    private boolean isUserInSession = false;
    private boolean isVerificationEmailSent = false;
    private User user;

    public boolean isUserLoggedIn() {
        return isUserLoggedIn;
    }

    public void setUserLoggedIn(boolean userLoggedIn) {
        isUserLoggedIn = userLoggedIn;
    }

    public boolean isUserRegistered() {
        return isUserRegistered;
    }

    public void setUserRegistered(boolean userRegistered) {
        isUserRegistered = userRegistered;
    }

    public boolean isUserInSession() {
        return isUserInSession;
    }

    public void setUserInSession(boolean userInSession) {
        isUserInSession = userInSession;
    }

    public boolean isVerificationEmailSent() {
        return isVerificationEmailSent;
    }

    public void setVerificationEmailSent(boolean verificationEmailSent) {
        isVerificationEmailSent = verificationEmailSent;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}