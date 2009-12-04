/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: MySchoolListTagHandler.java,v 1.5 2009/12/04 20:54:11 npatury Exp $
 */

package gs.web.jsp.link;

import gs.data.school.School;
import gs.web.util.UrlBuilder;

/**
 * Generates My School List tag.
 */
public class MySchoolListTagHandler extends LinkTagHandler {

    private School _school;
    private School _remove;

    public MySchoolListTagHandler() {
        super();
        setRel("nofollow");
    }

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder;
        if (null == getSchool()) {
            builder = new UrlBuilder(UrlBuilder.MY_SCHOOL_LIST);
        } else {
            builder = new UrlBuilder(UrlBuilder.MY_SCHOOL_LIST, getSchool().getDatabaseState(), getSchool().getId().toString());
        }

        if (_remove != null) {
            builder.addParameter("command", "remove");
            builder.addParameter("state", _remove.getDatabaseState().getAbbreviation());
            builder.addParameter("ids", String.valueOf(_remove.getId()));
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

    public School getRemove() {
        return _remove;
    }

    public void setRemove(School remove) {
        _remove = remove;
    }
}
