package gs.web;

import gs.data.state.State;
import gs.data.state.StateManager;
import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests for the SessionContext object
 *
 * @author Todd Huss <mailto:thuss@greatschools.net>
 */
public class SessionContextTest extends TestCase {


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
        assertTrue(!ctx.isAdFree());
        assertTrue(!ctx.isCobranded());
        assertTrue(!ctx.isYahooCobrand());

        // Add the cobrand parameter
        request.addParameter("cobrand", "number1expert");
        _sessionContextUtil.updateFromParams(request, _mockHttpServletResponse, ctx);
        assertEquals("number1expert.dev.greatschools.net", ctx.getHostName());
        assertTrue(ctx.isAdFree());
        assertTrue(ctx.isCobranded());

        request.addParameter("cobrand", "yahoo");
        _sessionContextUtil.updateFromParams(request, _mockHttpServletResponse, ctx);
        assertTrue(ctx.isYahooCobrand());

    }

    public void testHostCobrandUrlOnLiveSite() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        SessionContext ctx = new SessionContext();
        request.setServerName("sfgate.greatschools.net");
        _sessionContextUtil.updateFromParams(request, _mockHttpServletResponse, ctx);
        assertEquals("sfgate.greatschools.net", ctx.getHostName());
        assertTrue(!ctx.isAdFree());
        assertTrue(ctx.isCobranded());
        assertEquals(ctx.getCobrand(), "sfgate");
        assertNull(ctx.getState());
        assertTrue(ctx.getStateOrDefault().equals(State.CA));

        // Add the state parameter
        request.addParameter("state", "wy");
        _sessionContextUtil.updateFromParams(request, _mockHttpServletResponse, ctx);
        assertTrue(ctx.getState().equals(State.WY));

        // Now try a non-standard URL cobrand such as babycenter
        request = new MockHttpServletRequest();
        ctx = new SessionContext();
        request.setServerName("greatschools.babycenter.com");
        _sessionContextUtil.updateFromParams(request, _mockHttpServletResponse, ctx);
        assertEquals("greatschools.babycenter.com", ctx.getHostName());
        assertTrue(!ctx.isAdFree());
        assertTrue(ctx.isCobranded());
    }

    public void testHostMainUrlOnLiveSiteWithCobrandParameter() {
        // Try developer workstation scenario
        MockHttpServletRequest request = new MockHttpServletRequest();
        SessionContext ctx = new SessionContext();
        request.setServerName("www.greatschools.net");
        request.addParameter("cobrand", "framed");
        _sessionContextUtil.updateFromParams(request, _mockHttpServletResponse, ctx);
        assertEquals("framed.greatschools.net", ctx.getHostName());
        assertTrue(ctx.isAdFree());
        assertTrue(ctx.isCobranded());
        assertEquals(ctx.getCobrand(), "framed");
    }

    public void testHostCobrandUrlOnDevSite() {
        // Try developer workstation scenario
        SessionContext ctx = new SessionContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("azcentral.dev.greatschools.net");
        _sessionContextUtil.updateFromParams(request, _mockHttpServletResponse, ctx);
        assertEquals("azcentral.dev.greatschools.net", ctx.getHostName());
        assertTrue(!ctx.isAdFree());
        assertTrue(ctx.isCobranded());
        assertEquals(ctx.getCobrand(), "azcentral");
    }

    public void testIsYahooCobrand() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        SessionContext ctx = new SessionContext();
        request.setServerName("yahoo.greatschools.net");
        _sessionContextUtil.updateFromParams(request, _mockHttpServletResponse, ctx);
        assertEquals(ctx.getHostName(), "yahoo.greatschools.net");
        assertTrue(!ctx.isAdFree());
        assertTrue(ctx.isCobranded());
        assertTrue(ctx.isYahooCobrand());

        request.setServerName("yahooed.greatschools.net");
        _sessionContextUtil.updateFromParams(request, _mockHttpServletResponse, ctx);
        assertEquals(ctx.getHostName(), "yahooed.greatschools.net");
        assertTrue(!ctx.isAdFree());
        assertTrue(ctx.isCobranded());
        assertTrue(ctx.isYahooCobrand());
    }

    public void testHostWithoutPeriod() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        SessionContext ctx = new SessionContext();
        request.setServerName("maddy");
        _sessionContextUtil.updateFromParams(request, _mockHttpServletResponse, ctx);
        assertEquals(ctx.getHostName(), "maddy");
        assertTrue(!ctx.isAdFree());
        assertTrue(!ctx.isCobranded());
        assertTrue(!ctx.isYahooCobrand());
    }

    public void testStateSetting() {
        SessionContextUtil util = new SessionContextUtil();
        util.setStateManager(new StateManager());

        SessionContext ctx = new SessionContext();

        MockHttpServletRequest request = new MockHttpServletRequest();

        assertEquals(null, ctx.getState());

        request.addParameter("state", "");
        util.updateStateFromParam(ctx, request);
        assertEquals(null, ctx.getState());

        request.addParameter("state", "GA");
        util.updateStateFromParam(ctx, request);
        assertEquals(State.GA, ctx.getState());

        request.addParameter("state", "fl");
        util.updateStateFromParam(ctx, request);
        assertEquals(State.FL, ctx.getState());

        request.addParameter("state", "ct/");
        util.updateStateFromParam(ctx, request);
        assertEquals(State.CT, ctx.getState());

        request.addParameter("state", "x");
        util.updateStateFromParam(ctx, request);
        assertEquals(State.CT, ctx.getState());

        request.addParameter("state", "xx");
        util.updateStateFromParam(ctx, request);
        assertEquals(State.CT, ctx.getState());
    }

}
