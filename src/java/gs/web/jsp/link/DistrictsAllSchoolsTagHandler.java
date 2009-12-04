package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import gs.data.school.district.District;
import org.apache.commons.lang.StringUtils;

/**
 * All schools in a district
 *
 * @author <a href="mailto:dlee@greatschools.org">David Lee</a>
 */
public class DistrictsAllSchoolsTagHandler extends LinkTagHandler {
    private String _levelCode;
    private District _district;

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(_district, UrlBuilder.SCHOOLS_IN_DISTRICT);
        if (StringUtils.isNotBlank(_levelCode)) {
            builder.setParameter("lc", _levelCode);
        }
        
        return builder;
    }

    public District getDistrict() {
        return _district;
    }

    public void setDistrict(District district) {
        _district = district;
    }

    public String getLevelCode() {
        return _levelCode;
    }

    public void setLevelCode(String levelCode) {
        _levelCode = levelCode;
    }
}
