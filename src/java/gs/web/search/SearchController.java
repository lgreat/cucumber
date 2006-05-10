package gs.web.search;

import gs.data.search.SearchCommand;
import gs.data.search.Searcher;
import gs.data.search.SpellCheckSearcher;
import gs.data.state.State;
import gs.web.SessionContext;
import gs.web.ISessionFacade;
import org.apache.lucene.search.Hits;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    public static final String PARAM_DISTRICT_NAME = "distname";
    public static final String PARAM_LEVEL_CODES = "lc";
    public static final String PARAM_SCHOOL_TYPES = "st";
    public static final String PARAM_PAGE = "p";
    public static final String PARAM_SHOW_ALL = "showall";
    public static final String PARAM_CITY = "city";
    public static final String PARAM_DISTRICT = "district";


    private static final String MODEL_QUERY = "q";
    private static final String MODEL_DISTRICT_NAME = "distname";
    private static final String MODEL_LEVEL_CODES = "lc";
    private static final String MODEL_SCHOOL_TYPES = "st";
    private static final String MODEL_PAGE = "p";
    private static final String MODEL_SHOW_ALL = "showall";
    private static final String MODEL_CITY = "city";
    private static final String MODEL_DISTRICT = "district";
    private static final String MODEL_TITLE = "title";
    private static final String MODEL_HEADING1 = "heading1";

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

        Map model = new HashMap();
        String queryString = request.getParameter(PARAM_QUERY);
        request.setAttribute(MODEL_QUERY, queryString);


        if (searchCommand.getCity() != null) {
            request.setAttribute(MODEL_CITY, searchCommand.getCity());
        } else if (searchCommand.getDistrict() != null) {
            request.setAttribute(MODEL_DISTRICT, searchCommand.getDistrict());
            request.setAttribute(MODEL_DISTRICT_NAME, request.getParameter(PARAM_DISTRICT_NAME));
        }

        String[] levels = request.getParameterValues(PARAM_LEVEL_CODES);
        if (levels != null) {
            request.setAttribute(MODEL_LEVEL_CODES, levels);
        }

        String[] sTypes = request.getParameterValues(PARAM_SCHOOL_TYPES);
        if (sTypes != null) {
            request.setAttribute(MODEL_SCHOOL_TYPES, sTypes);
        }

        int page = 1;
        String p = request.getParameter(PARAM_PAGE);
        if (p != null) {
            try {
                request.setAttribute(MODEL_PAGE, p);
                page = Integer.parseInt(p);
            } catch (Exception e) {
                // ignore this and just assume the page is 1.
            }
        }

        int pageSize = 10;
        int schoolsPageSize = "true".equals(request.getParameter(PARAM_SHOW_ALL)) ? -1 : 10;

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
            model.put("schoolsTotal", new Integer(_resultsPager.getSchoolsTotal()));
            model.put("schools", _resultsPager.getSchools(page, schoolsPageSize));
            model.put("pageSize", new Integer(pageSize));
            model.put("mainResults", _resultsPager.getResults(page, pageSize));
            model.put("total", new Integer(hts.length()));
        }

        model.put(MODEL_TITLE, queryString + " - Greatschools.net Search");

        String heading1;
        if (hts != null && hts.length() > 0) {
            String paramType = request.getParameter("type");
            if ("topic".equals(paramType)) {
                heading1 = "Topic results";
            }  else if ("school".equals(paramType)) {
                heading1 = "School results";
            }  else  {
                heading1 = "All results";
            }
            heading1 += " for \"<span class=\"headerquery\">" + queryString + "</span>\"";
        } else  {
            heading1 = "No results found";
            heading1 += " for \"<span class=\"headerquery\">" + queryString + "</span>\"";
            if (searchCommand.getState() != null) {
                heading1 += " in " +searchCommand.getState().getLongName();
            }
        }
        model.put(MODEL_HEADING1, heading1);
        /*
       ${gsweb:highlight(param.q, 'private', 'text')}</span>&quot;
        */

        String viewName;
        viewName = "search/mixed";
        return new ModelAndView(viewName, model);
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
