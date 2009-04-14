package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import gs.data.state.State;
import gs.data.school.district.District;

/**
 * @author Dave Roy (droy@greatschools.net)
 */
public class DistrictHomeTagHandler extends LinkTagHandler {
    private Integer _schoolId;
    private District _district;
    private State _state;

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder urlBuilder =
                new UrlBuilder(_district, UrlBuilder.DISTRICT_HOME);
        if (_schoolId != null) {
            urlBuilder.addParameter("school_id", String.valueOf(_schoolId));
        }
        return urlBuilder;
    }

    public District getDistrict() {
        return _district;
    }

    public void setDistrict(District district) {
        _district = district;
    }

    public Integer getSchoolId() {
        return _schoolId;
    }

    public void setSchoolId(Integer schoolId) {
        _schoolId = schoolId;
    }
}