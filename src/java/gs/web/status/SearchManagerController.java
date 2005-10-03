package gs.web.status;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import gs.data.search.IndexDir;
import gs.data.search.Indexer;
import gs.data.state.StateManager;
import gs.data.state.State;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;

/**
 * Controls re-indexing of Lucene indexes.
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
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
                } else {
                    long start = 0;

                    if (request.getParameter("all") != null) {
                        start = System.currentTimeMillis();
                        _indexer.index(StateManager.getList(),
                                _indexDir.getMainDirectory(), _indexDir.getSpellCheckDirectory());
                    } else if (request.getParameter("test") != null) {
                        start = System.currentTimeMillis();
                        List states = new ArrayList();
                        states.add(State.CA);
                        states.add(State.WY);
                        states.add(State.NY);
                        states.add(State.AK);
                        _indexer.index(states,
                                _indexDir.getMainDirectory(), _indexDir.getSpellCheckDirectory());
                    }

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
