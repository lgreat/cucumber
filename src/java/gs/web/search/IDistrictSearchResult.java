package gs.web.search;

import gs.data.state.State;

public interface IDistrictSearchResult extends ISearchResult {

    public Integer getId();

    public State getState();

    public String getName();

    public String getCity();
}
