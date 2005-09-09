package gs.web.status;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.search.IndexDir;
import gs.data.search.Indexer;

import java.io.IOException;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SearchManagerController implements Controller {

    public static final String BEAN_ID = "/status/systemtest.page";
    private IndexDir _indexDir;
    private Indexer _indexer;
    private static Log log = LogFactory.getLog(SearchManagerController.class);

    String time = null;
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {


        Identity ident = (Identity)request.getAttribute("identity");
        if (ident == null) {
            log.debug("ident is null");
            //return new ModelAndView (new RedirectView("login.page"));
        } else {
            log.debug("ident: " + ident.getUsername());
        }

        if (request.getParameter("start") != null) {

            long start = System.currentTimeMillis();
            _indexer.index(_indexDir.getMainDirectory(), _indexDir.getSpellCheckDirectory());
            long end = System.currentTimeMillis();
            long totalSeconds = (end-start)/1000;
            long minutes = totalSeconds/60;
            long seconds = totalSeconds % 60;
            StringBuffer buf = new StringBuffer();
            buf.append(String.valueOf(minutes));
            buf.append(":");
            buf.append(String.valueOf(seconds));
            time = buf.toString();
        }
        return new ModelAndView ("status/searchmanager", "time", time);
    }

    public void setIndexer(Indexer indexer) {
        _indexer = indexer;
    }

    public void setIndexDir(IndexDir indexDir) {
        _indexDir = indexDir;
    }

}
