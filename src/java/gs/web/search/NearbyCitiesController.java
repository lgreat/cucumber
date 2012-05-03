package gs.web.search;


import gs.data.search.*;
import gs.data.search.beans.CitySearchResult;
import gs.data.search.beans.ICitySearchResult;
import gs.web.util.Url;
import gs.web.util.UrlUtil;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

@Controller
@RequestMapping("/search/nearby/cities")
public class NearbyCitiesController implements BeanFactoryAware {
    
    BeanFactory _beanFactory;

    private GsSolrSearcher _gsSolrSearcher;

    private static final String MODEL_DATA_KEY = "cities";
    private static final float MAX_RADIUS = 100;
    private static final int MAX_COUNT = 100;

    @RequestMapping(method=RequestMethod.GET)
    public void list(
            @RequestParam(value="lat", required=true) float latitude,
            @RequestParam(value="lon", required=true) float longitude,
            @RequestParam(value="radius", required=false, defaultValue = "50") float radiusInMiles,
            @RequestParam(value="count", required=false, defaultValue = "5") int count,
            ModelMap modelMap
    ) {
        radiusInMiles = radiusInMiles < MAX_RADIUS ? radiusInMiles : MAX_RADIUS;
        count = count < MAX_COUNT ? count : MAX_COUNT;
        
        List<Map<String,String>> maps = new ArrayList<Map<String,String>>();

        GsSolrQuery gsSolrQuery = createGsSolrQuery();
        gsSolrQuery.restrictToRadius(latitude, longitude, radiusInMiles);
        gsSolrQuery.page(0, count);

        List<CitySearchResult> results = _gsSolrSearcher.simpleSearch(gsSolrQuery, CitySearchResult.class);

        for (CitySearchResult result : results) {
            maps.add(buildOneMap(result));
        }

        modelMap.put(MODEL_DATA_KEY, maps);
    }
    
    Map<String,String> buildOneMap(ICitySearchResult citySearchResult) {
        Map<String,String> map = new HashMap<String,String>();
        map.put("state", citySearchResult.getState().getAbbreviation());
        map.put("name", citySearchResult.getCity());
        try {
            String url = Url.SEARCH_SCHOOLS_NEARBY.relative(
                    citySearchResult.getLatitude().toString(),
                    citySearchResult.getLongitude().toString(),
                    "25",
                    URLEncoder.encode(citySearchResult.getCity() + ", " + citySearchResult.getState().getAbbreviation
                            (), "UTF-8"),
                    citySearchResult.getState().getAbbreviation()
            );
            url = UrlUtil.addParameter(url, "sortBy=GS_RATING_DESCENDING");
            map.put("url", url);
        } catch (UnsupportedEncodingException e) {
            map.put("url", "javascript:void(0);");
        }

        return map;
    }

    GsSolrQuery createGsSolrQuery() {
        GsSolrQuery gsSolrQuery = (GsSolrQuery) _beanFactory.getBean(GsSolrQuery.BEAN_ID, new Object[] {QueryType.CITY_SEARCH});
        return gsSolrQuery;
    }

    public BeanFactory getBeanFactory() {
        return _beanFactory;
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        _beanFactory = beanFactory;
    }

    public GsSolrSearcher getGsSolrSearcher() {
        return _gsSolrSearcher;
    }

    public void setGsSolrSearcher(GsSolrSearcher gsSolrSearcher) {
        _gsSolrSearcher = gsSolrSearcher;
    }
}
