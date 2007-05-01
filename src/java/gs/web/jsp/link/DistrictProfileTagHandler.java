package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import gs.data.state.State;

/**
 * @author Anthony Roy (aroy@greatschools.net)
 */
public class DistrictProfileTagHandler extends LinkTagHandler {
    private Integer _districtId;
    private Integer _schoolId;
    private State _state;

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder urlBuilder =
                new UrlBuilder(UrlBuilder.DISTRICT_PROFILE, getState(), String.valueOf(_districtId));
        if (_schoolId != null) {
            urlBuilder.addParameter("schoolId", String.valueOf(_schoolId));
        }
        return urlBuilder;
    }

    public Integer getDistrictId() {
        return _districtId;
    }

    public void setDistrictId(Integer districtId) {
        _districtId = districtId;
    }

    public Integer getSchoolId() {
        return _schoolId;
    }

    public void setSchoolId(Integer schoolId) {
        _schoolId = schoolId;
    }

    public State getState() {
        if (_state != null) {
            return _state;
        }
        return super.getState();
    }

    public void setState(State state) {
        _state = state;
    }
}
