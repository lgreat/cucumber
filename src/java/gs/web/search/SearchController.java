package gs.web.search;

import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.search.*;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.ISessionFacade;
import gs.web.SessionContext;
import gs.web.AnchorListModelFactory;
import gs.web.util.Anchor;
import gs.web.util.AnchorListModel;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This controller handles all search requests.
 * Search results are returned in paged form with single-type pages having 10
 * results and mixed-type pages having 3 results.  Multiple mixed-type pages
 * can be delivered in the model.
 * <ul>
 * <li>c    :  constraint</li>
 * <li>st   : state - CA, NY, WA, etc.</li>
 * <li>p    :  page</li>
 * <li>q    :  query string</li>
 * <li>s    :  style</li>
 * <li>sort :  sort column</li>
 * <li>r    :  sort reverse? (t/f)</li>
 * </ul>
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SearchController extends AbstractFormController {

    public static final String BEAN_ID = "/search/search.page";

    private SpellCheckSearcher _spellCheckSearcher;
    private Searcher _searcher;

    private boolean suggest = false;

    public static final String PARAM_DEBUG = "debug";
    public static final String PARAM_QUERY = "q";
    public static final String PARAM_PAGE = "p";
    public static final String PARAM_SHOW_ALL = "showall";

    public static final String PARAM_MORE_FILTERED = "morefiltered";
    public static final String PARAM_MORE_CITIES = "morecities";
    public static final String PARAM_MORE_DISTRICTS = "moredistricts";

    private static final String PARAM_SCHOOL_TYPE = "st";


    private static final String MODEL_PAGE_SIZE = "pageSize";
    protected static final String MODEL_RESULTS = "mainResults";
    private static final String MODEL_TOTAL_HITS = "total";

    private static final String MODEL_QUERY = "q";
    private static final String MODEL_PAGE = "p";
    /*private static final String MODEL_CITY = "city";
    private static final String MODEL_DISTRICT = "district";*/
    private static final String MODEL_TITLE = "title";
    private static final String MODEL_HEADING1 = "heading1";

    public static final String MODEL_CITIES = "cities";
    public static final String MODEL_DISTRICTS = "districts";
    public static final String MODEL_FILTERED_CITIES = "filteredCities"; // AnchorListModel
    public static final String MODEL_SHOW_SUGGESTIONS = "showSuggestions"; // Boolean
    public static final String MODEL_SHOW_QUERY_AGAIN = "showQueryAgain"; // Boolean
    private static final String MODEL_SHOW_STATE_CHOOSER = "showStateChooser"; // Boolean
    private static final String MODEL_NO_RESULTS_EXPLAINED = "noResultsExplanation";


    private static int LIST_SIZE = 3;  // The # of city or dist results to show
    private static int EXTENDED_LIST_SIZE = 50;

    private static final Logger _log =
            Logger.getLogger(SearchController.class);


    private StateManager _stateManager;
    private GSQueryParser _queryParser;
    private ISchoolDao _schoolDao;
    private AnchorListModelFactory _anchorListModelFactory;

    public SearchController(Searcher searcher) {
        _searcher = searcher;

        //_queryParser = new GSQueryParser("text", new GSAnalyzer());
        //_queryParser.setOperator(QueryParser.DEFAULT_OPERATOR_AND);
        _queryParser = new GSQueryParser();
    }

    public boolean isFormSubmission(HttpServletRequest request) {
        return true;
    }

    public ModelAndView showForm(HttpServletRequest request,
                                 HttpServletResponse response, BindException errors)
            throws Exception {
        throw new RuntimeException("SearchController.showForm() should not be called");
    }

    /**
     * Though this method throws <code>Exception</code>, it should swallow most
     * (all?) searching errors while just logging appropriately and returning
     * no results to the user.  Search/Query/Parsing errors are meaningless to
     * most users and should be handled internally.
     *
     * @return a <code>ModelAndView</code> which contains Map containting
     *         search results and attendant parameters as the model.
     * @throws Exception
     */
    public ModelAndView processFormSubmission(
            HttpServletRequest request, HttpServletResponse response, Object command, BindException errors)
            throws Exception {

        // Validate inputs
        if (command == null) {
            throw new IllegalArgumentException("no command");
        }

        if (!(command instanceof SearchCommand)) {
            throw new IllegalArgumentException("command of wrong type");
        }


        final ISessionFacade sessionContext = SessionContext.getInstance(request);
        SearchCommand searchCommand = (SearchCommand) command;

        boolean debug = false;
        if (request.getParameter(PARAM_DEBUG) != null) {
            debug = true;
        }

        Map model = createModel(request, searchCommand, sessionContext, debug);


        String viewName;
        viewName = "search/mixedResults";
        return new ModelAndView(viewName, model);
    }

    protected Map createModel(HttpServletRequest request, SearchCommand searchCommand, ISessionFacade sessionContext, boolean debug) throws IOException {
        Map model = new HashMap();
        final String queryString = searchCommand.getQueryString();
        model.put(MODEL_QUERY, queryString);

        int page = 1;
        String p = request.getParameter(PARAM_PAGE);
        if (p != null) {
            try {
                model.put(MODEL_PAGE, p);
                page = Integer.parseInt(p);
            } catch (Exception e) {
                // ignore this and just assume the page is 1.
            }
        }

        int pageSize = 10;

        boolean resultsToShow = false;
        Hits hits = _searcher.search(searchCommand);
        ResultsPager _resultsPager = new ResultsPager(hits, searchCommand.getType());
        if (hits != null && hits.length() > 0) {
            if (debug) {
                _resultsPager.enableExplanation(_searcher, searchCommand.getQuery());
            }
            if (suggest) {
                model.put("suggestion", getSuggestion(queryString,
                        sessionContext.getState()));
            }
            model.put(MODEL_PAGE_SIZE, new Integer(pageSize));
            model.put(MODEL_RESULTS, _resultsPager.getResults(page, pageSize));
            model.put(MODEL_TOTAL_HITS, new Integer(hits.length()));
            resultsToShow = true;
        }

        model.put(MODEL_TITLE, "Greatschools.net Search: " + queryString);

        // Parse parameters
        int filteredListSize =
                StringUtils.isNotEmpty(request.getParameter(PARAM_MORE_FILTERED)) ?
                        EXTENDED_LIST_SIZE : LIST_SIZE;


        State state = sessionContext.getStateOrDefault();
        if (StringUtils.isNotEmpty(queryString)) {

            BooleanQuery baseQuery = createBaseQuery(sessionContext, state, queryString);

            if (!"topic".equals(searchCommand.getType())) {
                Hits cityHits = _searcher.searchForCities(queryString, state);
                if (cityHits != null && cityHits.length() != 0) {
                    final int maxCities = StringUtils.isNotEmpty(request.getParameter(PARAM_MORE_CITIES)) ?
                            EXTENDED_LIST_SIZE : LIST_SIZE;
                    final boolean showMore = cityHits.length() > LIST_SIZE && maxCities == LIST_SIZE;
                    AnchorListModel cities = _anchorListModelFactory.createCitiesListModel(request, cityHits,
                            StringUtils.equals("charter", request.getParameter(PARAM_SCHOOL_TYPE)) ? SchoolType.CHARTER : null,
                            maxCities,
                            showMore);
                    if (cities.getResults().size() > 0) {
                        model.put(MODEL_CITIES, cities);
                        resultsToShow = true;
                    }
                }


                if (cityHits != null && cityHits.length() > 0) {

                    UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, state, "");
                    String lowerCaseQuery = queryString.toLowerCase();
                    StringBuffer filtersBuffer = new StringBuffer("All <span id=\"rollupfilters\">");
                    String st = determineSchoolType(lowerCaseQuery, filtersBuffer, urlBuilder);
                    String gl = determineGradeLevel(lowerCaseQuery, filtersBuffer, urlBuilder);

                    filtersBuffer.append("</span> schools in the city of:");
                    model.put("filters", filtersBuffer.toString());
                    model.put("filterparams", urlBuilder.toString());


                    if (gl != null || st != null) {

                        AnchorListModel anchorListModel = createFilteredCitiesListModel(filtersBuffer, cityHits, st, gl, filteredListSize, urlBuilder, state, request);

                        if (anchorListModel.getResults().size() > 0) {
                            model.put(MODEL_FILTERED_CITIES, anchorListModel);
                            resultsToShow = true;
                        }
                    }
                }


                Hits districtHits = searchForDistricts(baseQuery);
                AnchorListModel districts = _anchorListModelFactory.createDistrictsListModel(request, districtHits, state,
                        StringUtils.equals("charter", request.getParameter(PARAM_SCHOOL_TYPE)) ? SchoolType.CHARTER : null, StringUtils.isNotEmpty(request.getParameter(PARAM_MORE_DISTRICTS)) ?
                        EXTENDED_LIST_SIZE : LIST_SIZE, districtHits.length() > LIST_SIZE &&
                        (StringUtils.isNotEmpty(request.getParameter(PARAM_MORE_DISTRICTS)) ?
                        EXTENDED_LIST_SIZE : LIST_SIZE) == LIST_SIZE);
                if (districts.getResults().size() > 0) {
                    model.put(MODEL_DISTRICTS, districts);
                    resultsToShow = true;
                }
            }

        }

        String heading1;
        if (resultsToShow) { // was hits != null && hits.length() > 0
            String paramType = searchCommand.getType();
            if ("topic".equals(paramType)) {
                heading1 = "Topic results";
            } else if ("school".equals(paramType)) {
                heading1 = "School results";
            } else {
                heading1 = "Results";
            }
            heading1 += " for \"<span class=\"query\">" + queryString + "</span>\"";

            if (hits == null || hits.length() == 0) {
                if (!"topic".equals(searchCommand.getType()) &&
                        searchCommand.getState() != null) {
                    model.put(MODEL_NO_RESULTS_EXPLAINED,
                            "No schools found in " +
                                    searchCommand.getState().getLongName() +
                                    " matching \"<span class='query'>" +
                                    searchCommand.getQueryString() +
                                    "</span>\"");
                }

            }
        } else {
            heading1 = "No " + ("topic".equals(searchCommand.getType()) ? "topic" : "") +
                    " results found";
            if (searchCommand.getState() != null) {
                heading1 += " in " + searchCommand.getState().getLongName();
            }
            heading1 += " for \"<span class=\"query\">" + queryString + "</span>\"";
        }
        model.put(MODEL_HEADING1, heading1);


        model.put(MODEL_SHOW_QUERY_AGAIN, Boolean.TRUE);
        model.put(MODEL_SHOW_SUGGESTIONS, Boolean.valueOf(!resultsToShow));
        model.put(MODEL_SHOW_STATE_CHOOSER, Boolean.valueOf(!resultsToShow));
        return model;
    }

    /**
     * Create a basic boolean query to be re-used by other queries.
     *
     * @param sessionContext required
     * @param state          optional state
     */
    protected BooleanQuery createBaseQuery(ISessionFacade sessionContext, State state, String queryString) {
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
        return baseQuery;
    }

    /**
     * Supports "did-you-mean" functionality: returns a suggested query that
     * might return better results than the original query.
     *
     * @param state A state to filter search results.
     */
    private String getSuggestion(String query, State state) {
        String suggestion = _spellCheckSearcher.getSuggestion("name", query);
        if (suggestion == null) {
            suggestion = _spellCheckSearcher.getSuggestion("title", query);
        }
        if (suggestion == null) {
            suggestion = _spellCheckSearcher.getSuggestion("city", query);
        }
        if (suggestion != null) {
            // Check to see if the suggestion returns any results for the
            // current state.
            Hits suggestHits =
                    _searcher.search(suggestion + " state:" + state.getAbbreviation());
            if (suggestHits != null && suggestHits.length() > 0) {
                suggestion = suggestion.replaceAll("\\+", "");
            }
        }
        return suggestion;
    }


    private AnchorListModel createFilteredCitiesListModel(StringBuffer filtersBuffer, Hits cityHits, String st, String gl, int filteredListSize, UrlBuilder urlBuilder, State state, HttpServletRequest request) throws IOException {
        AnchorListModel anchorListModel = new AnchorListModel(filtersBuffer.toString());

        boolean needMore = false;
        int displayed = 0;
        for (int ii = 0; ii < cityHits.length(); ii++) {
            if (displayed < filteredListSize) {
                String cityName = cityHits.doc(ii).get("city");
                State stateOfCity = _stateManager.getState(cityHits.doc(ii).get("state"));
                int count = _schoolDao.countSchools(stateOfCity,
                        (st != null ? SchoolType.getSchoolType(st) : null),
                        (gl != null ? LevelCode.createLevelCode(gl.substring(0, 1)) : null),
                        cityName);
                if (count > 0) {
                    urlBuilder.setParameter("city", cityName);
                    urlBuilder.setParameter("state", stateOfCity.getAbbreviation());
                    cityName += ", " + stateOfCity.getAbbreviation();
                    anchorListModel.add(urlBuilder.asAnchor(request, cityName));
                    displayed++;
                }
            } else {
                needMore = true;
            }
        }
        if (needMore) {
            UrlBuilder builder = new UrlBuilder(request, "/search/search.page");
            builder.addParametersFromRequest(request);
            builder.setParameter(PARAM_MORE_FILTERED, "true");
            anchorListModel.add(builder.asAnchor(request, "more..."));
        }
        if (anchorListModel.getResults().size() > 0) {
            Anchor a = (Anchor) anchorListModel.getResults().get(anchorListModel.getResults().size() - 1);
            a.setStyleClass("last");
        }
        return anchorListModel;
    }

    private String determineSchoolType(String lowerCaseQuery, StringBuffer filtersBuffer, UrlBuilder urlBuilder) {
        String st = null;
        if (lowerCaseQuery.indexOf("public") != -1) {
            st = "public";
        } else if (lowerCaseQuery.indexOf("private") != -1) {
            st = "private";
        } else if (lowerCaseQuery.indexOf("charter") != -1) {
            st = "charter";
        }
        if (StringUtils.isNotEmpty(st)) {
            filtersBuffer.append(" " + st);
            urlBuilder.setParameter(PARAM_SCHOOL_TYPE, st);
        }

        return st;
    }

    private String determineGradeLevel(String lowerCaseQuery, StringBuffer filtersBuffer, UrlBuilder urlBuilder) {
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
        return gl;
    }



    protected Hits searchForDistricts(BooleanQuery baseQuery) {
        BooleanQuery districtQuery = new BooleanQuery();
        districtQuery.add(new TermQuery(new Term("type", "district")), true, false);
        districtQuery.add(baseQuery, true, false);
        Hits districtHits = _searcher.search(districtQuery, null, null, null);
        return districtHits;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }

    public void setSpellCheckSearcher(SpellCheckSearcher spellCheckSearcher) {
        _spellCheckSearcher = spellCheckSearcher;
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

    public AnchorListModelFactory getAnchorListModelFactory() {
        return _anchorListModelFactory;
    }

    public void setAnchorListModelFactory(AnchorListModelFactory anchorListModelFactory) {
        _anchorListModelFactory = anchorListModelFactory;
    }
}
