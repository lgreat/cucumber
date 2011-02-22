package gs.web.search;

import gs.data.state.State;

import java.util.List;

public interface DistrictSearchService {

    public List<IDistrictSearchResult> search(String query, State state) throws SearchException;

    public List<IDistrictSearchResult> search(String query, State state, int offset, int count) throws SearchException;

}
