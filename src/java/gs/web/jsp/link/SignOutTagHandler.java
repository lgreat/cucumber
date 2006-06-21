/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: SignOutTagHandler.java,v 1.1 2006/06/21 22:34:16 apeterson Exp $
 */

package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * Links to a sign-in page.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class SignOutTagHandler extends LinkTagHandler {
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.SIGN_OUT, getState());
    }
}
