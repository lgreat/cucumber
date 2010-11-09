package gs.web.search;

import gs.data.state.State;

public interface ICitySearchResult extends ISearchResult{

    public String getCity();

    public State getState();
}
