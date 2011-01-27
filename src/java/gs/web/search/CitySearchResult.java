package gs.web.search;

import gs.data.search.indexers.documentBuilders.CityDocumentBuilder;
import gs.data.state.State;
import org.apache.solr.client.solrj.beans.Field;

public class CitySearchResult implements ICitySearchResult {
    @Field(CityDocumentBuilder.CITY_NAME)
    private String _city;

    @Field(CityDocumentBuilder.STATE)
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
