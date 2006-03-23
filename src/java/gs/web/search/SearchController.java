package gs.web.search;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractFormController;
import org.springframework.validation.BindException;
import org.apache.lucene.search.*;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import gs.data.search.*;
import gs.data.search.Searcher;
import gs.data.search.SearchCommand;
import gs.data.state.State;
import gs.web.SessionContext;

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
    private SpellCheckSearcher _spellCheckSearcher;
    private Searcher _searcher;
    private ResultsPager _resultsPager;

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

        boolean debug = false;
        if (request.getParameter("debug") != null) {
            debug = true;
        }

        Map model = new HashMap();
        String queryString = request.getParameter("q");
        request.setAttribute("q", queryString);

        if (command != null && command instanceof SearchCommand) {
            SearchCommand sc = (SearchCommand) command;

            if (sc.getCity() != null) {
                request.setAttribute("city", sc.getCity());
            } else if (sc.getDistrict() != null) {
                request.setAttribute("district", sc.getDistrict());
                request.setAttribute("distname", request.getParameter("distname"));
            }

            String[] levels = request.getParameterValues("lc");
            if (levels != null) {
                request.setAttribute("lc", levels);
            }

            String[] sTypes = request.getParameterValues("st");
            if (sTypes != null) {
                request.setAttribute("st", sTypes);
            }

            int page = 1;
            String p = request.getParameter("p");
            if (p != null) {
                try {
                    request.setAttribute("p", p);
                    page = Integer.parseInt(p);
                } catch (Exception e) {
                    // ignore this and just assume the page is 1.
                }
            }

            int pageSize = 10;
            int schoolsPageSize = "true".equals(request.getParameter("showall")) ? -1 : 10;

            Hits hts = _searcher.search(sc);
            if (hts != null) {
                if (debug) {
                    _resultsPager.enableExplanation(_searcher, sc.getQuery());
                }
                _resultsPager.load(hts, sc.getType());
                if (suggest) {
                    model.put("suggestion", getSuggestion(queryString,
                            SessionContext.getInstance(request).getState()));
                }
                model.put("schoolsTotal", new Integer(_resultsPager.getSchoolsTotal()));
                model.put("schools", _resultsPager.getSchools(page, schoolsPageSize));
                model.put("pageSize", new Integer(pageSize));
                model.put("mainResults", _resultsPager.getResults(page, pageSize));
                model.put("total", new Integer(hts.length()));
            } else {
                _log.warn("Hits object is null for SearchCommand: " + sc);
            }
        }

        String viewName;
        /* ${(not empty param.city) or (not empty param.district)} */
        if (StringUtils.isNotEmpty(request.getParameter("city")) || StringUtils.isNotEmpty(request.getParameter("district"))) {
            viewName = "search/schoolsOnly";
        } else {
            viewName = "search/mixed";
        }
        return new ModelAndView(viewName, "results", model);
    }

    /**
     * Supports "did-you-mean" functionality: returns a suggested query that
     * might return better results than the original query.
     * @param query
     * @param state A state to filter search results.
     * @return
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
     * @param spellCheckSearcher
     */
    public void setSpellCheckSearcher(SpellCheckSearcher spellCheckSearcher) {
        _spellCheckSearcher = spellCheckSearcher;
    }

    /**
     * A setter for Spring
     * @param searcher
     */
    public void setSearcher(Searcher searcher) {
        _searcher = searcher;
    }

    /**
     * A setter for Spring
     * @param resultsPager
     */
    public void setResultsPager(ResultsPager resultsPager) {
        _resultsPager = resultsPager;
    }
}
