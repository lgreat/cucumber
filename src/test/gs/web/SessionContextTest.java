package gs.web;

import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import gs.data.state.State;
import gs.data.state.StateManager;

/**
 * Tests for the SessionContext object
 *
 * @author Todd Huss <mailto:thuss@greatschools.net>
 */
public class SessionContextTest extends TestCase {

    public void testHostDeveloperWorkstation() {
        // Try developer workstation scenario
        MockHttpServletRequest request = new MockHttpServletRequest();
        SessionContext ctx = new SessionContext();
        request.setServerName("localhost");
        ctx.updateFromParams(request);
        assertEquals(ctx.getHostName(), "dev.greatschools.net");
        assertTrue(!ctx.isAdFree());
        assertTrue(!ctx.isCobrand());
        assertTrue(!ctx.isYahooCobrand());

        // Add the cobrand parameter
        request.addParameter("cobrand", "number1expert");
        ctx.updateFromParams(request);
        assertEquals("number1expert.dev.greatschools.net", ctx.getHostName());
        assertTrue(ctx.isAdFree());
        assertTrue(ctx.isCobrand());

        request.addParameter("cobrand", "yahoo");
        ctx.updateFromParams(request);
        assertTrue(ctx.isYahooCobrand());

    }

    public void testHostCobrandUrlOnLiveSite() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        SessionContext ctx = new SessionContext();
        ctx.setStateManager(new StateManager());
        request.setServerName("sfgate.greatschools.net");
        ctx.updateFromParams(request);
        assertEquals("sfgate.greatschools.net", ctx.getHostName());
        assertTrue(!ctx.isAdFree());
        assertTrue(ctx.isCobrand());
        assertEquals(ctx.getCobrand(), "sfgate");
        assertNull(ctx.getState());
        assertTrue(ctx.getStateOrDefault().equals(State.CA));

        // Add the state parameter
        request.addParameter("state", "wy");
        ctx.updateFromParams(request);
        assertTrue(ctx.getState().equals(State.WY));

        // Now try a non-standard URL cobrand such as babycenter
        request = new MockHttpServletRequest();
        ctx = new SessionContext();
        ctx.setStateManager(new StateManager());
        request.setServerName("greatschools.babycenter.com");
        ctx.updateFromParams(request);
        assertEquals("greatschools.babycenter.com", ctx.getHostName());
        assertTrue(!ctx.isAdFree());
        assertTrue(ctx.isCobrand());
    }

    public void testHostMainUrlOnLiveSiteWithCobrandParameter() {
        // Try developer workstation scenario
        MockHttpServletRequest request = new MockHttpServletRequest();
        SessionContext ctx = new SessionContext();
        ctx.setStateManager(new StateManager());
        request.setServerName("www.greatschools.net");
        request.addParameter("cobrand", "framed");
        ctx.updateFromParams(request);
        assertEquals("framed.greatschools.net", ctx.getHostName());
        assertTrue(ctx.isAdFree());
        assertTrue(ctx.isCobrand());
        assertEquals(ctx.getCobrand(), "framed");
    }

    public void testHostCobrandUrlOnDevSite() {
        // Try developer workstation scenario
        MockHttpServletRequest request = new MockHttpServletRequest();
        SessionContext ctx = new SessionContext();
        ctx.setStateManager(new StateManager());
        request.setServerName("azcentral.dev.greatschools.net");
        ctx.updateFromParams(request);
        assertEquals("azcentral.dev.greatschools.net", ctx.getHostName());
        assertTrue(!ctx.isAdFree());
        assertTrue(ctx.isCobrand());
        assertEquals(ctx.getCobrand(), "azcentral");
    }

    public void testIsYahooCobrand() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        SessionContext ctx = new SessionContext();
        request.setServerName("yahoo.greatschools.net");
        ctx.updateFromParams(request);
        assertEquals(ctx.getHostName(), "yahoo.greatschools.net");
        assertTrue(!ctx.isAdFree());
        assertTrue(ctx.isCobrand());
        assertTrue(ctx.isYahooCobrand());

        request.setServerName("yahooed.greatschools.net");
        ctx.updateFromParams(request);
        assertEquals(ctx.getHostName(), "yahooed.greatschools.net");
        assertTrue(!ctx.isAdFree());
        assertTrue(ctx.isCobrand());
        assertTrue(ctx.isYahooCobrand());
    }

    public void testHostWithoutPeriod() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        SessionContext ctx = new SessionContext();
        request.setServerName("maddy");
        ctx.updateFromParams(request);
        assertEquals(ctx.getHostName(), "maddy");
        assertTrue(!ctx.isAdFree());
        assertTrue(!ctx.isCobrand());
        assertTrue(!ctx.isYahooCobrand());
    }

}
