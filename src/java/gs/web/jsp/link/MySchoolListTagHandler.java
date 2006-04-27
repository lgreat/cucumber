/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: MySchoolListTagHandler.java,v 1.1 2006/04/27 22:53:47 apeterson Exp $
 */

package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * Generates My School List tag.
 *
 * @author Andrew Peterson <mailto:apeterson@greatschools.net>
 */
public class MySchoolListTagHandler extends LinkTagHandler {

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.MY_SCHOOL_LIST, getState());
        return builder;
    }

    protected String getDefaultLinkText() {
        return "My School List";
    }

}
