package gs.web.search;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import gs.data.search.*;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */

public class SearchController extends AbstractController {

    public static final String BEAN_ID = "/search.page";
    private static Log _log = LogFactory.getLog(SearchController.class);
    private Searcher _searcher;

    public ModelAndView handleRequestInternal (HttpServletRequest request,
                                               HttpServletResponse response)
                                    throws Exception {

        SearchResultSet resultSet = null;
        String queryString = request.getParameter("q");
        HttpSession session = request.getSession (true);

        // When changing styles, keep the current search active.
        if (request.getParameter ("style") != null) {
            queryString = (String) session.getAttribute ("query");
            session.setAttribute ("q", queryString);
        }

        if (queryString != null && !queryString.equals ("")) {
            session.setAttribute ("query", queryString);
            resultSet = _searcher.search(queryString);
        }

        return new ModelAndView("search", "results", resultSet);
    }

    public Searcher getSearcher() {
        return _searcher;
    }

    public void setSearcher(Searcher searcher) {
        _searcher = searcher;
    }
}