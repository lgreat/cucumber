package gs.web.search;

import gs.data.school.SchoolType;
import gs.data.search.PorterStandardAnalyzer;
import gs.data.search.SearchCommand;
import gs.data.search.Searcher;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.ISessionFacade;
import gs.web.SessionContext;
import gs.web.util.ListModel;
import gs.web.util.UrlBuilder;
import gs.web.util.Anchor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the controller that manages the CityDistrict module.  This module
 * appears above the main search results on search.page.  Two required parameters
 * are used to construct the model returned to the CityDistrict view:
 * <ul>
 * <li>The search query string</li>
 * <li>The current State</li>
 * </ul>
 * There are three optional paramaters that may be used to display more city or
 * district results:
 * <ul>
 * <li>morefiltered</li>
 * <li>morecities</li>
 * <li>moredistricts</li>
 * </ul>
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 * @author Andrew Peterson <mailto:apeterson@greatschools.net>
 */
public class CityDistrictRollupController extends ParameterizableViewController {

    public static final String BEAN_ID = "/search/citydistrict.module";

    public static final String PARAM_MORE_FILTERED = "morefiltered";
    public static final String PARAM_MORE_CITIES = "morecities";
    public static final String PARAM_MORE_DISTRICTS = "moredistricts";

    private static int LIST_SIZE = 3;  // The # of city or dist results to show
    private static int EXTENDED_LIST_SIZE = 50;

    private static final Logger _log =
            Logger.getLogger(CityDistrictRollupController.class);

    private static final String[] CITY_DIST_STOP_WORDS = {
            "a", "an", "and", "are", "as", "at", "be", "but", "by",
            "for", "if", "in", "into", "is", "it",
            "no", "not", "of", "on", "or", "s", "such",
            "t", "that", "the", "their", "then", "there", "these",
            "they", "this", "to", "was", "will", "with",
            // City/District specific stopwords
            "charter", "city", "district", "elementary", "middle", "high",
            "junior", "public", "private", "school", "schools"
    };

    private Searcher _searcher;
    private StateManager _stateManager;
    private QueryParser _queryParser;
    public static final String MODEL_CITIES = "cities";
    public static final String MODEL_DISTRICTS = "districts";
    public static final String MODEL_FILTERED_CITIES = "filteredCities";

