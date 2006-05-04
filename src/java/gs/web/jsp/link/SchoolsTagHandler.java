/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: SchoolsTagHandler.java,v 1.1 2006/05/04 07:13:57 apeterson Exp $
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
 * @todo school type restriction
 */
public class SchoolsTagHandler extends LinkTagHandler {

    private ICity _city;
    private LevelCode _levelCode;
    private String _schoolTypes;

    protected UrlBuilder createUrlBuilder() {
        if (_city != null) {
            UrlBuilder builder = new UrlBuilder(_city, UrlBuilder.SCHOOLS_IN_CITY);
            if (_levelCode != null) {
                builder.setParameter(SchoolsController.PARAM_LEVEL_CODE, _levelCode.getCommaSeparatedString());
                // TODO: make sure the mutiple levels work
            }
            if (StringUtils.isNotEmpty(_schoolTypes)) {
                String[] sts = StringUtils.split(_schoolTypes, ",");
                for (int i = 0; i < sts.length; i++) {
                    builder.addParameter(SchoolsController.PARAM_SCHOOL_TYPE, sts[i]);
                }
            }
            return builder;
        }
        return null;
    }

    public ICity getCity() {
        return _city;
    }

    public void setCity(ICity city) {
        _city = city;
    }

    public LevelCode getLevelCode() {
        return _levelCode;
    }

    public void setLevelCode(LevelCode levelCode) {
        _levelCode = levelCode;
    }

    public String getSchoolTypes() {
        return _schoolTypes;
    }

    public void setSchoolTypes(String schoolTypes) {
        _schoolTypes = schoolTypes;
    }
}
