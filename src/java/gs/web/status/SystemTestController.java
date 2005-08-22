package gs.web.status;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
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
public class SystemTestController implements Controller {

    public static final String BEAN_ID = "/status/systemtest.page";
    private IndexDir _indexDir;
    private Indexer _indexer;
    private static Log log = LogFactory.getLog(SystemTestController.class);

    String time = null;
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getParameter("start") != null) {
            long start = System.currentTimeMillis();
            _indexer.index(_indexDir.getMainDirectory(), _indexDir.getSpellCheckDirectory());
            long end = System.currentTimeMillis();
            long t = (end-start)/1000;
            time = String.valueOf(t);
        }
        return new ModelAndView ("/status/systemtest", "time", time);
    }

    public void setIndexer(Indexer indexer) {
        _indexer = indexer;
    }

    public void setIndexDir(IndexDir indexDir) {
        _indexDir = indexDir;
    }

}