    public CityDistrictRollupController(Searcher searcher) {
        _searcher = searcher;
        _queryParser = new QueryParser("text", new PorterStandardAnalyzer(CITY_DIST_STOP_WORDS));
        _queryParser.setOperator(QueryParser.DEFAULT_OPERATOR_AND);
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {

        boolean stuffToShow = false;

        // Parse parameters
        int filteredListSize =
                StringUtils.isNotEmpty(request.getParameter(PARAM_MORE_FILTERED)) ?
                        EXTENDED_LIST_SIZE : LIST_SIZE;


        ISessionFacade sessionContext = SessionContext.getInstance(request);
        State state = sessionContext.getStateOrDefault();

        String queryString = request.getParameter("q");


        Map model = new HashMap();
        if (StringUtils.isNotEmpty(queryString)) {

            BooleanQuery baseQuery = new BooleanQuery();
            if (sessionContext.getState() != null) {
                baseQuery.add(new TermQuery(new Term("state", state.getAbbreviationLowerCase())), true, false);
            }

            try {
                Query keywordQuery = _queryParser.parse(queryString);
                baseQuery.add(keywordQuery, true, false);
            } catch (ParseException pe) {
                _log.warn("error parsing: " + queryString, pe);
            }


            Hits cityHits = searchForCities(queryString, sessionContext.getState());
            ListModel cities = createCitiesListModel(request, cityHits, state,
                    StringUtils.equals("charter", request.getParameter("st")) ? SchoolType.CHARTER : null);
            model.put(MODEL_CITIES, cities);
            if (cities.getResults().size() > 0) {
                stuffToShow = true;
            }


            if (cityHits != null && cityHits.length() > 0) {

                StringBuffer filtersBuffer = new StringBuffer("All <span id=\"rollupfilters\">");

                UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, state, "");
                String lowerCaseQuery = queryString.toLowerCase();
                String st = null;
                if (lowerCaseQuery.indexOf("public") != -1) {
                    filtersBuffer.append(" public");
                    urlBuilder.setParameter("st", "public");
                    st = "public";
                } else if (lowerCaseQuery.indexOf("private") != -1) {
                    filtersBuffer.append(" private");
                    urlBuilder.setParameter("st", "public");
                    st = "private";
                } else if (lowerCaseQuery.indexOf("charter") != -1) {
                    filtersBuffer.append(" charter");
                    urlBuilder.setParameter("st", "charter");
                    st = "charter";
                }

                String gl = null;
                if (lowerCaseQuery.indexOf("elementary") != -1 ||
                        lowerCaseQuery.indexOf("primary") != -1) {
                    filtersBuffer.append(" elementary");
                    urlBuilder.setParameter("lc", "e");
                    gl = "elementary";
                } else if (lowerCaseQuery.indexOf("middle") != -1 ||
                        lowerCaseQuery.indexOf("junior") != -1 ||
                        lowerCaseQuery.indexOf("jr") != -1) {
                    filtersBuffer.append(" middle");
                    urlBuilder.setParameter("lc", "m");
                    gl = "middle";
                } else if (lowerCaseQuery.indexOf("high") != -1 ||
                        lowerCaseQuery.indexOf("senior") != -1) {
                    filtersBuffer.append(" high");
                    urlBuilder.setParameter("lc", "h");
                    gl = "high";
                }

                filtersBuffer.append("</span> schools in the city of:");
                model.put("filters", filtersBuffer.toString());
                model.put("filterparams", urlBuilder.toString());


                if (gl != null || st != null) {

                    ListModel listModel = new ListModel(filtersBuffer.toString());

                    boolean needMore = false;
                    for (int ii = 0; ii < cityHits.length(); ii++) {
                        String cityName = cityHits.doc(ii).get("city");
                        State stateOfCity = _stateManager.getState(cityHits.doc(ii).get("state"));

                        SearchCommand command = new SearchCommand();
                        command.setCity(cityName);
                        command.setState(stateOfCity);
                        command.setType("school");
                        if (st != null) {
                            command.setSt(new String[]{st});
                        }
                        if (gl != null) {
                            command.setGl(new String[]{gl});
                        }

                        Hits hits = _searcher.search(command);

                        if (hits != null && hits.length() > 0) {
                            if (ii < filteredListSize) {
                                urlBuilder.setParameter("city", cityName);
                                urlBuilder.setParameter("state", stateOfCity.getAbbreviation());
                                if (!ObjectUtils.equals(state, stateOfCity)) {
                                    cityName += ", " + stateOfCity.getAbbreviation();
                                }
                                listModel.addResult(urlBuilder.asAnchor(request, cityName));
                            } else {
                                needMore = true;
                            }
                        }
                    }
                    if (needMore) {
                        UrlBuilder builder = new UrlBuilder(request, "/search/search.page");
                        builder.addParametersFromRequest(request);
                        builder.setParameter(PARAM_MORE_FILTERED, "true");
                        listModel.addResult(builder.asAnchor(request, "more..."));
                    }
                    if (listModel.getResults().size() > 0) {
                        Anchor a = (Anchor) listModel.getResults().get(listModel.getResults().size() - 1);
                        a.setStyleClass("last");
                    }

                    if (listModel.getResults().size() > 0) {
                        model.put(MODEL_FILTERED_CITIES, listModel);
                    }
                }
            }


            Hits districtHits = searchForDistricts(baseQuery);
            ListModel districts = createDistrictsListModel(request, districtHits, state,
                    StringUtils.equals("charter", request.getParameter("st")) ? SchoolType.CHARTER : null);
            if (districts.getResults().size() > 0) {
                stuffToShow = true;
            }
            model.put(MODEL_DISTRICTS, districts);
        }

        //    <c:if test="${param.type != 'topic' and ((not empty model.filteredcities) || (not empty model.cities) || (not empty model.districts))}">

        return stuffToShow ? new ModelAndView(getViewName(), model) : null;
    }

