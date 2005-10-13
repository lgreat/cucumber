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

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

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

    private boolean SUGGEST = true;

    public boolean isFormSubmission(HttpServletRequest request) {
        return true;
    }

    public ModelAndView showForm(HttpServletRequest request,
                                 HttpServletResponse response, BindException errors)
			throws Exception {
        throw new RuntimeException("SearchController.showForm() should not be called");
        //return doRequest(request, response);
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
        if (request.getParameter("debug") != null) { debug = true; }

        Map model = new HashMap();

        String queryString = request.getParameter("q");
        _log.info("Search query:" + queryString);

        if (command != null && command instanceof SearchCommand &&
                queryString != null && !queryString.equals("")) {
            SearchCommand sc = (SearchCommand)command;

            List qList= new ArrayList(); // for debug output
            if (debug) { qList.add(sc.getQuery()); }

            Hits hts = _searcher.search(sc);
            if (hts != null) {
                _log.debug("hit count: " + hts.length());
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

            String constraint = request.getParameter("c");

            int pageSize = 10;
            int schoolsPageSize = 10;



            if (constraint != null && !constraint.equals("all") && !constraint.equals("")) {
                _resultsPager.load(hts, constraint);
            } else {
                String[] types =
                        {"school", "article", "district", "city", "term"};
                pageSize = 3;
                schoolsPageSize = 6;
                for (int i = 0; i < types.length; i++) {
                    sc.setType(types[i]);
                    if (debug) { qList.add(sc.getQuery()); }
                    Hits hits = _searcher.search(sc);
                    _resultsPager.load(hits, types[i]);
                }
            }



            _resultsPager.setQuery(queryString);

            if (SUGGEST) {
                String suggestion = _spellCheckSearcher.getSuggestion("name", queryString);

                if (suggestion == null) {
                    suggestion = _spellCheckSearcher.getSuggestion("title", queryString);
                }

                if (suggestion == null) {
                    suggestion = _spellCheckSearcher.getSuggestion("cityname", queryString);
                }

                if (suggestion != null) {
                    suggestion = suggestion.replaceAll("\\+", "");
                }
                model.put("suggestion", suggestion);
            }

            if (debug) { model.put("queries", qList);}
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
        }
        long requestEnd = System.currentTimeMillis();
        long requestTime = requestEnd - requestStart;
        if (debug) { model.put("requesttime", Long.toString(requestTime)); }
        return new ModelAndView("search/search", "results", model);
    }

    /**
     * A setter for Spring
     * @param pager
     */
    public void setResultsPager(ResultsPager pager) {
        _resultsPager = pager;
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
}
