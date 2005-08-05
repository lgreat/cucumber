package gs.web.search;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import gs.data.search.*;
import gs.data.state.State;
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
    private SessionContext _sessionContext;

    public ModelAndView handleRequestInternal(HttpServletRequest request,
                                              HttpServletResponse response)
            throws Exception {

        SearchResultSet resultSet = null;
        Map model =  new HashMap ();
        //List resultSet = null;

        String queryString = request.getParameter("q");

        if (queryString != null && !queryString.equals("")) {

            StringBuffer queryBuffer = new StringBuffer();
            queryBuffer.append(queryString);

            HttpSession session = request.getSession(true);

            _sessionContext = SessionContext.getInstance(request);

            /**
             * I'm splitting the setting and reading of location because in a
             * production system, the location might be set somewhere else.
             */
            String location = request.getParameter("l");
            if (location != null) {
                _log.debug("location is: " + location);
                if (location.equals("CA")) {
                    _sessionContext.setState(State.CA);
                } else if (location.equals("NY")) {
                    _sessionContext.setState(State.NY);
                } else {
                    _sessionContext.setState(null);
                }
            }

            State state = _sessionContext.getState();
            if (state != null) {
                _log.debug("state: " + state.getLongName());
                queryBuffer.append(" AND state:");
                queryBuffer.append(state.getAbbreviation());
            }

            // now handle search constraints. only deal with this if the
            // type: field has not be used in the query
            // todo: this will break if someone types a query like:  foo AND "type:" OR bar
            if (queryString.indexOf("type:") == -1) {
                String constraint = request.getParameter("c");
                if (constraint != null) {
                    if (constraint.equals("article")) {
                        queryBuffer.insert(0, "type:article AND ");
                    } else if (constraint.equals("school")) {
                        queryBuffer.insert(0, "type:school AND ");
                    }
                }
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

            resultSet = _searcher.search(queryBuffer.toString (), page);

            System.out.println ("sysout list " + resultSet.getList ().size ());
            _log.debug ("log list size: " + resultSet.getList ());
            
            model.put ("hits", resultSet.getList ());
            model.put ("total", new Integer (resultSet.getTotalResults()));
        }

        //return new ModelAndView("search", "results", resultSet);
        return new ModelAndView("search", "results", model);
    }

    public Searcher getSearcher() {
        return _searcher;
    }

    public void setSearcher(Searcher searcher) {
        _searcher = searcher;
    }
}