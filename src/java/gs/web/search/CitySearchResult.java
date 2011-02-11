package gs.web.search;

import gs.data.search.indexers.documentBuilders.CityDocumentBuilder;
import gs.data.state.State;
import gs.data.state.StateManager;
import org.apache.solr.client.solrj.beans.Field;

import java.util.ArrayList;

public class CitySearchResult implements ICitySearchResult {

    private String _city;

    private State _state;

    public String getCity() {
        return _city;
    }

    public void setCity(String city) {
        _city = city;
    }

    @Field(CityDocumentBuilder.CITY_NAME)
    public void setCity(ArrayList<String> cities) {
        _city = cities.get(0);
    }

    public State getState() {
        return _state;
    }

    @Field(CityDocumentBuilder.STATE)
    public void setState(ArrayList<String> states) {
        _state = new StateManager().getState(states.get(0));
    }

    public void setState(State state) {
        _state = state;
    }
}
