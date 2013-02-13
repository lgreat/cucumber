package gs.web.status;

import gs.data.search.GsSolrQuery;
import gs.data.search.GsSolrSearcher;
import gs.data.search.SolrConnectionManager;
import gs.data.search.beans.SolrSchoolSearchResult;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.util.context.SessionContextUtil;
import gs.data.admin.IPropertyDao;
import org.apache.solr.client.solrj.SolrServer;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import javax.servlet.http.Cookie;
import java.util.Map;
import java.lang.management.MemoryUsage;

import static org.easymock.classextension.EasyMock.*;

/**
 * Tests the MonitorController
 */
public class MonitorControllerTest extends BaseControllerTestCase {

    private MonitorController _controller;
    private IPropertyDao _propertyDao;
    private SolrConnectionManager _solrConnectionManager;
    private GsSolrSearcher _gsSolrSearcher;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = (MonitorController) getApplicationContext().getBean(MonitorController.BEAN_ID);
        _propertyDao = createStrictMock(IPropertyDao.class);
        _controller.setPropertyDao(_propertyDao);
        _solrConnectionManager = createStrictMock(SolrConnectionManager.class);
        _gsSolrSearcher = createStrictMock(GsSolrSearcher.class);
        _controller.setSolrConnectionManager(_solrConnectionManager);
        _controller.setGsSolrSearcher(_gsSolrSearcher);
    }

    private void replayAllMocks() {
        replayMocks(_solrConnectionManager, _gsSolrSearcher);
    }

    private void resetAllMocks() {
        resetMocks(_solrConnectionManager, _gsSolrSearcher);
    }

    private void verifyAllMocks() {
        verifyMocks(_solrConnectionManager, _gsSolrSearcher);
    }

    /**
     * The handle request method connects to the database and gets the build version
     */
    public void testHandleRequest() throws Exception {
        expect(_solrConnectionManager.getSolrReadOnlyServerUrl()).andReturn("foo");
        expect(_solrConnectionManager.getSolrReadWriteServerUrl()).andReturn("bar");
        expect(_solrConnectionManager.getReadOnlySolrServer()).andReturn(createStrictMock(SolrServer.class));
        expect(_solrConnectionManager.getReadWriteSolrServer()).andReturn(createStrictMock(SolrServer.class));
        expect(_gsSolrSearcher.search(isA(GsSolrQuery.class), eq(SolrSchoolSearchResult.class))).andReturn(null);
        replayAllMocks();
        ModelAndView mv = _controller.handleRequest(getRequest(), getResponse());
        verifyAllMocks();

        assertTrue(mv.getViewName().indexOf("status") > -1);
        Map model = mv.getModel();
        assertNotNull(model);
        assertTrue(((String) model.get("version")).length() > 0);
        assertTrue(((String) model.get("hostname")).length() > 0);
        assertTrue(((String) model.get("branch")).length() > 3);
        assertTrue(((String) model.get("fisheyeGsweb")).indexOf("GSWeb") > -1);
        assertTrue(((String) model.get("fisheyeGsdata")).indexOf("GSData") > -1);
        assertTrue((Boolean) model.get("mainReadWrite"));
        assertEquals(model.get("mainError"), "");
        assertTrue((Boolean) model.get("stateReadWrite"));
        assertEquals(model.get("stateError"), "");
        assertEquals(1, getRequest().getSession(false).getAttribute("hitcount"));
        assertNotNull(getRequest().getSession(false).getAttribute("thishost"));
        assertNotNull(getRequest().getSession(false).getAttribute("lasthost"));
        assertTrue(((String) model.get("indexVersion")).length() > 0);
        Map environment = (Map)model.get("environment");
        assertNotNull(environment);
        // assertNotNull(environment.get("log4j.configuration"));
        assertNotNull(environment.get("log4j.mailappender"));
        assertTrue(((String) model.get("abConfiguration")).length() > 0);

        GsMockHttpServletRequest request = getRequest();
        request.addParameter("logmessage", "test");
        request.setMethod("POST");
        // This exercises the logging code and tests hitcount incrementing
        resetAllMocks();
        expect(_solrConnectionManager.getSolrReadOnlyServerUrl()).andReturn("foo");
        expect(_solrConnectionManager.getSolrReadWriteServerUrl()).andReturn("bar");
        expect(_solrConnectionManager.getReadOnlySolrServer()).andReturn(createStrictMock(SolrServer.class));
        expect(_solrConnectionManager.getReadWriteSolrServer()).andReturn(createStrictMock(SolrServer.class));
        expect(_gsSolrSearcher.search(isA(GsSolrQuery.class), eq(SolrSchoolSearchResult.class))).andReturn(null);
        replayAllMocks();
        mv = _controller.handleRequest(getRequest(), getResponse());
        verifyAllMocks();
        assertNotNull(mv);
        HttpSession session = request.getSession(true);
        assertEquals(2, session.getAttribute("hitcount"));
    }

    public void testIncrementVersion() {
        String cookieValue = "1";
        Cookie cookie = new Cookie(SessionContextUtil.TRACKING_NUMBER, cookieValue);
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        getRequest().setCookies(new Cookie[] {cookie});

        expect(_propertyDao.getProperty(IPropertyDao.VARIANT_CONFIGURATION)).andReturn("1/1").times(3);
        replay(_propertyDao);
        _controller.incrementVersion(getRequest(), getResponse());
        verify(_propertyDao);

        assertEquals("2", getResponse().getCookie(SessionContextUtil.TRACKING_NUMBER).getValue());
    }

    public void testIncrementVersion2() {
        String cookieValue = "1";
        Cookie cookie = new Cookie(SessionContextUtil.TRACKING_NUMBER, cookieValue);
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        getRequest().setCookies(new Cookie[] {cookie});

        expect(_propertyDao.getProperty(IPropertyDao.VARIANT_CONFIGURATION)).andReturn("5/1").times(6);
        replay(_propertyDao);
        _controller.incrementVersion(getRequest(), getResponse());
        verify(_propertyDao);

        assertEquals("5", getResponse().getCookie(SessionContextUtil.TRACKING_NUMBER).getValue());
    }

    public void testIncrementVersion3() {
        replay(_propertyDao);
        _controller.incrementVersion(getRequest(), getResponse());
        verify(_propertyDao);

        assertNull(getResponse().getCookie(SessionContextUtil.TRACKING_NUMBER));
    }

    public void testIncrementVersion4() {
        String cookieValue = "1";
        Cookie cookie = new Cookie(SessionContextUtil.TRACKING_NUMBER, cookieValue);
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        getRequest().setCookies(new Cookie[] {cookie});

        expect(_propertyDao.getProperty(IPropertyDao.VARIANT_CONFIGURATION)).andReturn("1").times(102);
        replay(_propertyDao);
        _controller.incrementVersion(getRequest(), getResponse());
        verify(_propertyDao);

        assertEquals("Expect original value to be incremented 101 times",
                "102", getResponse().getCookie(SessionContextUtil.TRACKING_NUMBER).getValue());
    }

    public void testGetManagementMap() throws Exception {
        Map<String, MemoryUsage> m = _controller.getManagementMap();
        assertNotNull (m.get(MonitorController.HEAP_USAGE));
        assertNotNull (m.get(MonitorController.NON_HEAP_USAGE));
        //assertNotNull (m.get(MonitorController.PERM_GEN_USAGE));
        //The perm gen pool is not guaranteed to be available.
    }
}
