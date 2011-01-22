package gs.web.search;

import gs.data.state.State;

import java.util.List;

public interface DistrictSearchService {

    public List<? extends IDistrictSearchResult> search(String query, State state) throws SchoolSearchService.SearchException;

    public List<? extends IDistrictSearchResult> search(String query, State state, int offset, int count) throws SchoolSearchService.SearchException;

}
