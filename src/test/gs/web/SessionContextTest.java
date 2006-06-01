package gs.web;

import gs.data.state.State;
import gs.data.state.StateManager;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.Cookie;

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
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
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

        request.setParameter("cobrand", "family");
        _sessionContextUtil.updateFromParams(request, _mockHttpServletResponse, ctx);
        assertTrue(ctx.isFamilyCobrand());

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
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
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
        request = new GsMockHttpServletRequest();
        ctx = new SessionContext();
        request.setServerName("greatschools.babycenter.com");
        _sessionContextUtil.updateFromParams(request, _mockHttpServletResponse, ctx);
        assertEquals("greatschools.babycenter.com", ctx.getHostName());
        assertTrue(ctx.isCobranded());
        assertEquals("secure.greatschools.net", ctx.getSecureHostName());
    }

    public void testHostMainUrlOnLiveSiteWithCobrandParameter() {
        // Try developer workstation scenario
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
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
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setServerName("azcentral.dev.greatschools.net");
        _sessionContextUtil.updateFromParams(request, _mockHttpServletResponse, ctx);
        assertEquals("azcentral.dev.greatschools.net", ctx.getHostName());
        assertTrue(ctx.isCobranded());
        assertEquals("azcentral", ctx.getCobrand());
        assertEquals("secure.dev.greatschools.net", ctx.getSecureHostName());
    }

    public void testIsYahooCobrand() {
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
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

    public void testIsFamilyCobrand() {
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        SessionContext ctx = new SessionContext();
        request.setServerName("family.greatschools.net");
        _sessionContextUtil.updateFromParams(request, _mockHttpServletResponse, ctx);
        assertEquals(ctx.getHostName(), "family.greatschools.net");
        assertTrue(ctx.isCobranded());
        assertTrue(ctx.isFamilyCobrand());
    }

    public void testHostWithoutPeriod() {
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        SessionContext ctx = new SessionContext();
        request.setServerName("maddy");
        _sessionContextUtil.updateFromParams(request, _mockHttpServletResponse, ctx);
        assertEquals(ctx.getHostName(), "maddy");
        assertTrue(!ctx.isCobranded());
        assertTrue(!ctx.isYahooCobrand());
    }

    public void testDomainWideCookie() {
        SessionContextUtil util = new SessionContextUtil();
        util.setStateManager(new StateManager());

        MockHttpServletResponse response = new MockHttpServletResponse();
        util.setDomainWideCookie(response, "someCookie", "someValue", 100);
        Cookie cookie = response.getCookie("someCookie");
        assertNotNull(cookie);
        assertEquals("someValue", cookie.getValue());
        assertEquals("/", cookie.getPath());
        assertEquals(100, cookie.getMaxAge());

        response = new MockHttpServletResponse();
        util.setDomainWideCookie(response, "someCookie", "someValue", 0);
        cookie = response.getCookie("someCookie");
        assertNotNull(cookie);
        assertEquals("someValue", cookie.getValue());
        assertEquals("/", cookie.getPath());
        assertEquals(0, cookie.getMaxAge());
    }

    public void testStateSetting() {
        SessionContextUtil util = new SessionContextUtil();
        util.setStateManager(new StateManager());
        SessionContext ctx = new SessionContext();

        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertEquals(null, ctx.getState());

        //do not set a state variable
        request.setParameter("bogus", "");
        util.updateStateFromParam(ctx, request, response);
        assertEquals(null, ctx.getState());
        assertEquals(null, getStateFromMockResponse(response));

        //bogus state
        request.setParameter("state", "bo");
        util.updateStateFromParam(ctx, request, response);
        assertEquals(null, ctx.getState());
        assertEquals(null, getStateFromMockResponse(response));

        request.setParameter("state", "");
        util.updateStateFromParam(ctx, request, response);
        assertEquals(null, ctx.getState());
        assertEquals(null, getStateFromMockResponse(response));

        request.setParameter("state", "GA");
        util.updateStateFromParam(ctx, request, response);
        assertEquals(State.GA, ctx.getState());
        assertEquals(State.GA, getStateFromMockResponse(response));

        request.setParameter("state", "fl");
        util.updateStateFromParam(ctx, request, response);
        assertEquals(State.FL, ctx.getState());
        assertEquals(State.FL, getStateFromMockResponse(response));

        request.setParameter("state", "ct/");
        util.updateStateFromParam(ctx, request, response);
        assertEquals(State.CT, ctx.getState());
        assertEquals(State.CT, getStateFromMockResponse(response));

        request.setParameter("state", "x");
        util.updateStateFromParam(ctx, request, response);
        assertEquals(State.CT, ctx.getState());
        assertEquals(State.CT, getStateFromMockResponse(response));

        request.setParameter("state", "xx");
        util.updateStateFromParam(ctx, request, response);
        assertEquals(State.CT, ctx.getState());
        assertEquals(State.CT, getStateFromMockResponse(response));

        request.setParameter("state", "US");
        util.updateStateFromParam(ctx, request, response);
        assertNull(ctx.getState());        
        assertNull(getStateFromMockResponse(response));

    }

    /**
     * MockHttpServletResponse stores cookie values in a list and not a map so
     * it's possible to have cookies in the list with the same name.
     *
     * This method performs operations on the LAST cookie inserted into the response.
     */
    private State getStateFromMockResponse(MockHttpServletResponse response) {
        StateManager sm = new StateManager();
        Cookie [] cookies = response.getCookies();
        String state = null;

        for (int i=0; i<cookies.length; i++) {
            Cookie c = cookies[i];
            if ("state".equals(c.getName())) {
                state = c.getValue();
            }
        }

        if (state == null) {
            return null;
        } else {
            return sm.getState(state);
        }
    }
    public void testABVersion() {
        SessionContextUtil util = new SessionContextUtil();
        util.setStateManager(new StateManager());
        SessionContext ctx = new SessionContext();

        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setRemoteAddr("123.456.789.012");
        util.updateFromRequestAttributes(request, ctx);
        assertEquals("a", ctx.getABVersion());

        GsMockHttpServletRequest bRequest = new GsMockHttpServletRequest();
        bRequest.setRemoteAddr("123.456.789.123");
        util.updateFromRequestAttributes(bRequest, ctx);
        assertEquals("b", ctx.getABVersion());

        GsMockHttpServletRequest cRequest = new GsMockHttpServletRequest();
        cRequest.setRemoteAddr("123.456.789.000");
        util.updateFromRequestAttributes(cRequest, ctx);
        assertEquals("a", ctx.getABVersion());

    }
}