    private ListModel createDistrictsListModel(HttpServletRequest request, Hits districtHits, State state, SchoolType schoolType) throws IOException {
        ListModel listModel = new ListModel("All " +
                (SchoolType.CHARTER.equals(schoolType) ? "charter " : "") +
                " schools in the district of:");

        int districtListSize =
                StringUtils.isNotEmpty(request.getParameter(PARAM_MORE_DISTRICTS)) ?
                        EXTENDED_LIST_SIZE : LIST_SIZE;
        for (int j = 0; j < districtListSize; j++) {
            if (districtHits != null && districtHits.length() > j) {
                Document districtDoc = districtHits.doc(j);
                String id = districtDoc.get("id");
                UrlBuilder builder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_DISTRICT, state, id);
                String districtName = districtDoc.get("name");
                String s = districtDoc.get("state");
                State stateOfCity = _stateManager.getState(s);
                if (!ObjectUtils.equals(state, stateOfCity)) {
                    districtName += " (" + stateOfCity.getAbbreviation() + ")";
                }
                listModel.addResult(builder.asAnchor(request, districtName));
            }
        }
        // add a more button if necessary
        if (districtHits.length() > LIST_SIZE &&
                districtListSize == LIST_SIZE) {
            UrlBuilder builder = new UrlBuilder(request, "/search/search.page");
            builder.addParametersFromRequest(request);
            builder.setParameter(PARAM_MORE_DISTRICTS, "true");
            listModel.addResult(builder.asAnchor(request, "more districts..."));
        }
        if (listModel.getResults().size() > 0) {
            Anchor a = (Anchor) listModel.getResults().get(listModel.getResults().size() - 1);
            a.setStyleClass("last");
        }
        return listModel;
    }

    private ListModel createCitiesListModel(HttpServletRequest request, Hits cityHits, State state, SchoolType schoolType) throws IOException {
        ListModel listModel = new ListModel("All " +
                (SchoolType.CHARTER.equals(schoolType) ? "charter " : "") +
                "schools in the city of: ");
        int cityListSize =
                StringUtils.isNotEmpty(request.getParameter(PARAM_MORE_CITIES)) ?
                        EXTENDED_LIST_SIZE : LIST_SIZE;
        for (int i = 0; i < cityListSize; i++) {
            if (cityHits != null && cityHits.length() > i) {
                Document cityDoc = cityHits.doc(i);
                String cityName = cityDoc.get("city");
                String s = cityDoc.get("state");
                State stateOfCity = _stateManager.getState(s);
                UrlBuilder builder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY,
                        stateOfCity,
                        cityName);
                if (!stateOfCity.equals(state)) {
                    cityName += ", " + stateOfCity;
                }
                listModel.addResult(builder.asAnchor(request, cityName));
            }
        }
        // add a more button if necessary
        if (cityHits.length() > LIST_SIZE &&
                cityListSize == LIST_SIZE) {
            UrlBuilder builder = new UrlBuilder(request, "/search/search.page");
            builder.addParametersFromRequest(request);
            builder.setParameter(PARAM_MORE_CITIES, "true");
            listModel.addResult(builder.asAnchor(request, "more cities..."));
        }
        if (listModel.getResults().size() > 0) {
            Anchor a = (Anchor) listModel.getResults().get(listModel.getResults().size() - 1);
            a.setStyleClass("last");
        }
        return listModel;
    }

    private Hits searchForDistricts(BooleanQuery baseQuery) {
        BooleanQuery districtQuery = new BooleanQuery();
        districtQuery.add(new TermQuery(new Term("type", "district")), true, false);
        districtQuery.add(baseQuery, true, false);
        Hits districtHits = _searcher.search(districtQuery, null, null, null);
        return districtHits;
    }

    /**
     * Query for cities matching query.
     *
     * @param state optional base state. Will be used for weighting the results.
     */
    private Hits searchForCities(String queryString, State state) {
        try {
            Query keywordQuery = _queryParser.parse(queryString);
            BooleanQuery cityQuery = new BooleanQuery();
            cityQuery.add(new TermQuery(new Term("type", "city")), true, false);
            cityQuery.add(keywordQuery, true, false);
            Hits cityHits = _searcher.search(cityQuery, null, null, null);
            return cityHits;
        } catch (ParseException pe) {
            _log.warn("error parsing: " + queryString, pe);
            return null;
        }

    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }
}
