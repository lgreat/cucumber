package gs.web.search;

import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.search.*;
import gs.data.search.Searcher;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.school.SearchResultsCookie;
import gs.web.util.PageHelper;
import gs.web.util.RedirectView301;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.list.AnchorListModel;
import gs.web.util.list.AnchorListModelFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
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
import java.util.*;

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
 * @author Chris Kimm <mailto:chriskimm@greatschools.org>
 */
public class SearchController extends AbstractFormController {

    public static final String BEAN_ID = "/search/oldSearch.page";

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

    public static final String PARAM_SORT_COLUMN = "sortColumn";
    public static final String PARAM_SORT_DIRECTION = "sortDirection";

    public static final String MODEL_PAGE_SIZE = "pageSize";
    protected static final String MODEL_RESULTS = "mainResults";
    public static final String MODEL_TOTAL_HITS = "total";
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
    public static final String MODEL_TYPEOVERRIDE = "typeOverride";
    public static final String MODEL_REL_CANONICAL = "relCanonical";

    private static int LIST_SIZE = 3;  // The # of city or dist results to show
    private static int EXTENDED_LIST_SIZE = 50;

    private static final Logger _log =
            Logger.getLogger(SearchController.class);


    private GSQueryParser _queryParser;
    private ISchoolDao _schoolDao;
    private StateManager _stateManager;
    private AnchorListModelFactory _anchorListModelFactory;

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

