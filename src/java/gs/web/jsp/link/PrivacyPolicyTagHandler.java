/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: PrivacyPolicyTagHandler.java,v 1.1 2006/05/03 19:34:42 dlee Exp $
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
