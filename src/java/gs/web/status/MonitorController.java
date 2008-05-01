package gs.web.status;

import gs.data.dao.DatabaseTestEntity;
import gs.data.dao.IDao;
import gs.data.dao.IPartitionDao;
import gs.data.dao.PartitionedDatabaseTestEntity;
import gs.data.search.Searcher;
import gs.data.state.State;
import gs.data.util.StackTraceUtil;
import gs.web.util.ReadWriteController;
import gs.web.util.VariantConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Controller for showing the build _versionProperties and database connectivity check
 */
public class MonitorController implements ReadWriteController {

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

    /**
     * Used to determine index version number
     */
    private Searcher _searcher;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // This tests the logging system.  Doing it this way seems a lot simpler
        // than creating a FormController, etc.
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

        Map<String, Object> model = new HashMap<String, Object>();

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
            DatabaseTestEntity testWrite = new DatabaseTestEntity();
            _dao.saveObject(testWrite);
            DatabaseTestEntity testRead = (DatabaseTestEntity) _dao.getObject(DatabaseTestEntity.class, testWrite.getId());
            _dao.removeObject(DatabaseTestEntity.class, testRead.getId());
            mainReadWrite = true;
        } catch (Exception e) {
            _log.error("Error reading and writing to the main database", e);
            mainError = StackTraceUtil.getStackTrace(e);
        }

        // Test reading and writing to a state database
        boolean stateReadWrite = false;
        String stateError = "";
        try {
            PartitionedDatabaseTestEntity testStateWrite = new PartitionedDatabaseTestEntity();
            _partitionDao.saveObject(testStateWrite, State.CA);
            PartitionedDatabaseTestEntity testStateRead = (PartitionedDatabaseTestEntity) _partitionDao.getObject(PartitionedDatabaseTestEntity.class, State.CA, testStateWrite.getId());
            _partitionDao.removeObject(PartitionedDatabaseTestEntity.class, State.CA, testStateRead.getId());
            stateReadWrite = true;
        } catch (Exception e) {
            _log.error("Error reading and writing to the state database", e);
            mainError = StackTraceUtil.getStackTrace(e);
        }
        model.put("mainReadWrite", Boolean.valueOf(mainReadWrite));
        model.put("mainError", mainError);
        model.put("stateReadWrite", Boolean.valueOf(stateReadWrite));
        model.put("stateError", stateError);
        model.put("environment", getEnvironmentMap());
        model.put("abConfiguration", VariantConfiguration.convertABConfigurationToString());

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

        model.put("indexVersion", getIndexVersion());

        model.put("x_cluster_client_ip", request.getHeader("HTTP_X_CLUSTER_CLIENT_IP"));
        model.put("remote_addr", request.getRemoteAddr());

        return new ModelAndView(_viewName, model);
    }

    public void setPartitionDao(IPartitionDao partitionDao) {
        this._partitionDao = partitionDao;
    }

    public void setDao(IDao dao) {
        this._dao = dao;
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

    private String getIndexVersion() {
        SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
        Date d = new Date(_searcher.getIndexVersion());
        return format.format(d);
    }

    private Map getEnvironmentMap() {

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
        StringBuffer osBuffer = new StringBuffer(props.getProperty("os.name"));
        osBuffer.append(" ");
        osBuffer.append(props.getProperty("os.version"));
        env.put("Operating System", osBuffer.toString());
        env.put("log4j.configuration", props.getProperty("log4j.configuration"));
        String mailappender = "not configured";
//        if (((Logger)_log).getAppender("mail") != null) {
//            mailappender = "configured";
//        }
        env.put("log4j.mailappender", mailappender);

        Runtime rt = Runtime.getRuntime();
        env.put("memory - total", new Long(rt.totalMemory()));
        env.put("memory - max", new Long(rt.maxMemory()));
        env.put("memory - free", new Long(rt.freeMemory()));


        return env;
    }


    public void setSearcher(Searcher searcher) {
        _searcher = searcher;
    }
}
