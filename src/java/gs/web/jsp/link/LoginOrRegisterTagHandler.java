/**
 * Copyright (c) 2006 GreatSchools.net. All Rights Reserved.
 */
package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;

/**
 * Provides link handling for links to the community login page.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class LoginOrRegisterTagHandler extends LinkTagHandler {
    private String _redirect;
    private String _email;

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, null, null);
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
