package gs.web.status;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.search.SearchResultSet;
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

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        _indexer.index(_indexDir);
        return new ModelAndView ("/status/systemtest");
    }

    public void setIndexer(Indexer indexer) {
        _indexer = indexer;
    }

    public void setIndexDir(IndexDir indexDir) {
        _indexDir = indexDir;
    }

}
