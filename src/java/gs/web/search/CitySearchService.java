package gs.web.search;

import gs.data.state.State;

import java.util.List;

public interface CitySearchService {

    public List<ICitySearchResult> search(String query, State state) throws SearchException;
    
    public List<ICitySearchResult> search(String query, State state, int offset, int count) throws SearchException;

}
