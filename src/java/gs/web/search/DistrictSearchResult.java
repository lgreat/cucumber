package gs.web.search;

import gs.data.state.State;

public class DistrictSearchResult implements IDistrictSearchResult {
    private String _district;
    private State _state;

    public String getDistrict() {
        return _district;
    }

    public void setDistrict(String district) {
        _district = district;
    }

    public State getState() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }
}
