/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: SchoolsTagHandler.java,v 1.3 2006/06/02 00:15:34 aroy Exp $
 */

package gs.web.jsp.link;

import gs.data.geo.ICity;
import gs.data.school.LevelCode;
import gs.web.school.SchoolsController;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;

/**
 * Access to the schools page.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 * @todo add param districtid
 * @todo handle no city or no district -- go to browse state schools page
 * @todo school type restriction with multiple school types
 */
public class SchoolsTagHandler extends LinkTagHandler {

    private ICity _city;
    private LevelCode _levelCode;
    private String _schoolTypes;

    protected UrlBuilder createUrlBuilder() {
        if (_city != null) {
            UrlBuilder builder = new UrlBuilder(_city, UrlBuilder.SCHOOLS_IN_CITY, _levelCode, _schoolTypes);
            return builder;
        }
        return null;
    }

    public void setCity(ICity city) {
        _city = city;
    }

    public void setLevelCode(LevelCode levelCode) {
        _levelCode = levelCode;
    }

    public void setSchoolTypes(String schoolTypes) {
        _schoolTypes = schoolTypes;
    }
}