        // ok, this seems like a valid search, set the "hasSearched" cookie
        if (!searchCommand.isTopicsOnly()) {
            PageHelper.setHasSearchedCookie(request, response);
        }
        Map<String, Object> model;
        try {
            model = createModel(request, searchCommand, sessionContext, debug);
        } catch (RuntimeException e) {
            if (e.getMessage().equals("InvalidPage")) {
                UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.SCHOOL_SEARCH, sessionContext.getState(), request.getParameter(PARAM_QUERY));
                String lcParam = request.getParameter(PARAM_LEVEL_CODE);
                if (StringUtils.isNotBlank(lcParam)) {
                    urlBuilder.setParameter("lc", lcParam);
                }
                String[] schoolTypes = request.getParameterValues(PARAM_SCHOOL_TYPE);
                if (schoolTypes != null && schoolTypes.length > 0) {
                    for (String schoolType : schoolTypes) {
                        urlBuilder.addParameter("st", schoolType);
                    }
                }
                return new ModelAndView(new RedirectView301(urlBuilder.asSiteRelative(request)));
            } else {
                throw e;
            }
        }

        String viewname;
        if (searchCommand.isTopicsOnly()) {
            viewname = "search/mixedResults";
        } else {
            viewname = "search/schoolResults";
        }

        // aroy: This class is deprecated and this code is no longer called
        SearchResultsCookie mostRecentSearchResultsCookie = new SearchResultsCookie(request, response);
        String url = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        if (queryString != null) {
            url += "?" + queryString;
        }
        mostRecentSearchResultsCookie.setProperty("mostRecentSearchResults", url);
        
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

        int pageSize = 25;

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

        // Build the results and the model
        String sortColumn = request.getParameter(PARAM_SORT_COLUMN);
        String sortDirection = request.getParameter(PARAM_SORT_DIRECTION);
        // don't have a default sort
        if (sortDirection == null) {
            if (sortColumn != null && sortColumn.equals("schoolResultsHeader")) {
                sortDirection = "asc";
            } else {
                sortDirection = "desc";
            }
        }
        model.put(PARAM_SORT_COLUMN, sortColumn);
        model.put(PARAM_SORT_DIRECTION, sortDirection);

        Sort sort = createSort(request, searchCommand);
        searchCommand.setSort(sort);

        Hits hits = _searcher.search(searchCommand);

        Comparator comparator = SchoolComparatorFactory.createComparator(sortColumn, sortDirection);

        ResultsPager _resultsPager;
        ResultsPager.ResultType resultType;
        try {
            resultType = ResultsPager.ResultType.valueOf(searchCommand.getType());
        } catch (Exception e) {
            resultType = ResultsPager.ResultType.school;
        }
        if (comparator != null) {
            // sort the hits using the comparator
            _resultsPager = new ResultsPager(hits, resultType, comparator);
        } else {
            _resultsPager = new ResultsPager(hits, resultType);
        }

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

            List<Object> results = _resultsPager.getResults(page, pageSize);
            if (results == null) {
                // redirect to canonical page
                throw new RuntimeException("InvalidPage");
            }
            setCityGAMAttributes(request, results);
            model.put(MODEL_RESULTS, results);

            model.put(MODEL_TOTAL_HITS, hits.length());
            resultsToShow = true;
        }

        // set these attributes even if there are no hits
        setQueryGAMAttributes(request, queryString);

        model.put(MODEL_TITLE, "Greatschools.org Search: " + queryString);
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
                    for (int x=0; x < cityHits.length(); x++) {
                        Document cityDoc = cityHits.doc(x);
                        try {
                            if (StringUtils.equalsIgnoreCase(queryString, cityDoc.get("city"))
                                    && StringUtils.equalsIgnoreCase(state.getAbbreviation(), cityDoc.get("state"))) {
                                UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.CITY_PAGE, state, queryString);
                                model.put(MODEL_REL_CANONICAL, urlBuilder.asFullUrl(request));
                            }
                        } catch (Exception e) {
                            _log.warn("Error determining city URL for canonical: " + e, e);
                        }
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
                model.put(MODEL_TYPEOVERRIDE,"topic");
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
                model.put(MODEL_TYPEOVERRIDE,type);
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

        if (searchCommand.isSchoolsOnly() && model.get(MODEL_REL_CANONICAL) == null) {
            try {
                UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.RESEARCH, state);
                model.put(MODEL_REL_CANONICAL, urlBuilder.asFullUrl(request));
            } catch (Exception e) {
                _log.warn("Error determining state URL for canonical: " + e, e);                
            }
        }

        model.put(MODEL_SHOW_QUERY_AGAIN, Boolean.TRUE);
        model.put(MODEL_SHOW_SUGGESTIONS, !resultsToShow);
        model.put(MODEL_SHOW_STATE_CHOOSER, !resultsToShow);
        return model;
    }

    // GS-10448
    private void setCityGAMAttributes(HttpServletRequest request, List<Object> results) {
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        if (pageHelper != null) {
            Set<String> cityNames = new HashSet<String>();
            for (Object result : results) {
                if (result instanceof SchoolSearchResult) {
                    SchoolSearchResult res = (SchoolSearchResult) result;
                    cityNames.add(res.getSchool().getCity());
                }
            }

            for (String cityName : cityNames) {
                pageHelper.addAdKeywordMulti("city", cityName);
            }
        }
    }

    // GS-10642
    private void setQueryGAMAttributes(HttpServletRequest request, String queryString) {
        queryString = StringUtils.trimToNull(queryString);
        if (StringUtils.isBlank(queryString)) {
            return;
        }

        // also consider hyphens to be token separators
        queryString = queryString.replaceAll("-"," ");

        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        if (pageHelper != null) {
            String[] tokens = StringUtils.split(queryString);
            List<String> tokenList = Arrays.asList(tokens);

            Set<String> terms = new HashSet<String>(tokenList);
            for (String term : terms) {
                pageHelper.addAdKeywordMulti("query", term);
            }
        }
    }

    protected Sort createSort(HttpServletRequest request, SearchCommand searchCommand) {
        if (!searchCommand.isSchoolsOnly()) {
            return null;
        }

        String sortColumn = request.getParameter(PARAM_SORT_COLUMN);
        String sortDirection = request.getParameter(PARAM_SORT_DIRECTION);
        Sort result = null;
        // don't have a default sort
        if (sortColumn != null && sortColumn.equals("schoolResultsHeader")) {
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
}
