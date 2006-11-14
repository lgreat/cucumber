package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * Provides link handling for links to the main registration page.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RegistrationTagHandler extends LinkTagHandler {
    private String _redirect;
    private String _email;

    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.REGISTRATION, null, _email, _redirect);
    }

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        _email = email;
    }

    public String getRedirect() {
        return _redirect;
    }

    public void setRedirect(String redirect) {
        _redirect = redirect;
    }
}
