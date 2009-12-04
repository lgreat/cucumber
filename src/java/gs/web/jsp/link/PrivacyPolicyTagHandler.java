/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: PrivacyPolicyTagHandler.java,v 1.2 2009/12/04 20:54:10 npatury Exp $
 */
package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * The purpose is ...
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class PrivacyPolicyTagHandler extends LinkTagHandler{
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.PRIVACY_POLICY, getState());
        return builder;
    }
}
