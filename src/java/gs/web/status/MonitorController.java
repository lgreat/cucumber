package gs.web.status;

import gs.data.dao.DatabaseTestEntity;
import gs.data.dao.IDao;
import gs.data.dao.IPartitionDao;
import gs.data.dao.PartitionedDatabaseTestEntity;
import gs.data.search.Searcher;
import gs.data.state.State;
import gs.data.util.CmsUtil;
import gs.data.util.CommunityUtil;
import gs.data.util.StackTraceUtil;
import gs.data.admin.IPropertyDao;
import gs.web.community.UploadAvatarHoverController;
import gs.web.request.RequestInfo;
import gs.web.search.CmsRelatedFeatureCacheManager;
import gs.web.util.ReadWriteController;
import gs.web.util.VariantConfiguration;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContextUtil;
import java.text.ParseException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.MemoryPoolMXBean;

/**
 * Controller for showing the build _versionProperties and database connectivity check
 */
public class MonitorController implements ReadWriteController {

    /**
     * The bean name configured in pages-servlet.xml
     */
    public static final String BEAN_ID = "/status/monitor.page";
    public static final String SECRET_NUMBER = "58742";
    public static final String CMS_CACHE_REFRESH_PARAM = "cmscacherefresh";

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

    private IPropertyDao _propertyDao;

    /**
     * The version of GSWeb
     */
    private Properties _versionProperties;

    /**
     * Used to determine index version number
     */
    private Searcher _searcher;

    private CmsRelatedFeatureCacheManager _cmsRelatedFeatureCacheManager;
    
    long[] _blackHole;
    long[] _blackHole2;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, ParseException {
        
        if (request.getParameter("die_893u4odsjf982348") != null) {
            selfDestruct();
        }

        if (request.getParameter(CMS_CACHE_REFRESH_PARAM) != null) {
            cmsCacheRefresh();
        }
        
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
        String buildtime = _versionProperties.getProperty("gsweb.buildtime");
        String version = _versionProperties.getProperty("gsweb.version");
        String branch = _versionProperties.getProperty("gsweb.branch");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            Date buildTimeDate = sdf.parse(buildtime);
            sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
            buildtime = sdf.format(buildTimeDate);
        } catch (Exception e) {
            // ignore
        }
        model.put("buildtime", buildtime);
        model.put("version", version);

        // Set the fisheye url to compare against
        model.put("branch", branch);
        model.put("fisheyeGsweb", generateFisheyeUrl(branch, buildtime, "GSWeb"));
        model.put("fisheyeGsdata", generateFisheyeUrl(branch, buildtime, "GSData"));

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
        model.put("mainReadWrite", mainReadWrite);
        model.put("mainError", mainError);
        model.put("stateReadWrite", stateReadWrite);
        model.put("stateError", stateError);
        model.put("environment", getEnvironmentMap());
        model.put("management", getManagementMap());
        model.put("rtmemory", getRuntimeMemoryMap());

        if (request.getParameter("increment") != null) {
            incrementVersion(request, response);
        }
        model.put("abConfiguration", VariantConfiguration.convertABConfigurationToString());

        model.put("mobileSiteEnabled", isMobileSiteEnabled(request));

        // Test setting some values in the session to try session replication
        HttpSession session = request.getSession(true);

        String thishost = (String) session.getAttribute("thishost");
        if (thishost == null) thishost = "NA";
        session.setAttribute("lasthost", thishost);
        session.setAttribute("thishost", hostname);

        Integer hitcount = (Integer) session.getAttribute("hitcount");
        if (hitcount == null) {
            hitcount = 1;
        } else {
            hitcount++;
        }
        session.setAttribute("hitcount", hitcount);

        model.put("indexVersion", getIndexVersion());

        model.put("x_cluster_client_ip_attr", request.getAttribute("HTTP_X_CLUSTER_CLIENT_IP"));
        model.put("remote_addr", request.getRemoteAddr());

        if (StringUtils.equals(request.getParameter("secret_number"), SECRET_NUMBER)) {
            model.put("showExtra", true);
            model.put("avatarURLPrefix", CommunityUtil.getAvatarURLPrefix());
            model.put("avatarUploadURL", System.getProperty(UploadAvatarHoverController.POST_URL_PROPERTY_KEY));
            model.put("cms_enabled", CmsUtil.isCmsEnabled());
            model.put("cms_db_url", System.getProperty("cms.db.url"));
            model.put("solr_ro_server_url", System.getProperty("solr.ro.server.url"));
            model.put("solr_rw_server_url", System.getProperty("solr.rw.server.url"));
        }


