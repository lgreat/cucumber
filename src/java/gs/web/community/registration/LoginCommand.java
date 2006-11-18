/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: LoginCommand.java,v 1.2 2006/11/18 00:08:13 aroy Exp $
 */
package gs.web.community.registration;

import gs.web.util.validator.EmailValidator;

/**
 * Backing object for #LoginController
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class LoginCommand implements EmailValidator.IEmail {
    private String _email;
    private String _redirect;
    private String _password;
    private boolean _rememberMe;

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

    public String getPassword() {
        return _password;
    }

    public void setPassword(String password) {
        _password = password;
    }

    public boolean isRememberMe() {
        return _rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        _rememberMe = rememberMe;
    }
}
