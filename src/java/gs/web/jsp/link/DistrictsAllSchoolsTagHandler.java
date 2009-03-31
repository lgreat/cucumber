package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;

/**
 * All schools in a district
 *
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class DistrictsAllSchoolsTagHandler extends LinkTagHandler {
    private Integer _districtId;
    private String _levelCode;

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_DISTRICT, getState(), getDistrictId().toString());
        if (StringUtils.isNotBlank(_levelCode)) {
            builder.setParameter("lc", _levelCode);
        }
        
        return builder;
    }

    public Integer getDistrictId() {
        return _districtId;
    }

    public void setDistrictId(Integer districtId) {
        _districtId = districtId;
    }
    public String getLevelCode() {
        return _levelCode;
    }

    public void setLevelCode(String levelCode) {
        _levelCode = levelCode;
    }
}
