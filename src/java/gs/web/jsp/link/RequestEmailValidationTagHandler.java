package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RequestEmailValidationTagHandler extends LinkTagHandler {

    private String _email;

    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.REQUEST_EMAIL_VALIDATION, getState(), getEmail());
    }

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        _email = email;
    }
}
