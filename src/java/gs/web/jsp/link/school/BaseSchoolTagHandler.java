/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: BaseSchoolTagHandler.java,v 1.1 2006/10/12 23:58:04 dlee Exp $
 */
package gs.web.jsp.link.school;

import gs.data.school.School;
import gs.web.jsp.link.LinkTagHandler;

/**
 * The purpose is ...
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public abstract class BaseSchoolTagHandler extends LinkTagHandler {
    private School _school;

    public School getSchool() {
        return _school;
    }

    public void setSchool(School school) {
        _school = school;
    }
}
