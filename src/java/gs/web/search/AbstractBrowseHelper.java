package gs.web.search;

import gs.data.geo.City;
import gs.data.search.SearchException;
import gs.data.search.SearchResultsPage;
import gs.data.search.beans.ICitySearchResult;
import gs.data.search.services.CitySearchService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractBrowseHelper extends AbstractSchoolSearchHelper {

    @Autowired
    private CitySearchService _citySearchService;

    public List<ICitySearchResult> getNearbyCities(City city, Float latitude, Float longitude) {
        int defaultRadius = 50;
        int NEARBY_CITIES_PAGE_SIZE = 33;

        List<ICitySearchResult> citySearchResults = new ArrayList<ICitySearchResult>();
        try {
            SearchResultsPage<ICitySearchResult> cityPage = getCitySearchService().getCitiesNear(latitude, longitude, defaultRadius, null, 0, NEARBY_CITIES_PAGE_SIZE);

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
            getLogger().debug("something went wrong when attempting to use CitySearchService. Eating exception", ex);
        }

        return citySearchResults;
    }

    public abstract Logger getLogger();

    public CitySearchService getCitySearchService() {
        return _citySearchService;
    }

    public void setCitySearchService(CitySearchService citySearchService) {
        _citySearchService = citySearchService;
    }
}
