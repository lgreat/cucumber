package gs.web;

import gs.data.state.State;
import gs.data.state.StateManager;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests for the SessionContext object
 *
 * @author Todd Huss <mailto:thuss@greatschools.net>
 */
public class SessionContextTest extends BaseTestCase {


    private SessionContextUtil _sessionContextUtil;
    private MockHttpServletResponse _mockHttpServletResponse;

    protected void setUp() throws Exception {
        super.setUp();

        _sessionContextUtil = new SessionContextUtil();
        _sessionContextUtil.setStateManager(new StateManager());
        _mockHttpServletResponse = new MockHttpServletResponse();
    }

    public void testHostDeveloperWorkstation() {
        // Try developer workstation scenario
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("localhost");

        SessionContext ctx = new SessionContext();

        _sessionContextUtil.updateFromParams(request, _mockHttpServletResponse, ctx);
        assertEquals(ctx.getHostName(), "dev.greatschools.net");
        assertTrue(!ctx.isCobranded());
        assertTrue(!ctx.isYahooCobrand());

        // Add the cobrand parameter
        request.setParameter("cobrand", "number1expert");
        _sessionContextUtil.updateFromParams(request, _mockHttpServletResponse, ctx);
        assertEquals("number1expert.dev.greatschools.net", ctx.getHostName());
        assertTrue(ctx.isCobranded());

        request.setParameter("cobrand", "yahoo");
        _sessionContextUtil.updateFromParams(request, _mockHttpServletResponse, ctx);
        assertTrue(ctx.isYahooCobrand());
        assertEquals("secure.dev.greatschools.net", ctx.getSecureHostName());

    }

    /**
     * Make sure we can get a value back for advertising being online
     */
    public void testAdvertising() {
        ISessionFacade sess = (ISessionFacade) getApplicationContext().getBean("sessionContext");
        assertNotNull(sess);
        assertTrue(sess.isAdvertisingOnline());
    }

    public void testHostCobrandUrlOnLiveSite() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        SessionContext ctx = new SessionContext();
        request.setServerName("sfgate.greatschools.net");
        _sessionContextUtil.updateFromParams(request, _mockHttpServletResponse, ctx);
        assertEquals("sfgate.greatschools.net", ctx.getHostName());
        assertTrue(ctx.isCobranded());
        assertEquals(ctx.getCobrand(), "sfgate");
        assertNull(ctx.getState());
        assertTrue(ctx.getStateOrDefault().equals(State.CA));

        // Add the state parameter
        request.setParameter("state", "wy");
        _sessionContextUtil.updateFromParams(request, _mockHttpServletResponse, ctx);
        assertTrue(ctx.getState().equals(State.WY));

        // Now try a non-standard URL cobrand such as babycenter
        request = new MockHttpServletRequest();
        ctx = new SessionContext();
        request.setServerName("greatschools.babycenter.com");
        _sessionContextUtil.updateFromParams(request, _mockHttpServletResponse, ctx);
        assertEquals("greatschools.babycenter.com", ctx.getHostName());
        assertTrue(ctx.isCobranded());
        assertEquals("secure.greatschools.net", ctx.getSecureHostName());
    }

    public void testHostMainUrlOnLiveSiteWithCobrandParameter() {
        // Try developer workstation scenario
        MockHttpServletRequest request = new MockHttpServletRequest();
        SessionContext ctx = new SessionContext();
        request.setServerName("www.greatschools.net");
        request.setParameter("cobrand", "framed");
        _sessionContextUtil.updateFromParams(request, _mockHttpServletResponse, ctx);
        assertEquals("framed.greatschools.net", ctx.getHostName());
        assertTrue(ctx.isCobranded());
        assertEquals(ctx.getCobrand(), "framed");
        assertEquals("secure.greatschools.net", ctx.getSecureHostName());
    }

    public void testHostCobrandUrlOnDevSite() {
        // Try developer workstation scenario
        SessionContext ctx = new SessionContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("azcentral.dev.greatschools.net");
        _sessionContextUtil.updateFromParams(request, _mockHttpServletResponse, ctx);
        assertEquals("azcentral.dev.greatschools.net", ctx.getHostName());
        assertTrue(ctx.isCobranded());
        assertEquals("azcentral", ctx.getCobrand());
        assertEquals("secure.dev.greatschools.net", ctx.getSecureHostName());
    }

    public void testIsYahooCobrand() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        SessionContext ctx = new SessionContext();
        request.setServerName("yahoo.greatschools.net");
        _sessionContextUtil.updateFromParams(request, _mockHttpServletResponse, ctx);
        assertEquals(ctx.getHostName(), "yahoo.greatschools.net");
        assertTrue(ctx.isCobranded());
        assertTrue(ctx.isYahooCobrand());

        request.setServerName("yahooed.greatschools.net");
        _sessionContextUtil.updateFromParams(request, _mockHttpServletResponse, ctx);
        assertEquals(ctx.getHostName(), "yahooed.greatschools.net");
        assertTrue(ctx.isCobranded());
        assertTrue(ctx.isYahooCobrand());
    }

    public void testHostWithoutPeriod() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        SessionContext ctx = new SessionContext();
        request.setServerName("maddy");
        _sessionContextUtil.updateFromParams(request, _mockHttpServletResponse, ctx);
        assertEquals(ctx.getHostName(), "maddy");
        assertTrue(!ctx.isCobranded());
        assertTrue(!ctx.isYahooCobrand());
    }

    public void testStateSetting() {
        SessionContextUtil util = new SessionContextUtil();
        util.setStateManager(new StateManager());

        SessionContext ctx = new SessionContext();

        MockHttpServletRequest request = new MockHttpServletRequest();

        assertEquals(null, ctx.getState());

        request.setParameter("state", "");
        util.updateStateFromParam(ctx, request);
        assertEquals(null, ctx.getState());

        request.setParameter("state", "GA");
        util.updateStateFromParam(ctx, request);
        assertEquals(State.GA, ctx.getState());

        request.setParameter("state", "fl");
        util.updateStateFromParam(ctx, request);
        assertEquals(State.FL, ctx.getState());

        request.setParameter("state", "ct/");
        util.updateStateFromParam(ctx, request);
        assertEquals(State.CT, ctx.getState());

        request.setParameter("state", "x");
        util.updateStateFromParam(ctx, request);
        assertEquals(State.CT, ctx.getState());

        request.setParameter("state", "xx");
        util.updateStateFromParam(ctx, request);
        assertEquals(State.CT, ctx.getState());
    }

    public void testABVersion() {
        SessionContextUtil util = new SessionContextUtil();
        util.setStateManager(new StateManager());
        SessionContext ctx = new SessionContext();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("123.456.789.012");
        util.updateFromRequestAttributes(request, ctx);
        assertEquals("a", ctx.getABVersion());

        MockHttpServletRequest bRequest = new MockHttpServletRequest();
        bRequest.setRemoteAddr("123.456.789.123");
        util.updateFromRequestAttributes(bRequest, ctx);
        assertEquals("b", ctx.getABVersion());

        MockHttpServletRequest cRequest = new MockHttpServletRequest();
        cRequest.setRemoteAddr("123.456.789.000");
        util.updateFromRequestAttributes(cRequest, ctx);
        assertEquals("a", ctx.getABVersion());

    }
}
