package gs.web.community.registration;


import gs.data.community.WelcomeMessageStatus;
import gs.data.school.School;

import javax.servlet.http.HttpServletRequest;

public class RegistrationOrLoginBehavior {
    private String _how;
    private String _fbSignedRequest;
    private String _redirectUrl;

    public School _school;

    private boolean _sendVerificationEmail = true;
    private boolean _sendConfirmationEmail = true;
    private WelcomeMessageStatus _welcomeMessageStatus;

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
        return _sendVerificationEmail;
    }

    public WelcomeMessageStatus getWelcomeMessageStatus() {
        return _welcomeMessageStatus;
    }

    public void setWelcomeMessageStatus(WelcomeMessageStatus welcomeMessageStatus) {
        _welcomeMessageStatus = welcomeMessageStatus;
    }

    public boolean sendConfirmationEmail() {
        return _sendConfirmationEmail;
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

    public void setSendVerificationEmail(boolean sendVerificationEmail) {
        _sendVerificationEmail = sendVerificationEmail;
    }

    public void setSendConfirmationEmail(boolean sendConfirmationEmail) {
        _sendConfirmationEmail = sendConfirmationEmail;
    }
}
