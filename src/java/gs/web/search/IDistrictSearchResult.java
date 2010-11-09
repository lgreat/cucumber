package gs.web.search;

import gs.data.state.State;

public interface IDistrictSearchResult extends ISearchResult {

    public String getDistrict();

    public State getState();
}
