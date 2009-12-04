/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: BaseSchoolTagHandler.java,v 1.3 2009/12/04 20:54:11 npatury Exp $
 */
package gs.web.jsp.link.school;

import gs.data.school.School;
import gs.web.jsp.link.LinkTagHandler;
import gs.web.util.UrlBuilder;

/**
 * The purpose is ...
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public abstract class BaseSchoolTagHandler extends LinkTagHandler {
    private School _school;

    protected abstract UrlBuilder createUrlBuilder();

    public School getSchool() {
        return _school;
    }

    public void setSchool(School school) {
        _school = school;
    }
}
