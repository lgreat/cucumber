package gs.web.search;

import gs.data.school.SchoolType;
import gs.data.search.PorterStandardAnalyzer;
import gs.data.search.SearchCommand;
import gs.data.search.Searcher;
import gs.data.search.SpellCheckSearcher;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.ISessionFacade;
import gs.web.SessionContext;
import gs.web.util.Anchor;
import gs.web.util.ListModel;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
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
    private static final String MODEL_RESULTS = "mainResults";
    private static final String MODEL_TOTAL_HITS = "total";

    private static final String MODEL_QUERY = "q";
    private static final String MODEL_PAGE = "p";
    /*private static final String MODEL_CITY = "city";
    private static final String MODEL_DISTRICT = "district";*/
    private static final String MODEL_TITLE = "title";
    private static final String MODEL_HEADING1 = "heading1";

    public static final String MODEL_CITIES = "cities";
    public static final String MODEL_DISTRICTS = "districts";
    public static final String MODEL_FILTERED_CITIES = "filteredCities";
    public static final String MODEL_SHOW_SUGGESTIONS = "showSuggestions"; // Boolean
    public static final String MODEL_SHOW_QUERY_AGAIN = "showQueryAgain"; // Boolean
    private static final String MODEL_SHOW_STATE_CHOOSER = "showStateChooser";


    private static int LIST_SIZE = 3;  // The # of city or dist results to show
    private static int EXTENDED_LIST_SIZE = 50;

    private static final Logger _log =
            Logger.getLogger(SearchController.class);

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

    private StateManager _stateManager;
    private QueryParser _queryParser;

    public SearchController(Searcher searcher) {
        _searcher = searcher;
        _queryParser = new QueryParser("text", new PorterStandardAnalyzer(CITY_DIST_STOP_WORDS));
        _queryParser.setOperator(QueryParser.DEFAULT_OPERATOR_AND);

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

    private Map createModel(HttpServletRequest request, SearchCommand searchCommand, ISessionFacade sessionContext, boolean debug) throws IOException {
        Map model = new HashMap();
        String queryString = searchCommand.getQueryString();
        model.put(MODEL_QUERY, queryString);

        /*if (searchCommand.getCity() != null) {
            model.put(MODEL_CITY, searchCommand.getCity());
        } else if (searchCommand.getDistrict() != null) {
            model.put(MODEL_DISTRICT, searchCommand.getDistrict());
        }*/


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

        boolean stuffToShow = false;
        Hits hts = _searcher.search(searchCommand);
        ResultsPager _resultsPager = new ResultsPager(hts, searchCommand.getType());
        if (hts != null) {
            if (debug) {
                _resultsPager.enableExplanation(_searcher, searchCommand.getQuery());
            }
            if (suggest) {
                model.put("suggestion", getSuggestion(queryString,
                        sessionContext.getState()));
            }
            model.put(MODEL_PAGE_SIZE, new Integer(pageSize));
            model.put(MODEL_RESULTS, _resultsPager.getResults(page, pageSize));
            model.put(MODEL_TOTAL_HITS, new Integer(hts.length()));
            stuffToShow = hts.length() > 0;
        }



        model.put(MODEL_TITLE, "Greatschools.net Search: " + queryString);

        String heading1;
        if (hts != null && hts.length() > 0) {
            String paramType = searchCommand.getType();
            if ("topic".equals(paramType)) {
                heading1 = "Topic results";
            } else if ("school".equals(paramType)) {
                heading1 = "School results";
            } else {
                heading1 = "All results";
            }
            heading1 += " for \"<span class=\"headerquery\">" + queryString + "</span>\"";
        } else {
            heading1 = "No results found";
            if (searchCommand.getState() != null) {
                heading1 += " in " + searchCommand.getState().getLongName();
            }
            heading1 += " for \"<span class=\"headerquery\">" + queryString + "</span>\"";
        }
        model.put(MODEL_HEADING1, heading1);

        // Parse parameters
        int filteredListSize =
                StringUtils.isNotEmpty(request.getParameter(PARAM_MORE_FILTERED)) ?
                        EXTENDED_LIST_SIZE : LIST_SIZE;


        State state = sessionContext.getStateOrDefault();
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
            if (cityHits != null && cityHits.length() != 0) {
                ListModel cities = createCitiesListModel(request, cityHits, state,
                        StringUtils.equals("charter", request.getParameter(PARAM_SCHOOL_TYPE)) ? SchoolType.CHARTER : null);
                if (cities.getResults().size() > 0) {
                    model.put(MODEL_CITIES, cities);
                    stuffToShow = true;
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

                    ListModel listModel = createFilteredCitiesListModel(filtersBuffer, cityHits, st, gl, filteredListSize, urlBuilder, state, request);

                    if (listModel.getResults().size() > 0) {
                        model.put(MODEL_FILTERED_CITIES, listModel);
                        stuffToShow = true;
                    }
                }
            }


            Hits districtHits = searchForDistricts(baseQuery);
            ListModel districts = createDistrictsListModel(request, districtHits, state,
                    StringUtils.equals("charter", request.getParameter(PARAM_SCHOOL_TYPE)) ? SchoolType.CHARTER : null);
            if (districts.getResults().size() > 0) {
                model.put(MODEL_DISTRICTS, districts);
                stuffToShow = true;
            }
        }
        model.put(MODEL_SHOW_QUERY_AGAIN, Boolean.TRUE);
        model.put(MODEL_SHOW_SUGGESTIONS, Boolean.valueOf(!stuffToShow));
        model.put(MODEL_SHOW_STATE_CHOOSER, Boolean.valueOf(!stuffToShow));
        return model;
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


    private ListModel createFilteredCitiesListModel(StringBuffer filtersBuffer, Hits cityHits, String st, String gl, int filteredListSize, UrlBuilder urlBuilder, State state, HttpServletRequest request) throws IOException {
        ListModel listModel = new ListModel(filtersBuffer.toString());

        boolean needMore = false;
        int displayed = 0;
        for (int ii = 0; ii < cityHits.length(); ii++) {
            if (displayed < filteredListSize) {
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
                    urlBuilder.setParameter("city", cityName);
                    urlBuilder.setParameter("state", stateOfCity.getAbbreviation());
                    cityName += ", " + stateOfCity.getAbbreviation();
                    listModel.addResult(urlBuilder.asAnchor(request, cityName));
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
            listModel.addResult(builder.asAnchor(request, "more..."));
        }
        if (listModel.getResults().size() > 0) {
            Anchor a = (Anchor) listModel.getResults().get(listModel.getResults().size() - 1);
            a.setStyleClass("last");
        }
        return listModel;
    }

    private String determineSchoolType(String lowerCaseQuery, StringBuffer filtersBuffer, UrlBuilder urlBuilder) {
        String st = null;
        if (lowerCaseQuery.indexOf("public") != -1) {
            filtersBuffer.append(" public");
            urlBuilder.setParameter(PARAM_SCHOOL_TYPE, "public");
            st = "public";
        } else if (lowerCaseQuery.indexOf("private") != -1) {
            filtersBuffer.append(" private");
            urlBuilder.setParameter(PARAM_SCHOOL_TYPE, "public");
            st = "private";
        } else if (lowerCaseQuery.indexOf("charter") != -1) {
            filtersBuffer.append(" charter");
            urlBuilder.setParameter(PARAM_SCHOOL_TYPE, "charter");
            st = "charter";
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
                cityName += ", " + stateOfCity;
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

    /**
     * A setter for Spring
     */
    public void setSpellCheckSearcher(SpellCheckSearcher spellCheckSearcher) {
        _spellCheckSearcher = spellCheckSearcher;
    }

    /**
     * A setter for Spring
     */
    public void setSearcher(Searcher searcher) {
        _searcher = searcher;
    }

    /**
     * A setter for Spring
     * @param resultsPager


    public void setResultsPager(ResultsPager resultsPager) {
    _resultsPager = resultsPager;
    }
     */
}
