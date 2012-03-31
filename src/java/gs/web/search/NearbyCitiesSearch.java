package gs.web.search;

import gs.data.geo.City;
import gs.data.search.CitySearchFieldConstraints;
import gs.data.search.IFieldConstraint;
import gs.data.search.SearchException;
import gs.data.search.SearchResultsPage;
import gs.data.search.beans.ICitySearchResult;
import gs.data.search.services.CitySearchService;
import gs.data.state.State;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * In the event SchoolSearchController is refactored, this class can be modified to accept as input a different
 * class as long as it contains lat, lon, and a way of determining the current city when on a browse page.
 */
public class NearbyCitiesSearch {
    public static final int DEFAULT_RADIUS = 50;
    SchoolSearchCommandWithFields _commandAndFields;

    private static final Logger _log = Logger.getLogger(NearbyCitiesSearch.class);

    private CitySearchService _citySearchService;

    public NearbyCitiesSearch(SchoolSearchCommandWithFields commandAndFields) {
        this._commandAndFields = commandAndFields;
    }

    /**
     * In the future this method should probably be decoupled from the "SchoolSearchCommandWithFields" class,
     * but only if this SchoolSearchController is refactored as well.
     *
     * @return
     */
    public List<ICitySearchResult> getNearbyCities() {
        Float browseLat = _commandAndFields.getLatitude();
        Float browseLon = _commandAndFields.getLongitude();
        City city = _commandAndFields.getCityFromUrl();
        List<ICitySearchResult> citySearchResults = new ArrayList<ICitySearchResult>();
        try {
            SearchResultsPage<ICitySearchResult> cityPage = getCitySearchService().getCitiesNear(browseLat, browseLon, DEFAULT_RADIUS, null, 0, SchoolSearchController.NEARBY_CITIES_PAGE_SIZE);

            citySearchResults = cityPage.getSearchResults();

            //if nearby city matches this city, remove it from nearby list
            if (citySearchResults != null && city != null) {
                Iterator<ICitySearchResult> iterator = citySearchResults.listIterator();
                while (iterator.hasNext()) {
                    ICitySearchResult result = iterator.next();
                    if (result.getCity().equals(city.getName())) {
                        iterator.remove();
                    }
                }
            }
        } catch (SearchException ex) {
            _log.debug("something went wrong when attempting to use CitySearchService. Eating exception", ex);
        }

        return citySearchResults;
    }

    public List<ICitySearchResult> getNearbyCitiesByLatLon() {
        Float lat = _commandAndFields.getLatitude();
        Float lon = _commandAndFields.getLongitude();
        List<ICitySearchResult> citySearchResults = new ArrayList<ICitySearchResult>();
        try {
            if (lat != null && lon != null) {
                SearchResultsPage<ICitySearchResult> cityPage = getCitySearchService().getCitiesNear(lat, lon, DEFAULT_RADIUS, null, 0, SchoolSearchController.NEARBY_CITIES_PAGE_SIZE);
                citySearchResults = cityPage.getSearchResults();
            }
        } catch (SearchException ex) {
            _log.debug("Something went wrong when attempting to use CitySearchService. Eating exception", ex);
        }

        return citySearchResults;
    }

    /**
     * Try to decouple this class from "SchoolSearchCommandWithFields" if SchoolSearchController changes.
     * Method does not actually look for "nearby" cities... just cities matching a string
     *
     * @return
     */
    public List<ICitySearchResult> searchForCities() {
        String searchString = _commandAndFields.getSearchString();
        State state = _commandAndFields.getState();
        List<ICitySearchResult> citySearchResults = new ArrayList<ICitySearchResult>();

        if (state == null) {
            //don't try to find cities without a state
            return citySearchResults;
        }

        Map<IFieldConstraint, String> cityConstraints = new HashMap<IFieldConstraint, String>();
        cityConstraints.put(CitySearchFieldConstraints.STATE, state.getAbbreviationLowerCase());

        try {
            if (searchString != null) {
                try {
                    // when searching for "anchorage, ak", do not search for cities matching "ak"
                    // primarily an issue with city autocomplete as implemented for GS-11928 Find a School by location
                    if (StringUtils.endsWithIgnoreCase(searchString, " " + state.getAbbreviation()) ||
                            StringUtils.endsWithIgnoreCase(searchString, "," + state.getAbbreviation())) {
                        searchString = StringUtils.substring(searchString, 0, searchString.length()-2);
                    }
                } catch (Exception e) {/* ignore */}
                SearchResultsPage<ICitySearchResult> cityPage = getCitySearchService().search(searchString, cityConstraints, null, null, 0, 33);
                citySearchResults = cityPage.getSearchResults();
            }
        } catch (SearchException ex) {
            _log.debug("something when wrong when attempting to use CitySearchService. Eating exception", ex);
        }

        return citySearchResults;
    }

    public CitySearchService getCitySearchService() {
        return _citySearchService;
    }

    public void setCitySearchService(CitySearchService citySearchService) {
        _citySearchService = citySearchService;
    }
}
