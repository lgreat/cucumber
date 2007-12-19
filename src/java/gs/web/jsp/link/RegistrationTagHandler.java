package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;

/**
 * Provides link handling for links to the main registration page.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RegistrationTagHandler extends LinkTagHandler {
    private String _redirect;
    private String _email;
    private String _cpn;

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION, null, _email, _redirect);
        if (!StringUtils.isEmpty(_cpn)) {
            builder.addParameter("cpn", _cpn);
        }
        return builder;
    }

    public void setEmail(String email) {
        _email = email;
    }

    public void setRedirect(String redirect) {
        _redirect = redirect;
    }

    public String getCpn() {
        return _cpn;
    }

    public void setCpn(String cpn) {
        _cpn = cpn;
    }
}
