package gs.web.search;

import gs.data.state.State;

public class DistrictSearchResult implements IDistrictSearchResult {

    private Integer _id;
    private State _state;
    private String _name;
    private String _city;

    public Integer getId() {
        return _id;
    }

    public void setId(Integer id) {
        _id = id;
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
