package gs.web.status;

import gs.data.dao.IDao;
import gs.data.dao.IPartitionDao;
import gs.data.state.State;
import gs.data.test.Test;
import gs.data.test.TestState;
import gs.data.util.StackTraceUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Controller for showing the build _versionProperties and database connectivity check
 */
public class MonitorController implements Controller {

    /**
     * The bean name configured in pages-servlet.xml
     */
    public static final String BEAN_ID = "/status/monitor.page";

    private static final Log _log = LogFactory.getLog(MonitorController.class);

    /**
     * The JSP view to use
     */
    private String _viewName;

    /**
     * The generic base Dao
     */
    private IDao _dao;

    /**
     * The partition Dao to talk to the state databases
     */
    private IPartitionDao _partitionDao;

    /**
     * The version of GSWeb
     */
    private Properties _versionProperties;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Map model = new HashMap();

        // Set the version
        model.put("version", _versionProperties.getProperty("gsweb.buildtime"));

        // Test reading and writing to the main gs_school database
        boolean mainReadWrite = false;
        String mainError = "";
        try {
            Test testWrite = new Test();
            _dao.saveObject(testWrite);
            Test testRead = (Test) _dao.getObject(Test.class, testWrite.getId());
            _dao.removeObject(Test.class, testRead.getId());
            mainReadWrite = true;
        } catch (Exception e) {
            _log.error("Error reading and writing to the main database", e);
            mainError = StackTraceUtil.getStackTrace(e);
        }
        model.put("mainReadWrite", new Boolean(mainReadWrite));
        model.put("mainError", mainError);

        // Test reading and writing to a state database
        boolean stateReadWrite = false;
        String stateError = "";
        try {
            TestState testStateWrite = new TestState();
            _partitionDao.saveObject(testStateWrite, State.CA);
            TestState testStateRead = (TestState) _partitionDao.getObject(TestState.class, State.CA, testStateWrite.getId());
            _partitionDao.removeObject(TestState.class, State.CA, testStateRead.getId());
            stateReadWrite = true;
        } catch (Exception e) {
            _log.error("Error reading and writing to the state database", e);
            mainError = StackTraceUtil.getStackTrace(e);
        }
        model.put("stateReadWrite", new Boolean(stateReadWrite));
        model.put("stateError", stateError);

        return new ModelAndView(_viewName, model);
    }

    public IPartitionDao getPartitionDao() {
        return _partitionDao;
    }

    public void setPartitionDao(IPartitionDao partitionDao) {
        this._partitionDao = partitionDao;
    }

    public IDao getDao() {
        return _dao;
    }

    public void setDao(IDao dao) {
        this._dao = dao;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        this._viewName = viewName;
    }

    public Properties getVersionProperties() {
        return _versionProperties;
    }

    public void setVersionProperties(Properties versionProperties) {
        this._versionProperties = versionProperties;
    }
}
