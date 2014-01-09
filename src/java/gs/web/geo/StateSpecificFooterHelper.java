package gs.web.geo;

import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.school.ISchoolDao;
import gs.data.search.GSAnalyzer;
import gs.data.search.Searcher;
import gs.data.state.State;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.queryParser.QueryParser;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class StateSpecificFooterHelper {
    public static final String BEAN_ID = "stateSpecificFooterHelper";
    private static final Log _log = LogFactory.getLog(StateSpecificFooterHelper.class);
    public static final int NUM_CITIES = 28;
    public static final String MODEL_TOP_CITIES = "popularCitiesByState";
    public static final String MODEL_ALPHA_GROUPS = "citiesInStateAlpha";
    public static final String STATE_FOR_POPULAR_CITIES = "stateForPopularCities";
    private IGeoDao _geoDao;
    private Searcher _searcher;
    private QueryParser _queryParser;
    private static Cache _topCitiesCache;
    private static Cache _alphaGroupsCache;
    private ISchoolDao _schoolDao;

    static {
        _topCitiesCache = new Cache(StateSpecificFooterHelper.class.getName() + ".topCities", 51, false, false, 3 * 60 * 60, 3 * 60 * 60);
        CacheManager.create().addCache(_topCitiesCache);

        _alphaGroupsCache = new Cache(StateSpecificFooterHelper.class.getName() + ".alphaGroups", 51, false, false, 3 * 60 * 60, 3 * 60 * 60);
        CacheManager.create().addCache(_alphaGroupsCache);
    }

    public static void clearCache() {
        _topCitiesCache.removeAll();
        _alphaGroupsCache.removeAll();
    }

    public StateSpecificFooterHelper() {
        _queryParser = new QueryParser("text", new GSAnalyzer());
        _queryParser.setDefaultOperator(QueryParser.Operator.AND);
    }

    public void placePopularCitiesInModel(State s, Map model) {
        if (s == null || model == null) {
            _log.warn("Call to StateSpecificFooterHelper without a state or model");
            return;
        }
        try {
            List<City> cities;
            Element element = _topCitiesCache.get(s);

            if (element != null) {
                cities = (List<City>) element.getObjectValue();
            } else {
                cities = _geoDao.findTopCitiesByPopulationInState(s, NUM_CITIES);
                _topCitiesCache.put(new Element(s,cities));
            }
            //we want to cache the results from geo dao in all circumstances, and I don't think the time required to
            //sort 28 strings is worth cacheing the sort result.
            if (cities != null && cities.size() == NUM_CITIES) {
                Collections.sort(cities, new Comparator<City>()
                {
                    public int compare(City o1, City o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });

                model.put(MODEL_TOP_CITIES, cities);
            }

            List<String> alphaGroups;
            Element alphaGroupsElement = _alphaGroupsCache.get(s);
            if (alphaGroupsElement != null) {
                alphaGroups = (List<String>)alphaGroupsElement.getObjectValue();
            } else {
                alphaGroups = getAlphaGroups(s);
                _alphaGroupsCache.put(new Element(s,alphaGroups));
            }
            model.put(MODEL_ALPHA_GROUPS, alphaGroups);

        } catch (Exception e) {
            _log.error(e, e);
        }
    }

    public void displayPopularCitiesForState(State s, Map model) {
        model.put(STATE_FOR_POPULAR_CITIES, s);
    }

    public void displayPopularCitiesForState(final State s, final ModelAndView modelAndView) {
        modelAndView.addObject(STATE_FOR_POPULAR_CITIES, s);

    }

    /**
     * Builds a List of Lists grouped by alpha order.  Each list should contain only
     * items that begin with the same letter.
     * @param state - a <code>State</code>
     * @return
     */
    protected List<String> getAlphaGroups(State state) {
        Map<String,Integer> cityCounts = _schoolDao.getCitySchoolCountForActiveSchools(state);

        List<String> alphaGroups = new ArrayList<String>();
        if (cityCounts != null && cityCounts.size() > 0) {
            char currentLetter = 'a';
            boolean currentLetterHasCities = false;

            Set<Map.Entry<String,Integer>> entrySet = cityCounts.entrySet();
            for (Map.Entry<String,Integer> entry : entrySet) {
                String name = entry.getKey();
                // Add the current letter to the alphaGroups on each letter change.
                String lowerName = name.trim().toLowerCase();
                if ((lowerName.length() > 0) && (currentLetter != lowerName.charAt(0))) {
                    if (currentLetterHasCities) {
                        alphaGroups.add(String.valueOf(currentLetter).toUpperCase());
                    }
                    currentLetter = lowerName.charAt(0);
                }

                if (name.matches("^\\p{Alnum}.*")) {
                    currentLetterHasCities = true;
                }
            }

            // Add the last working list.
            if (currentLetterHasCities) {
                alphaGroups.add(String.valueOf(currentLetter).toUpperCase());
            }
        }
        return alphaGroups;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public Searcher getSearcher() {
        return _searcher;
    }

    public void setSearcher(Searcher searcher) {
        _searcher = searcher;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }
}
