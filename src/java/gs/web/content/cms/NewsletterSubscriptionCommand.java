package gs.web.content.cms;

import gs.web.util.validator.EmailValidator;

public class NewsletterSubscriptionCommand implements EmailValidator.IEmail {
    private String _email;
    private boolean _partnerNewsletter;
    private boolean _ajaxRequest;
    private boolean _nlSignUpFromHomePage;

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        _email = email;
    }

    public boolean isPartnerNewsletter() {
        return _partnerNewsletter;
    }

    public void setPartnerNewsletter(boolean partnerNewsletter) {
        _partnerNewsletter = partnerNewsletter;
    }

    public boolean isAjaxRequest() {
        return _ajaxRequest;
    }

    public void setAjaxRequest(boolean ajaxRequest) {
        _ajaxRequest = ajaxRequest;
    }

    public boolean isNlSignUpFromHomePage() {
        return _nlSignUpFromHomePage;
    }

    public void setNlSignUpFromHomePage(boolean nlSignUpFromHomePage) {
        _nlSignUpFromHomePage = nlSignUpFromHomePage;
    }
}