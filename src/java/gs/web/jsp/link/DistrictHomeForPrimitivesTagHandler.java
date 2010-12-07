package gs.web.jsp.link;

import gs.data.school.district.District;
import gs.data.state.State;
import gs.web.util.UrlBuilder;

/**
 * @author Samson Sprouse (ssprouse@greatschools.org)
 */
public class DistrictHomeForPrimitivesTagHandler extends LinkTagHandler {
    private Integer _schoolId;

    private State _state;

    private Integer _districtId;

    private String _name;

    private String _city;

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder urlBuilder = new UrlBuilder(getState(), getDistrictId(), getName(), getCity(), UrlBuilder.DISTRICT_HOME);
        if (_schoolId != null) {
            urlBuilder.addParameter("schoolId", String.valueOf(_schoolId));
        }
        return urlBuilder;
    }

    public Integer getSchoolId() {
        return _schoolId;
    }

    public void setSchoolId(Integer schoolId) {
        _schoolId = schoolId;
    }

    public Integer getDistrictId() {
        return _districtId;
    }

    public void setDistrictId(Integer districtId) {
        _districtId = districtId;
    }

    public State getState() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getCity() {
        return _city;
    }

    public void setCity(String city) {
        _city = city;
    }
}