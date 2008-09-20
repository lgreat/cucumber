package gs.web.search;

import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.search.GSQueryParser;
import gs.data.search.SearchCommand;
import gs.data.search.Searcher;
import gs.data.search.Indexer;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.geo.IGeoDao;
import gs.data.geo.City;
import gs.data.geo.MultipleMatchesException;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.list.AnchorListModel;
import gs.web.util.list.AnchorListModelFactory;
import gs.web.school.SchoolsController;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.*;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractFormController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;

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

    private Searcher _searcher;

    public static final String PARAM_DEBUG = "debug";
    public static final String PARAM_QUERY = "q";
    public static final String PARAM_PAGE = "p";
    public static final String PARAM_SHOW_ALL = "showall";

    public static final String PARAM_MORE_FILTERED = "morefiltered";
    public static final String PARAM_MORE_CITIES = "morecities";
    public static final String PARAM_MORE_DISTRICTS = "moredistricts";

    public static final String PARAM_SCHOOL_TYPE = "st";
    public static final String PARAM_LEVEL_CODE = "lc";

    private static final String MODEL_PAGE_SIZE = "pageSize";
    protected static final String MODEL_RESULTS = "mainResults";
    private static final String MODEL_TOTAL_HITS = "total";
    public static final String MODEL_SEARCH_TYPE = "type";

    private static final String MODEL_QUERY = "q";
    private static final String MODEL_PAGE = "p";
    private static final String MODEL_TITLE = "title";
    private static final String MODEL_HEADING1 = "heading1";

    public static final String MODEL_SCHOOL_TYPE = "schoolType";
    public static final String MODEL_LEVEL_CODE = "levelCode";

    public static final String MODEL_CITIES = "cities";
    public static final String MODEL_DISTRICTS = "districts";
    public static final String MODEL_SHOW_SUGGESTIONS = "showSuggestions"; // Boolean
    public static final String MODEL_SHOW_QUERY_AGAIN = "showQueryAgain"; // Boolean
    private static final String MODEL_SHOW_STATE_CHOOSER = "showStateChooser"; // Boolean
    private static final String MODEL_NO_RESULTS_EXPLAINED = "noResultsExplanation";


    private static int LIST_SIZE = 3;  // The # of city or dist results to show
    private static int EXTENDED_LIST_SIZE = 50;

    private static final Logger _log =
            Logger.getLogger(SearchController.class);


    private GSQueryParser _queryParser;
    private ISchoolDao _schoolDao;
    private StateManager _stateManager;
    private AnchorListModelFactory _anchorListModelFactory;
    private IGeoDao _geoDao;

    public SearchController(Searcher searcher) {
        _searcher = searcher;
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
        if (command == null) throw new IllegalArgumentException("no command");
        if (!(command instanceof SearchCommand)) throw new IllegalArgumentException("command of wrong type");

        final SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        SearchCommand searchCommand = (SearchCommand) command;

        boolean debug = false;
        if (request.getParameter(PARAM_DEBUG) != null) debug = true;

        // Blank query string takes the user to browse pages
        if (StringUtils.isBlank(searchCommand.getQueryString())) {
            UrlBuilder builder;
            if (searchCommand.isTopicsOnly()) {
                builder = new UrlBuilder(UrlBuilder.ARTICLE_LIBRARY, sessionContext.getState());
            } else {
                builder = new UrlBuilder(UrlBuilder.RESEARCH, sessionContext.getState());
            }
            final String url = builder.asSiteRelative(request);
            final RedirectView view = new RedirectView(url, false);
            return new ModelAndView(view);
        }

        // GS-6866
        if ("b".equals(sessionContext.getABVersion())) {
            // need to add a check for multiple cities by the same name because findCity() just logs an error if multiple
            City city;
            try {
                city = getGeoDao().findCity(sessionContext.getState(), searchCommand.getQueryString(), false, true);
            } catch (MultipleMatchesException e) {
                // If there are two cities with the same name in a state they should be returned to normal search results
                city = null;
            }

            if (city != null) {
                return new ModelAndView(new RedirectView(
                    SchoolsController.createNewCityBrowseURI(city.getState(), city.getName(), new HashSet<SchoolType>(), null)));
            }
        }

        // ok, this seems like a valid search, set the "hasSearched" cookie
        if (!searchCommand.isTopicsOnly()) PageHelper.setHasSearchedCookie(request, response);
        Map<String, Object> model = createModel(request, searchCommand, sessionContext, debug);

        String viewname;
        if (searchCommand.isTopicsOnly()) {
            viewname = "search/mixedResults";
        } else {
            viewname = "search/schoolResults";
        }

        return new ModelAndView(viewname, model);
    }

    protected Map<String, Object> createModel(HttpServletRequest request, SearchCommand searchCommand, SessionContext sessionContext, boolean debug) throws IOException {
        Map<String, Object> model = new HashMap<String, Object>();
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

        String paramLevelCode = request.getParameter(PARAM_LEVEL_CODE);
        LevelCode levelCode;
        if (paramLevelCode != null) {
            levelCode = LevelCode.createLevelCode(paramLevelCode);
            searchCommand.setLevelCode(levelCode);
            model.put(MODEL_LEVEL_CODE, levelCode);
        }

        String[] paramSchoolType = request.getParameterValues(PARAM_SCHOOL_TYPE);
        if (paramSchoolType != null) {
            searchCommand.setSt(paramSchoolType);
            model.put(MODEL_SCHOOL_TYPE, paramSchoolType);
        }

        int pageSize = 10;

        String pageSizeParam = request.getParameter("pageSize");
        if (StringUtils.isNotBlank(pageSizeParam)) {
            try {
                Integer paramPageSize = new Integer(pageSizeParam);
                if (paramPageSize > 1) {
                    pageSize = paramPageSize;
                }
            } catch (Exception ex) {
                _log.warn("pageSize parameter should be an integer, ignoring: " + pageSizeParam);
            }
        }

        boolean resultsToShow = false;

        Hits hits = _searcher.search(searchCommand);
        ResultsPager _resultsPager = new ResultsPager(hits, ResultsPager.ResultType.valueOf(searchCommand.getType()));
        model.put(MODEL_SEARCH_TYPE, _resultsPager.getType());
        if (hits != null && hits.length() > 0) {
            // GS-6867 zip code searches can force a state switch to occur, so switch states if necessary
            State srState = _stateManager.getState(hits.doc(0).get("state"));
            if (srState != null && !sessionContext.getStateOrDefault().equals(srState)) {
                sessionContext.setState(srState);
            }
            if (debug) {
                _resultsPager.enableExplanation(_searcher, searchCommand.getQuery());
            }
            model.put(MODEL_PAGE_SIZE, pageSize);
            model.put(MODEL_RESULTS, _resultsPager.getResults(page, pageSize));
            model.put(MODEL_TOTAL_HITS, hits.length());
            resultsToShow = true;
        }

        model.put(MODEL_TITLE, "Greatschools.net Search: " + queryString);
        State state = sessionContext.getStateOrDefault();
        if (StringUtils.isNotEmpty(queryString)) {
            BooleanQuery baseQuery = createBaseQuery(sessionContext, state, queryString);
            if (!searchCommand.isTopicsOnly()) {
                Hits cityHits = _searcher.searchForCities(queryString, state);
                if (cityHits != null && cityHits.length() != 0) {
                    final int maxCities = StringUtils.isNotEmpty(request.getParameter(PARAM_MORE_CITIES)) ?
                            EXTENDED_LIST_SIZE : LIST_SIZE;
                    final boolean showMore = cityHits.length() > LIST_SIZE && maxCities == LIST_SIZE;
                    SchoolType filteredST = null;
                    if (request.getParameterValues(PARAM_SCHOOL_TYPE) != null &&
                            request.getParameterValues(PARAM_SCHOOL_TYPE).length == 1 &&
                            StringUtils.equals("charter", request.getParameter(PARAM_SCHOOL_TYPE))) {
                        filteredST = SchoolType.CHARTER;
                    }
                    AnchorListModel cities = _anchorListModelFactory.createCitiesListModel(request, cityHits,
                            filteredST,
                            maxCities,
                            showMore);
                    if (cities.getResults().size() > 0) {
                        model.put(MODEL_CITIES, cities);
                        resultsToShow = true;
                    }
                }

                // special case for GS-7076
                if (State.DC.equals(state)) {
                    try {
                        Query dcQuery = _queryParser.parse("district of columbia");
                        baseQuery.add(dcQuery, BooleanClause.Occur.SHOULD);
                    } catch (ParseException pe) {
                        _log.warn(pe);
                    }
                }

                Hits districtHits = searchForDistricts(baseQuery);
                SchoolType filteredST = null;
                if (request.getParameterValues(PARAM_SCHOOL_TYPE) != null &&
                        request.getParameterValues(PARAM_SCHOOL_TYPE).length == 1 &&
                        StringUtils.equals("charter", request.getParameter(PARAM_SCHOOL_TYPE))) {
                    filteredST = SchoolType.CHARTER;
                }
                AnchorListModel districts = _anchorListModelFactory.createDistrictsListModel(request, districtHits, state,
                        filteredST, StringUtils.isNotEmpty(request.getParameter(PARAM_MORE_DISTRICTS)) ?
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
            if (searchCommand.isTopicsOnly()) {
                heading1 = "Article results";
            } else if (searchCommand.isSchoolsOnly()) {
                heading1 = "School results";
            } else {
                heading1 = "Results";
            }
            heading1 += " for \"<span class=\"query\">" + queryString + "</span>\"";

            if (hits == null || hits.length() == 0) {
                if (!searchCommand.isTopicsOnly() &&
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
            String type = "";
            if (searchCommand.isTopicsOnly()) {
                type = "topic";
            } else if (searchCommand.isSchoolsOnly()) {
                type = "school";
            }

            heading1 = "No " + type +
                    " results found";
            if (searchCommand.getState() != null) {
                heading1 += " in " + searchCommand.getState().getLongName();
            }
            heading1 += " for \"<span class=\"query\">" + queryString + "</span>\"";
        }
        model.put(MODEL_HEADING1, heading1);


        model.put(MODEL_SHOW_QUERY_AGAIN, Boolean.TRUE);
        model.put(MODEL_SHOW_SUGGESTIONS, !resultsToShow);
        model.put(MODEL_SHOW_STATE_CHOOSER, !resultsToShow);
        return model;
    }

    protected Sort createSort(HttpServletRequest request, SearchCommand searchCommand) {
        if (!searchCommand.isSchoolsOnly()) {
            return null;
        }

        String sortColumn = request.getParameter("sortColumn");
        String sortDirection = request.getParameter("sortDirection");
        Sort result = null;
        if (sortColumn == null || sortColumn.equals("schoolName")) {
            boolean descending = false;  // default is ascending order
            if (sortDirection != null && sortDirection.equals("desc")) {
                descending = true;
            }
            result = new Sort(new SortField(Indexer.SORTABLE_NAME, SortField.STRING, descending));
        }
        return result;
    }

    /**
     * Create a basic boolean query to be re-used by other queries.
     *
     * @param sessionContext required
     * @param state          optional state
     * @param queryString    a plain text query
     * @return a BooleanQuery type
     */
    protected BooleanQuery createBaseQuery(SessionContext sessionContext, State state, String queryString) {
        BooleanQuery baseQuery = new BooleanQuery();
        if (sessionContext.getState() != null) {
            baseQuery.add(new TermQuery(new Term("state",
                    state.getAbbreviationLowerCase())), BooleanClause.Occur.MUST);
        }

        try {
            Query keywordQuery = _queryParser.parse(queryString);
            baseQuery.add(keywordQuery, BooleanClause.Occur.MUST);
        } catch (ParseException pe) {
            _log.warn("error parsing: " + queryString, pe);
        }
        return baseQuery;
    }

    protected Hits searchForDistricts(BooleanQuery baseQuery) {
        BooleanQuery districtQuery = new BooleanQuery();
        districtQuery.add(new TermQuery(new Term("type", "district")),
                BooleanClause.Occur.MUST);
        districtQuery.add(baseQuery, BooleanClause.Occur.MUST);
        return _searcher.search(districtQuery, null, null, null);
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

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }
}
