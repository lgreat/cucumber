package gs.web.geo;

import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.state.State;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class StateSpecificFooterHelper {
    private static final Log _log = LogFactory.getLog(StateSpecificFooterHelper.class);
    public static final int NUM_CITIES = 28;
    public static final String MODEL_TOP_CITIES = "popularCitiesByState";
    private IGeoDao _geoDao;

    public void placePopularCitiesInModel(State s, Map model) {
        placePopularCitiesInModel(s, model, _geoDao);
    }

    public static void placePopularCitiesInModel(State s, Map model, IGeoDao geoDao) {
        try {
            List<City> cities = geoDao.findTopCitiesByPopulationInState(s, NUM_CITIES);
            if (cities != null && cities.size() == NUM_CITIES) {
                Collections.sort(cities, new Comparator<City>()
                {
                    public int compare(City o1, City o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
                model.put(MODEL_TOP_CITIES, cities);
            }
        } catch (Exception e) {
            _log.error(e, e);
        }
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }
}
