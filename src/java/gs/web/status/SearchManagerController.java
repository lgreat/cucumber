package gs.web.status;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import gs.data.search.IndexDir;
import gs.data.search.Indexer;
import gs.data.state.StateManager;
import gs.data.state.State;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * Controls re-indexing of Lucene indexes.  User may select to index all states
 * or a test subset including the following states:
 *
 * <ul><li>Alaska</li>
 *     <li>California</li>
 *     <li>NY</li>
 *     <li>WY</li>
 * </ul>
 * These test states may be indexed while connected to any database as there is
 * local test data for these states included in our dev environments.
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.org>
 */
public class SearchManagerController extends AbstractController {

    public static final String BEAN_ID = "/status/systemtest.page";
    private IndexDir _indexDir;
    private Indexer _indexer;
    private static Log log = LogFactory.getLog(SearchManagerController.class);

    String time = null;

    public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session != null) {

            Identity ident = (Identity) session.getAttribute("identity");

            if (ident != null) {

                session.setAttribute("authenticated", Boolean.TRUE);
                log.info("SearchManager login: " + ident.getUsername());

                if (request.getParameter("logout") != null) {
                    session.invalidate();
                    return new ModelAndView(new RedirectView("login.page"));
                } else if (request.getParameter("index") != null) {
                    long start = System.currentTimeMillis();
                    List states = null;

                    if (request.getParameter("all") != null) {
                        states = StateManager.getList();
                    } else if (request.getParameter("test") != null) {
                        states = new ArrayList();
                        states.add(State.AK);
                        states.add(State.CA);
                        states.add(State.NY);
                        states.add(State.WY);
                    } else if (request.getParameter("ak") != null) {
                        states = new ArrayList();
                        states.add(State.AK);
                    } else if (request.getParameter("ca") != null) {
                        states = new ArrayList();
                        states.add(State.CA);
                    } else if (request.getParameter("wy") != null) {
                        states = new ArrayList();
                        states.add(State.WY);
                    } else if (request.getParameter("la") != null) {
                        states = new ArrayList();
                        states.add(State.LA);
                    }

                    _indexer.index(states,
                            _indexDir.getMainDirectory(), _indexDir.getSpellCheckDirectory());

                    if (start > 0) {
                        long end = System.currentTimeMillis();
                        long totalSeconds = (end - start) / 1000;
                        long minutes = totalSeconds / 60;
                        long seconds = totalSeconds % 60;
                        StringBuffer buf = new StringBuffer();
                        buf.append(String.valueOf(minutes));
                        buf.append(":");
                        buf.append(String.valueOf(seconds));
                        time = buf.toString();
                    }
                }
                return new ModelAndView("status/searchmanager", "time", time);


            } else {
                log.info("SearchManager login identity is null!");
            }
        } else {
            log.info("SearchManager session is null: redirecting to login page");
        }
        return new ModelAndView(new RedirectView("login.page"));
    }

    public void setIndexer(Indexer indexer) {
        _indexer = indexer;
    }

    public void setIndexDir(IndexDir indexDir) {
        _indexDir = indexDir;
    }

}
