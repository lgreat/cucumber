/*
* Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
* $Id: NearbyCitiesController.java,v 1.27 2008/03/26 17:27:12 aroy Exp $
*/

package gs.web.geo;

import gs.data.geo.ICity;
import gs.data.geo.IGeoDao;
import gs.data.geo.City;
import gs.data.geo.bestplaces.BpZip;
import gs.data.geo.bestplaces.BpCity;
import gs.data.state.State;
import gs.data.test.rating.ICityRatingDao;
import gs.data.test.rating.CityRating;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.list.AnchorListModelFactory;
import gs.web.util.list.AnchorListModel;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.orm.ObjectRetrievalFailureException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Provides an AnchorListModel of cities near the provided param "city" and "state".
 * Parameters:
 * <li>state
 * <li>city
 * <li>count - maximum number of cities to show. (optional)
 * Uses the styles "town", "city" and "bigCity" based on population.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class NearbyCitiesController extends AbstractController {
    protected static final Log _log = LogFactory.getLog(NearbyCitiesController.class);
    protected static final String PARAM_CITY = "city";
    protected static final String PARAM_COUNT = "count";
    /**
     * Set if you want a "see more nearby cities..." link.
     */
    protected static final String PARAM_MORE = "more";
    /**
     * Set if you want a "see Browse all cities in CA..." link.
     */
    public static final String PARAM_ALL = "all";
    /**
     * Set if you want to include the state in all names. Otherwise, only
     * those in cities of other states are indicated.
     */
    public static final String PARAM_INCLUDE_STATE = "includeState";
    /**
     * How the cities are sorted. Default is by proximity, but also can use "alpha" for
     * alphabetical.
     */
    public static final String PARAM_ORDER = "order";

    protected static final String PARAM_HEADING = "heading";


    // AnchorListModel.RESULTS has a list of Anchor objects
    protected static final String MODEL_CITY = "cityObject"; // Base city, ICity
    public static final String MODEL_CITIES = "cities"; // List of nearby cities
    public static final String MODEL_COMPARE_CITIES = "compareCities";

    private static final int DEFAULT_MAX_CITIES = 20;

    private String _viewName;
    private IGeoDao _geoDao;
    private ICityRatingDao _cityRatingDao;
    private AnchorListModelFactory _anchorListModelFactory;


    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        Map<String, Object> model = new HashMap<String, Object>();

        State state = SessionContextUtil.getSessionContext(request).getStateOrDefault();

        String cityNameParam = request.getParameter(PARAM_CITY);
        if (StringUtils.isNotEmpty(cityNameParam) && state != null) {
            loadCityList(state, model);

            ICity city = _geoDao.findCity(state, cityNameParam);
            if (city != null) {
                model.put(MODEL_CITY, city);

                int limit = DEFAULT_MAX_CITIES;
                if (request.getParameter(PARAM_COUNT) != null) {
                    limit = new Integer(request.getParameter(PARAM_COUNT));
                }
                List<ICity> nearbyCities = _geoDao.findNearbyCities(city, limit);

                if (StringUtils.equals("alpha", request.getParameter(PARAM_ORDER))) {
                    Collections.sort(nearbyCities, new Comparator<ICity>() {
                        public int compare(ICity o, ICity o1) {
                            return o.getName().compareToIgnoreCase(o1.getName());
                        }
                    });
                }

                List<CityAndRating> nearbyCitiesWithRatings = attachCityRatings(nearbyCities);
                model.put(MODEL_CITIES, nearbyCitiesWithRatings);

                String heading = request.getParameter(PARAM_HEADING) != null ? request.getParameter(PARAM_HEADING) : "Cities Near " + city.getName();
                AnchorListModel anchorListModel = _anchorListModelFactory.createNearbyCitiesWithRatingsAnchorListModel(
                        heading, city,
                        nearbyCitiesWithRatings,
                        limit,
                        request.getParameter(PARAM_INCLUDE_STATE) != null,
                        request.getParameter(PARAM_MORE) != null,
                        request.getParameter(PARAM_ALL) != null,
                        request
                );
                model.put(AnchorListModel.DEFAULT, anchorListModel);
            }
        }
        return new ModelAndView(_viewName, model);
    }

    protected void loadCityList(State state, Map<String, Object> model) {
        List<City> cities = _geoDao.findCitiesByState(state);
        City city = new City();
        city.setName("Choose city");
        cities.add(0, city);
        model.put(MODEL_COMPARE_CITIES, cities);
    }

    protected List<CityAndRating> attachCityRatings(List<ICity> cities) {
        List<CityAndRating> cityRatings = new ArrayList<CityAndRating>();

        for (ICity city: cities) {
            CityRating rating = null;
            // grab median home price
            ICity cityVal = populateWithBp(city);
            // grab city rating
            try {
                rating = _cityRatingDao.getCityRatingByCity(city.getState(), city.getName());
            } catch (ObjectRetrievalFailureException orfe) {
                // this is ok, it will display N/A
            }
            CityAndRating cityRating = new CityAndRating(cityVal, rating);
            cityRatings.add(cityRating);
        }
        return cityRatings;
    }

    protected ICity populateWithBp(ICity city) {
        // pull from BpCity
        ICity cityVal = _geoDao.findBpCity(city.getState(), city.getName());
        if (cityVal == null) {
            // can't find in BpCity, try BpZip ...
            BpZip zip = _geoDao.findZip(city.getState(), city.getName());
            if (zip == null) {
                // can't find any Bp entries, just return the original without any census info
                cityVal = city;
            } else {
                // BpZip isn't an ICity, so let's create a BpCity and populate it quickly
                BpCity bpcity = new BpCity();
                bpcity.setName(city.getName());
                bpcity.setState(city.getState());
                bpcity.setLat(city.getLatLon().getLat());
                bpcity.setLon(city.getLatLon().getLon());
                bpcity.setHouseMedianValue(zip.getHouseMedianValue());
                cityVal = bpcity;
            }
        }
        return cityVal;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public AnchorListModelFactory getAnchorListModelFactory() {
        return _anchorListModelFactory;
    }

    public void setAnchorListModelFactory(AnchorListModelFactory anchorListModelFactory) {
        _anchorListModelFactory = anchorListModelFactory;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public ICityRatingDao getCityRatingDao() {
        return _cityRatingDao;
    }

    public void setCityRatingDao(ICityRatingDao cityRatingDao) {
        _cityRatingDao = cityRatingDao;
    }

    public static class CityAndRating {
        private ICity _city;
        private CityRating _rating;

        public CityAndRating(ICity city, CityRating rating) {
            _city = city;
            _rating = rating;
        }

        public ICity getCity() {
            return _city;
        }

        public void setCity(ICity city) {
            _city = city;
        }

        public CityRating getRating() {
            return _rating;
        }

        public void setRating(CityRating rating) {
            _rating = rating;
        }
    }

}