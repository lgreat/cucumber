package gs.web.search;

import gs.data.state.State;

import java.io.IOException;
import java.util.List;

public interface DistrictSearchService {

    public List<IDistrictSearchResult> search(String query, State state) throws SchoolSearchService.SearchException;
    
}
