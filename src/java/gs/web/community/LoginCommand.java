/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: LoginCommand.java,v 1.3 2009/12/04 22:15:11 npatury Exp $
 */
package gs.web.community;

import gs.web.util.validator.EmailValidator;

/**
 * Backing object for #LoginController
 *
 * @author David Lee <mailto:dlee@greatschools.org>
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
