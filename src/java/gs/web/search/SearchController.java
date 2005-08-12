package gs.web.search;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.lucene.search.Hits;
import gs.data.search.*;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.SessionContext;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 *         <p/>
 *         Parameters used in this page:
 *         <ul>
 *         <li>c :  constraint</li>
 *         <li>l :  location - CA, NY, WA, etc.</li>
 *         <li>p :  page</li>
 *         <li>q :  query string</li>
 *         <li>s :  style</li>
 *         </ul>
 */
public class SearchController extends AbstractController {

    public static final String BEAN_ID = "/search.page";
    private static Log _log = LogFactory.getLog(SearchController.class);
    private Searcher _searcher;
    private SpellCheckSearcher _spellCheckSearcher;
    private SessionContext _sessionContext;
    private ResultsPager _resultsPager;
    private StateManager _stateManager;

    public ModelAndView handleRequestInternal(HttpServletRequest request,
                                              HttpServletResponse response)
            throws Exception {

        Map model =  new HashMap ();

        String queryString = request.getParameter("q");

        if (queryString != null && !queryString.equals("")) {

            StringBuffer queryBuffer = new StringBuffer();
            queryBuffer.append(queryString);

            HttpSession session = request.getSession(true);

            _sessionContext = SessionContext.getInstance(request);

            String location = request.getParameter("l");
            State state = null;
            if (location != null) {
                state = _stateManager.getState(location);
                if (state != null) {
                    queryBuffer.append(" AND state:");
                    queryBuffer.append(state.getAbbreviation());
                }
            }
            _sessionContext.setState(state);


            int pageSize = 5;
            String constraint = request.getParameter("c");
            if (constraint != null && !constraint.equals("all")) {
                pageSize = 10;
            }

            // now deal with p - the page parameter.
            int page = 1;
            String p = request.getParameter("p");
            if (p != null) {
                try {
                    page = Integer.parseInt (p);
                } catch (Exception e) {
                    // ignore this and just assume the page is 1.
                }
            }
            _log.info("full query: " + queryBuffer.toString ());

            //Hits hits = _searcher.basicSearch(queryBuffer.toString ());
            SearchResult sr = _spellCheckSearcher.search(queryBuffer.toString ());

            _resultsPager.setHits(sr.getHits());
            model.put("suggestedQuery", sr.getSuggestedQueryString());
            model.put("articlesTotal", new Integer(_resultsPager.getArticlesTotal()));
            model.put("articles", _resultsPager.getArticles (page, pageSize));
            model.put("schoolsTotal", new Integer(_resultsPager.getSchoolsTotal()));
            model.put("schools", _resultsPager.getSchools(page, pageSize));
            model.put("pageSize", new Integer(pageSize));
        }

        return new ModelAndView("search", "results", model);
    }

    public Searcher getSearcher() {
        return _searcher;
    }

    public void setSearcher(Searcher searcher) {
        _searcher = searcher;
    }

    public void setResultsPager(ResultsPager pager) {
        _resultsPager = pager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }

    public void setSpellCheckSearcher(SpellCheckSearcher spellCheckSearcher) {
        _spellCheckSearcher = spellCheckSearcher;
    }
}