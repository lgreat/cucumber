package gs.web.search;

import gs.data.geo.City;
import gs.data.search.GsSolrQuery;
import gs.data.search.GsSolrSearcher;
import gs.data.search.QueryType;
import gs.data.search.beans.CitySearchResult;
import gs.data.search.fields.CityFields;
import gs.data.search.services.CitySearchService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;
import java.util.List;

public abstract class AbstractBrowseHelper extends AbstractSchoolSearchHelper {

    @Autowired
    private CitySearchService _citySearchService;

    @Autowired
    private NearbyCitiesController _nearbyCitiesController;

    @Autowired
    private GsSolrSearcher _gsSolrSearcher;

    public List<CitySearchResult> getNearbyCities(City excludeCity, Float latitude, Float longitude) {
        GsSolrQuery gsSolrQuery = createCityGsSolrQuery();
        gsSolrQuery.restrictToRadius(latitude, longitude, NEARBY_CITIES_RADIUS);
        gsSolrQuery.page(0, NEARBY_CITIES_COUNT);

        // exclude provided city from results
        gsSolrQuery.filterNot(CityFields.CITY_KEYWORD, excludeCity.getName().toLowerCase());

        List<CitySearchResult> results = _gsSolrSearcher.simpleSearch(gsSolrQuery, CitySearchResult.class);

        return results;
    }

    GsSolrQuery createCityGsSolrQuery() {
        GsSolrQuery gsSolrQuery = new GsSolrQuery(QueryType.CITY_SEARCH);
        return gsSolrQuery;
    }

    public abstract Logger getLogger();

    public CitySearchService getCitySearchService() {
        return _citySearchService;
    }

    public void setCitySearchService(CitySearchService citySearchService) {
        _citySearchService = citySearchService;
    }
}
