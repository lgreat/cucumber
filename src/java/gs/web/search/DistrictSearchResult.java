package gs.web.search;

import gs.data.search.indexers.documentBuilders.DistrictDocumentBuilder;
import gs.data.state.State;
import gs.data.state.StateManager;
import org.apache.solr.client.solrj.beans.Field;

public class DistrictSearchResult implements IDistrictSearchResult {

    @Field(DistrictDocumentBuilder.ID)
    private Integer _id;

    private State _state;
    @Field(DistrictDocumentBuilder.DISTRICT_NAME)
    private String _name;
    @Field(DistrictDocumentBuilder.ADDRESS_CITY)
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

    @Field(DistrictDocumentBuilder.ADDRESS_STATE)
    public void setState(String state) {
        _state = new StateManager().getState(state);
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
