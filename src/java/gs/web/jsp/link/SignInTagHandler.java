/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: SignInTagHandler.java,v 1.2 2006/07/14 20:59:51 apeterson Exp $
 */

package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * Links to a sign-in page.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class SignInTagHandler extends LinkTagHandler {

    private String _forwardTo;

    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.SIGN_IN, getState());
    }

    public String getForwardTo() {
        return _forwardTo;
    }

    public void setForwardTo(String forwardTo) {
        _forwardTo = forwardTo;
    }
}
