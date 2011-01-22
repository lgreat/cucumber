package gs.web.search;

import gs.data.state.State;
import org.apache.solr.client.solrj.beans.Field;

public class CitySearchResult implements ICitySearchResult {
    @Field("city_name")
    private String _city;

    @Field("city_state")
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
