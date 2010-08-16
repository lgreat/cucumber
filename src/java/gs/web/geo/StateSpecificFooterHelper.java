package gs.web.geo;

import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.search.GSAnalyzer;
import gs.data.search.Indexer;
import gs.data.search.Searcher;
import gs.data.state.State;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;

import java.io.IOException;
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
    private IGeoDao _geoDao;
    private Searcher _searcher;
    private QueryParser _queryParser;

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
            List<City> cities = _geoDao.findTopCitiesByPopulationInState(s, NUM_CITIES);
            if (cities != null && cities.size() == NUM_CITIES) {
                Collections.sort(cities, new Comparator<City>()
                {
                    public int compare(City o1, City o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
                model.put(MODEL_TOP_CITIES, cities);
            }
            model.put(MODEL_ALPHA_GROUPS, getAlphaGroups(getHits(s)));
        } catch (Exception e) {
            _log.error(e, e);
        }
    }

    /**
     * Returns a Hits object containing all of the matches for cities within a state.
     * @param state - a <code>State</code>
     * @return a Hits containing matches or null if no matches could be found.
     * @throws Exception if there is a parsing or searching error.
     */
    protected Hits getHits(State state) throws Exception {
        BooleanQuery query = new BooleanQuery();
        query.add(new TermQuery(new Term("type", "city")), BooleanClause.Occur.MUST);
        query.add(new TermQuery(new Term("state", state.getAbbreviationLowerCase())), BooleanClause.Occur.MUST);

        return _searcher.search(query, new Sort(Indexer.SORTABLE_NAME), null, null);
    }

    /**
     * Builds a List of Lists grouped by alpha order.  Each list should contain only
     * items that begin with the same letter.
     * @param hits the Lucene results
     * @return a List<String>
     * @throws java.io.IOException - if something gets nasty.
     */
    protected List<String> getAlphaGroups(Hits hits) throws IOException {
        List<String> alphaGroups = new ArrayList<String>();
        if (hits != null && hits.length() > 0) {
            char currentLetter = 'a';
            boolean currentLetterHasCities = false;
            for (int i = 0; i < hits.length(); i++) {
                Document doc = hits.doc(i);
                String name = doc.get(Indexer.CITY);

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
}
