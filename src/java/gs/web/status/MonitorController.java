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
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;
import java.net.InetAddress;

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

        // This tests the logging system.  Doing it this way seems a lot simpler
        // than creating a FormController, etc.
        if (request != null) {
            String method = request.getMethod();
            if (method != null && method.equalsIgnoreCase("POST")) {
                String logMessage = request.getParameter("logmessage");
                _log.debug(logMessage);
                _log.trace(logMessage);
                _log.info(logMessage);
                _log.warn(logMessage);
                _log.error(logMessage);
                _log.fatal(logMessage);
            }
        }

        Map model = new HashMap();

        // Set the version
        model.put("buildtime", 
                  _versionProperties.getProperty("gsweb.buildtime"));
        model.put("version", 
                  _versionProperties.getProperty("gsweb.version"));

        // Set the hostname
        String hostname = "Unable to resolve hostname";
        try {
            java.net.InetAddress localMachine =
                    java.net.InetAddress.getLocalHost();
            hostname = localMachine.getHostName();
        } catch (java.net.UnknownHostException e) {
            // No need to do anything
        }
        model.put("hostname", hostname);

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
        if (request != null) {
            model.put("environment", getEnvironmentMap(request.getSession().getServletContext()));
        }

        // Test setting some values in the session to try session replication
        HttpSession session = request.getSession(true);

        String thishost = (String) session.getAttribute("thishost");
        if (thishost == null) thishost = "NA";
        session.setAttribute("lasthost", thishost);
        session.setAttribute("thishost", hostname);

        Integer hitcount = (Integer) session.getAttribute("hitcount");
        if (hitcount == null) {
            hitcount = new Integer(1);
        } else {
            hitcount = new Integer(1 + hitcount.intValue());
        }
        session.setAttribute("hitcount", hitcount);


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

    private Map getEnvironmentMap(ServletContext context) {

        Properties props = System.getProperties();
        Map env = new TreeMap();
        /*
        if (context != null) {
            Enumeration en = context.getAttributeNames();
            while(en.hasMoreElements()) {
                env.put(en.nextElement(), context.getAttribute((String)en.nextElement()));
            }
        }
        */

        env.put("Java Version", props.getProperty("java.vm.version"));
        StringBuffer osBuffer = new StringBuffer (props.getProperty("os.name"));
        osBuffer.append(" ");
        osBuffer.append(props.getProperty("os.version"));
        env.put("Operating System", osBuffer.toString());

        Runtime rt = Runtime.getRuntime();
        env.put("memory - total", new Long(rt.totalMemory()));
        env.put("memory - max", new Long(rt.maxMemory()));
        env.put("memory - free", new Long(rt.freeMemory()));


        return env;
    }
}
