package gs.web.community.registration;


import gs.data.school.School;

import javax.servlet.http.HttpServletRequest;

public class RegistrationBehavior {
    private String _how;
    private String _fbSignedRequest;
    private String _redirectUrl;

    public School _school;

    public boolean isFacebookRegistration() {
        return _fbSignedRequest != null;
    }

    // whether the user needs to verify email, and hence have a provisional password.
    public boolean requireEmailVerification() {
        if (isFacebookRegistration()) {
            return false;
        }
        return true;
    }

    // whether to send email verification email to the user
    public boolean sendVerificationEmail() {
        return true;
    }

    public boolean sendConfirmationEmail() {
        return true;
    }

    public String getRedirectUrl() {
        return _redirectUrl;
    }

    public void setHow(String how) {
        _how = how;
    }

    public void setFbSignedRequest(String fbSignedRequest) {
        _fbSignedRequest = fbSignedRequest;
    }

    public void setRedirectUrl(String redirectUrl) {
        _redirectUrl = redirectUrl;
    }

    public School getSchool() {
        return _school;
    }

    public void setSchool(School school) {
        _school = school;
    }
}
