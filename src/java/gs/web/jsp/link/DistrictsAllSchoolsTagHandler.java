package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * All schools in a district
 *
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class DistrictsAllSchoolsTagHandler extends LinkTagHandler {
    private Integer _districtId;

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_DISTRICT, getState(), getDistrictId().toString());
        return builder;
    }

    public Integer getDistrictId() {
        return _districtId;
    }

    public void setDistrictId(Integer districtId) {
        _districtId = districtId;
    }
}
