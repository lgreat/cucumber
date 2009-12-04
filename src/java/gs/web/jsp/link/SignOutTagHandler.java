/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: SignOutTagHandler.java,v 1.3 2009/12/04 22:27:02 chriskimm Exp $
 */

package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * Links to a sign-in page.
 *
 * @author <a href="mailto:apeterson@greatschools.org">Andrew J. Peterson</a>
 */
public class SignOutTagHandler extends LinkTagHandler {
    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.SIGN_OUT, getState());
    }
}
