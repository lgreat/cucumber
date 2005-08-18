package gs.web.search;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import gs.data.search.*;
import gs.data.state.State;
import gs.data.state.StateManager;

import java.util.Map;
import java.util.HashMap;

/**
 * This controller handles all search requests.
 * Search results are returned in paged form with single-type pages having 10
 * results and mixed-type pages having 3 results.  Multiple mixed-type pages
 * can be delivered in the model.
 *
 *         <p/>
 *         Parameters used in this page:
 *         <ul>
 *         <li>c :  constraint</li>
 *         <li>l :  location - CA, NY, WA, etc.</li>
 *         <li>p :  page</li>
 *         <li>q :  query string</li>
 *         <li>s :  style</li>
 *         </ul>
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SearchController extends AbstractController {

    public static final String BEAN_ID = "/search.page";
    private static Log _log = LogFactory.getLog(SearchController.class);
    private SpellCheckSearcher _spellCheckSearcher;
    private ResultsPager _resultsPager;
    private StateManager _stateManager;
    private int pageSize = 3;

    /**
     * Though this message throws <code>Exception</code>, it should swallow most
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
    public ModelAndView handleRequestInternal(HttpServletRequest request,
                                              HttpServletResponse response)
            throws Exception {

        Map model =  new HashMap ();
        String queryString = request.getParameter("q");

        _log.info("Search query:" + queryString);

        if (queryString != null && !queryString.equals("")) {

            StringBuffer queryBuffer = new StringBuffer();
            queryBuffer.append(queryString);

            String location = request.getParameter("l");
            State state = null;
            if (location != null) {
                state = _stateManager.getState(location);
                if (state != null) {
                    queryBuffer.append(" AND state:");
                    queryBuffer.append(state.getAbbreviation());
                }
            }

            // deal with p - the page parameter.
            int page = 1;
            String p = request.getParameter("p");
            if (p != null) {
                try {
                    page = Integer.parseInt (p);
                } catch (Exception e) {
                    // ignore this and just assume the page is 1.
                }
            }

            String suggestion = null;
            String constraint = request.getParameter("c");
            String qString = queryBuffer.toString();

            if (constraint != null && !constraint.equals("all")) {
                pageSize = 10;
                StringBuffer clone = new StringBuffer (qString);
                clone.append(" AND type:");
                clone.append(constraint);
                DecoratedHits dh = _spellCheckSearcher.search(clone.toString ());

                if (dh != null) {
                    if (constraint.equals("school")) {
                        _resultsPager.setSchools (dh.getHits());

                    } else if (constraint.equals("article")) {
                        _resultsPager.setArticles (dh.getHits());
                    } else {
                        _resultsPager.setDistricts (dh.getHits());
                    }
                    suggestion = dh.getSuggestedQueryString();
                }
            } else {
                String[] types = {"school", "article", "district"};
                pageSize = 3;
                for (int i = 0; i < types.length; i++) {
                    StringBuffer clone = new StringBuffer (qString);
                    clone.append (" AND type:");
                    clone.append (types[i]);
                    DecoratedHits dh = _spellCheckSearcher.search(clone.toString ());
                    if (dh != null) {
                        if (types[i].equals("school")) {
                            _resultsPager.setSchools (dh.getHits());
                            suggestion = dh.getSuggestedQueryString();
                        } else if (types[i].equals("article")) {
                            _resultsPager.setArticles (dh.getHits());
                        } else {
                            _resultsPager.setDistricts (dh.getHits());
                        }
                    }
                }
            }

            model.put("suggestedQuery", suggestion);
            model.put("articlesTotal", new Integer(_resultsPager.getArticlesTotal()));
            model.put("articles", _resultsPager.getArticles (page, pageSize));
            model.put("schoolsTotal", new Integer(_resultsPager.getSchoolsTotal()));
            model.put("schools", _resultsPager.getSchools(page, pageSize));
            model.put("districtsTotal", new Integer(_resultsPager.getDistrictsTotal()));
            model.put("districts", _resultsPager.getDistricts(page, pageSize));
            model.put("pageSize", new Integer(pageSize));
        }

        return new ModelAndView("search", "results", model);
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
     * @param stateManager
     */
    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }

    /**
     * A setter for Spring
     * @param spellCheckSearcher
     */
    public void setSpellCheckSearcher(SpellCheckSearcher spellCheckSearcher) {
        _spellCheckSearcher = spellCheckSearcher;
    }
}