/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: LoginCommand.java,v 1.1 2006/06/26 20:51:51 dlee Exp $
 */
package gs.web.community;

import gs.web.util.validator.EmailValidator;

/**
 * Backing object for #LoginController
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class LoginCommand implements EmailValidator.IEmail {
    private String _email;
    private String _redirect;

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
