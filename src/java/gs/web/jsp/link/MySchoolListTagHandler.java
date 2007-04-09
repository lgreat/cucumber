/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: MySchoolListTagHandler.java,v 1.2 2007/04/09 20:35:17 dlee Exp $
 */

package gs.web.jsp.link;

import gs.data.school.School;
import gs.web.util.UrlBuilder;

/**
 * Generates My School List tag.
 *
 * @author Andrew Peterson <mailto:apeterson@greatschools.net>
 */
public class MySchoolListTagHandler extends LinkTagHandler {

    private School _school;

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder;
        if (null == getSchool()) {
            builder = new UrlBuilder(UrlBuilder.MY_SCHOOL_LIST, getState());
        } else {
            builder = new UrlBuilder(UrlBuilder.MY_SCHOOL_LIST, getSchool().getDatabaseState(), getSchool().getId().toString());
        }
        return builder;
    }

    protected String getDefaultLinkText() {
        return "My School List";
    }

    public School getSchool() {
        return _school;
    }

    public void setSchool(School school) {
        _school = school;
    }
}
