package gs.web.community.registration;


import javax.servlet.http.HttpServletRequest;

public class RegistrationBehavior {
    private String _how;
    private String _fbSignedRequest;
    private String _redirectUrl;

    public boolean isFacebookRegistration() {
        return _fbSignedRequest != null;
    }

    // whether to send email verification email to the user
    public boolean requireEmailVerification() {
        if (isFacebookRegistration()) {
            return false;
        }
        return false;
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
}
