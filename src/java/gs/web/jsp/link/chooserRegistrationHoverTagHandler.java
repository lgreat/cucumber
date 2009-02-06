package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;

/**
 * Created by IntelliJ IDEA.
 * User: npatury
 * Date: Feb 5, 2009
 * Time: 10:59:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class chooserRegistrationHoverTagHandler extends LinkTagHandler {
    private String _redirect;
    private String _email;

    public chooserRegistrationHoverTagHandler() {
        super();
        setRel("nofollow");
    }

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.CHOOSER_REGISTRATION_HOVER, null, null);
        if (StringUtils.isNotEmpty(_redirect)) {
            urlBuilder.setParameter("redirect", _redirect);
        }
        if (StringUtils.isNotEmpty(_email)) {
            urlBuilder.setParameter("email", _email);
        }
        return urlBuilder;
    }

    public String getRedirect() {
        return _redirect;
    }

    public void setRedirect(String redirect) {
        _redirect = redirect;
    }

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        _email = email;
    }
}