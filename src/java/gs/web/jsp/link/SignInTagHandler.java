/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: SignInTagHandler.java,v 1.4 2009/12/04 22:27:01 chriskimm Exp $
 */

package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * Links to a sign-in page.
 *
 * @author <a href="mailto:apeterson@greatschools.org">Andrew J. Peterson</a>
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
