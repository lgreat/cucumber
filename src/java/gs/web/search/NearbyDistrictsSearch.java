package gs.web.search;

import gs.data.search.DistrictSearchFieldConstraints;
import gs.data.search.IFieldConstraint;
import gs.data.search.SearchException;
import gs.data.search.SearchResultsPage;
import gs.data.search.beans.IDistrictSearchResult;
import gs.data.search.services.DistrictSearchService;
import gs.data.state.State;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In the event SchoolSearchController is refactored, this class can be modified to accept as input a different
 * class as long as it contains lat, lon, and a way of determining the current city when on a browse page.
 */
class NearbyDistrictsSearch {
    public static final int DEFAULT_RADIUS = 50;
    public static final int DEFAULT_LIMIT = 11;

    private DistrictSearchService _districtSearchService;

    private static final Logger _log = Logger.getLogger(NearbyDistrictsSearch.class);

    SchoolSearchCommandWithFields _commandAndFields;

    public NearbyDistrictsSearch(SchoolSearchCommandWithFields commandAndFields) {
        this._commandAndFields = commandAndFields;
    }

    /**
     * Try to decouple this class from "SchoolSearchCommandWithFields" if SchoolSearchController changes.
     * Method does not actually look for "nearby" cities... just cities matching a string
     *
     * @return
     */
    public List<IDistrictSearchResult> searchForDistricts() {
        String searchString = _commandAndFields.getSearchString();
        State state = _commandAndFields.getState();
        List<IDistrictSearchResult> districtSearchResults = new ArrayList<IDistrictSearchResult>();

        if (state == null) {
            //don't try to find districts without a state
            return districtSearchResults;
        }

        Map<IFieldConstraint, String> districtConstraints = new HashMap<IFieldConstraint, String>();
        districtConstraints.put(DistrictSearchFieldConstraints.STATE, state.getAbbreviationLowerCase());

        try {
            if (searchString != null) {
                SearchResultsPage<IDistrictSearchResult> districtPage = getDistrictSearchService().search(searchString, districtConstraints, null, null, 0, DEFAULT_LIMIT);
                districtSearchResults = districtPage.getSearchResults();
            }
        } catch (SearchException ex) {
            _log.debug("something when wrong when attempting to use CitySearchService. Eating exception", ex);
        }

        return districtSearchResults;
    }

    public DistrictSearchService getDistrictSearchService() {
        return _districtSearchService;
    }

    public void setDistrictSearchService(DistrictSearchService districtSearchService) {
        _districtSearchService = districtSearchService;
    }
}
