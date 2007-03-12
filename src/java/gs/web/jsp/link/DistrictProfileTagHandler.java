package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import gs.data.state.State;

/**
 * @author Anthony Roy (aroy@greatschools.net)
 */
public class DistrictProfileTagHandler extends LinkTagHandler {
    private Integer _districtId;
    private State _state;

    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.DISTRICT_PROFILE, getState(), String.valueOf(_districtId));
    }

    public Integer getDistrictId() {
        return _districtId;
    }

    public void setDistrictId(Integer districtId) {
        _districtId = districtId;
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