        return new ModelAndView(_viewName, model);
    }

    private void cmsCacheRefresh(){
        _cmsRelatedFeatureCacheManager.refresh();
    }

    protected String generateFisheyeUrl(String branch, String buildtime, String module) throws ParseException {
        Date builddate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(buildtime);
        String fisheyeBuildTime = new SimpleDateFormat("yyyy-MM-dd'T'HH'%3A'mm'%3A'ss.00").format(builddate);
        return "http://cvsweb.greatschools.org/search/gsrepo/" + module +
                "?ql=select+revisions+from+dir+%2F" + module + "+where+(" + ("HEAD".equals(branch)?"":"on+branch+" + branch + "+and+") + 
		"date+%3E%3D+" + fisheyeBuildTime + ")+group+by+changeset&amp;refresh=y";
    }

    static final String HEAP_USAGE = "Heap Usage";
    static final String NON_HEAP_USAGE = "Non-heap Usage";
    static final String PERM_GEN_USAGE = "Perm Gen Usage";

    protected Map<String, MemoryUsage> getManagementMap() {
        Map<String, MemoryUsage> m = new HashMap<String, MemoryUsage>();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        m.put(HEAP_USAGE, heapUsage);
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        m.put(NON_HEAP_USAGE, nonHeapUsage);
        List<MemoryPoolMXBean> memPoolBeans = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean bean : memPoolBeans) {
            if (bean.getName().toLowerCase().contains("perm gen")) {
                m.put(PERM_GEN_USAGE, bean.getUsage());
            }
        }
        return m;
    }

    static final String RUNTIME_MEMORY_TOTAL = "Runtime Memory - Total";
    static final String RUNTIME_MEMORY_MAX = "Runtime Memory - MAX";
    static final String RUNTIME_MEMORY_FREE = "Runtime Memory - FREE";

    protected Map<String, Long> getRuntimeMemoryMap() {
        Map<String, Long> m = new HashMap<String, Long>();
        Runtime rt = Runtime.getRuntime();
        m.put(RUNTIME_MEMORY_TOTAL, rt.totalMemory());
        m.put(RUNTIME_MEMORY_MAX, rt.maxMemory());
        m.put(RUNTIME_MEMORY_FREE, rt.freeMemory());
        return m;
    }

    protected void incrementVersion(HttpServletRequest request, HttpServletResponse response) {
        Cookie trackingNumber = findCookie(request, SessionContextUtil.TRACKING_NUMBER);
        if (trackingNumber == null) {
            return;
        }
        long secondsSinceEpoch;
        String cookieValue = trackingNumber.getValue();
        try {
            // Extract the time from the tracking number cookie (e.g. 180654739)
            secondsSinceEpoch = Long.valueOf(cookieValue);
        } catch (Exception e) {
            return;
        }
        long newValue = VariantConfiguration.getNumberForNextVariant(secondsSinceEpoch, _propertyDao);

        String newVariant = VariantConfiguration.getVariant(newValue, _propertyDao);
        trackingNumber.setValue(String.valueOf(newValue));
        trackingNumber.setPath("/");
        trackingNumber.setMaxAge(-1);
        if (!UrlUtil.isDeveloperWorkstation(request.getServerName())) {
            // don't set domain for developer workstations so they can still access the cookie!!
            trackingNumber.setDomain(".greatschools.org");
        }

        response.addCookie(trackingNumber);
        SessionContextUtil.getSessionContext(request).setAbVersion(newVariant);
    }


    protected boolean isMobileSiteEnabled(HttpServletRequest request) {
        return RequestInfo.getRequestInfo(request).isMobileSiteEnabled();
    }

    protected boolean isFruitcakeEnabled(HttpServletRequest request) {
        Cookie cookie = findCookie(request, RequestInfo.FRUITCAKE_ENABLED_COOKIE_NAME);
        return (cookie != null && Boolean.TRUE.equals(Boolean.valueOf(cookie.getValue())));
    }

    protected void enableFruitcake(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie(RequestInfo.FRUITCAKE_ENABLED_COOKIE_NAME,"true");
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        if (!UrlUtil.isDeveloperWorkstation(request.getServerName())) {
            // don't set domain for developer workstations so they can still access the cookie!!
            cookie.setDomain(".greatschools.org");
        }
        response.addCookie(cookie);
    }

    protected void disableFruitcake(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie(RequestInfo.FRUITCAKE_ENABLED_COOKIE_NAME, "false");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        if (!UrlUtil.isDeveloperWorkstation(request.getServerName())) {
            // don't set domain for developer workstations so they can still access the cookie!!
            cookie.setDomain(".greatschools.org");
        }
        response.addCookie(cookie);
    }

    protected Cookie findCookie(HttpServletRequest request, String cookieName) {
        Cookie cookies[] = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
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
        Map<String, Object> env = new TreeMap<String, Object>();
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
        return env;
    }
    
    public void selfDestruct() {
        
        _blackHole = new long[(int)Math.pow(2,26)];
        _blackHole2 = new long[(int)Math.pow(2,26)];
    }

    public void setSearcher(Searcher searcher) {
        _searcher = searcher;
    }

    public IPropertyDao getPropertyDao() {
        return _propertyDao;
    }

    public void setPropertyDao(IPropertyDao propertyDao) {
        _propertyDao = propertyDao;
    }

    public void setCmsRelatedFeatureCacheManager(CmsRelatedFeatureCacheManager cmsRelatedFeatureCacheManager) {
        _cmsRelatedFeatureCacheManager = cmsRelatedFeatureCacheManager;
    }
}
