/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: PrivacyPolicyTagHandler.java,v 1.3 2009/12/04 22:27:02 chriskimm Exp $
 */
package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * The purpose is ...
 *
 * @author David Lee <mailto:dlee@greatschools.org>
 */
public class PrivacyPolicyTagHandler extends LinkTagHandler{
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.PRIVACY_POLICY, getState());
        return builder;
    }
}
