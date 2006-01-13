package gs.web.search;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractFormController;
import org.springframework.validation.BindException;
import org.apache.lucene.search.*;
import org.apache.log4j.Logger;
import gs.data.search.*;
import gs.data.search.Searcher;
import gs.data.search.SearchCommand;
import gs.data.state.State;
import gs.web.SessionContext;
import gs.web.ISessionFacade;

import java.util.*;

/**
 * This controller handles all search requests.
 * Search results are returned in paged form with single-type pages having 10
 * results and mixed-type pages having 3 results.  Multiple mixed-type pages
 * can be delivered in the model.
 * <p/>
 * <p/>
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
    private static Logger _log = Logger.getLogger(SearchController.class);
    private static Logger searchLog = Logger.getLogger("search");
    private SpellCheckSearcher _spellCheckSearcher;
    private Searcher _searcher;
    private boolean suggest = false;

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
     * @param request
     * @param response
     * @return a <code>ModelAndView</code> which contains Map containting
     *         search results and attendant parameters as the model.
     * @throws Exception
     */
    public ModelAndView processFormSubmission(
            HttpServletRequest request, HttpServletResponse response, Object command, BindException errors)
            throws Exception {

        long requestStart = System.currentTimeMillis();

        boolean debug = false;
        if (request.getParameter("debug") != null) {
            debug = true;
        }

        ISessionFacade sessionContext = SessionContext.getInstance(request);

        Map model = new HashMap();
        ResultsPager _resultsPager = null;
        String queryString = request.getParameter("q");
        String constraint = null;
        String suggestion = null;
        if (command != null && command instanceof SearchCommand) {
            SearchCommand sc = (SearchCommand) command;

            // set up the city and dist attributes, needed by searchsummary
            if (sc.getCity() != null) {
                request.setAttribute("city", sc.getCity());
            } else if (sc.getDistrict() != null) {
                request.setAttribute("district", sc.getDistrict());
                request.setAttribute("distname", request.getParameter("distname"));
            }

            String[] levels = request.getParameterValues("gl");
            if (levels != null) {
                request.setAttribute("gl", levels);
            }

            String[] sTypes = request.getParameterValues("st");
            if (sTypes != null) {
                request.setAttribute("st", sTypes);
            }

            int page = 1;
            String p = request.getParameter("p");
            if (p != null) {
                try {
                    page = Integer.parseInt(p);
                } catch (Exception e) {
                    // ignore this and just assume the page is 1.
                }
            }

            int pageSize = 10;
            int schoolsPageSize = 10;

            constraint = sc.getType();

            Hits hts = _searcher.search(sc);
            if (hts != null) {

                _resultsPager = new ResultsPager();
                _resultsPager.setQuery(sc.getQueryString());

                if (debug) {
                    _resultsPager.enableExplanation(_searcher, sc.getQuery());
                }
                _resultsPager.load(hts, constraint);

                if (suggest) {
                    suggestion = _spellCheckSearcher.getSuggestion("name", queryString);
                    if (suggestion == null) {
                        suggestion = _spellCheckSearcher.getSuggestion("title", queryString);
                    }
                    if (suggestion == null) {
                        suggestion = _spellCheckSearcher.getSuggestion("city", queryString);
                    }
                    if (suggestion != null) {
                        // Check to see if the suggestion returns any results for the
                        // current state. It's ok if the filter returned by
                        // Searcher.getFilter is null.
                        long s = System.currentTimeMillis();
                        Filter filter = _searcher.getFilter(sessionContext.getState());
                        Hits suggestHits = _searcher.search(suggestion, null, null, filter);
                        if (suggestHits != null && suggestHits.length() > 0) {

                            suggestion = suggestion.replaceAll("\\+", "");
                            model.put("suggestion", suggestion);
                        }
                        long f = System.currentTimeMillis();
                        searchLog.info("did-you-mean overhead: " + (f - s));
                    }
                }

                model.put("articlesTotal", new Integer(_resultsPager.getArticlesTotal()));
                model.put("articles", _resultsPager.getArticles(page, pageSize));
                model.put("schoolsTotal", new Integer(_resultsPager.getSchoolsTotal()));
                model.put("schools", _resultsPager.getSchools(page, schoolsPageSize));
                model.put("districtsTotal", new Integer(_resultsPager.getDistrictsTotal()));
                model.put("districts", _resultsPager.getDistricts(page, pageSize));
                model.put("citiesTotal", new Integer(_resultsPager.getCitiesTotal()));
                model.put("cities", _resultsPager.getCities(page, pageSize));
                model.put("termsTotal", new Integer(_resultsPager.getTermsTotal()));
                model.put("terms", _resultsPager.getTerms(page, pageSize));
                model.put("pageSize", new Integer(pageSize));
                model.put("mainResults", _resultsPager.getResults(page, pageSize));
                model.put("total", new Integer(hts.length()));
            } else {
                _log.warn("Hits object is null for SearchCommand: " + sc);
            }
        }

        long requestEnd = System.currentTimeMillis();
        long requestTime = requestEnd - requestStart;
        if (debug) {
            model.put("requesttime", Long.toString(requestTime));
        }
        if (_resultsPager != null) {
            logIt(queryString, constraint, sessionContext.getState(),
                    _resultsPager.getResultsTotal(), requestTime, suggestion);
        }
        return new ModelAndView("search/search", "results", model);
    }

    private static void logIt(String query, String type, State state, int results,
                              long time, String suggestion) {
        StringBuffer logBuffer = new StringBuffer(100);
        logBuffer.append("query:[");
        if (query != null) {
            logBuffer.append(query);
        } else {
            logBuffer.append("null");
        }
        logBuffer.append("] ");
        logBuffer.append("type:[");
        logBuffer.append(type);
        logBuffer.append("] ");
        if (state != null) {
            logBuffer.append("state:[");
            logBuffer.append(state.getAbbreviation());
            logBuffer.append("] ");
        }
        logBuffer.append("results:[");
        logBuffer.append(results);
        logBuffer.append("] ");
        logBuffer.append("time:[");
        logBuffer.append(time);
        logBuffer.append("] ");
        if (suggestion != null) {
            logBuffer.append("suggestion:[");
            logBuffer.append(suggestion);
            logBuffer.append("] ");
        }
        searchLog.info(logBuffer.toString());
    }

    /**
     * A setter for Spring
     *
     * @param spellCheckSearcher
     */
    public void setSpellCheckSearcher(SpellCheckSearcher spellCheckSearcher) {
        _spellCheckSearcher = spellCheckSearcher;
    }

    /**
     * A setter for Spring
     *
     * @param searcher
     */
    public void setSearcher(Searcher searcher) {
        _searcher = searcher;
    }
}
