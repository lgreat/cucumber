package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * Provides ...
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ForgotPasswordTagHandler  extends LinkTagHandler {

    private String _email;

    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.FORGOT_PASSWORD, getState(), getEmail());
    }

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        _email = email;
    }
}
