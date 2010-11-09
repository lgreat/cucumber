package gs.web.search;

import gs.data.state.State;

public class CitySearchResult implements ICitySearchResult {
    private String _city;
    private State _state;

    public String getCity() {
        return _city;
    }

    public void setCity(String city) {
        _city = city;
    }

    public State getState() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }
}
