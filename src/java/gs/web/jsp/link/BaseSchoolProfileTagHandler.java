/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: BaseSchoolProfileTagHandler.java,v 1.1 2006/09/29 23:22:55 dlee Exp $
 */
package gs.web.jsp.link;

import gs.data.school.School;

/**
 * The purpose is ...
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public abstract class BaseSchoolProfileTagHandler extends LinkTagHandler {
    private School _school;

    public School getSchool() {
        return _school;
    }

    public void setSchool(School school) {
        _school = school;
    }
}
