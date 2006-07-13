package gs.web.util.context;

import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.admin.IPropertyDao;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.BaseTestCase;
import gs.web.GsMockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.Cookie;
import java.util.Date;

/**
 * Tests for the SessionContext object
 *
 * @author Todd Huss <mailto:thuss@greatschools.net>
 * @noinspection FeatureEnvy,HardcodedFileSeparator,MagicNumber
 */
public class SessionContextTest extends BaseTestCase {


    private SessionContextUtil _sessionContextUtil;
    private MockHttpServletResponse _response;
    private GsMockHttpServletRequest _request;
    private SessionContext _sessionContext;
    private static final String STATE_COOKIE = "state";

    /** @noinspection ProhibitedExceptionDeclared*/
    protected void setUp() throws Exception {
        super.setUp();

        _request = new GsMockHttpServletRequest();


        _response = new MockHttpServletResponse();

        _sessionContextUtil = new SessionContextUtil();
        _sessionContextUtil.setStateManager(new StateManager());
        final CookieGenerator stateCookieGenerator = new CookieGenerator();
        stateCookieGenerator.setCookieName(STATE_COOKIE);
        stateCookieGenerator.setCookieMaxAge(-1);
        _sessionContextUtil.setStateCookieGenerator(stateCookieGenerator);

        _sessionContext = new SessionContext();
    }

    public void testHostDeveloperWorkstation() {
        // Try developer workstation scenario
        _request.setServerName("localhost");


        _sessionContextUtil.updateFromParams(_request, _response, _sessionContext);
        assertEquals("dev.greatschools.net", _sessionContext.getHostName());
        assertFalse(_sessionContext.isCobranded());
        assertFalse(_sessionContext.isYahooCobrand());

        // Add the cobrand parameter
        _request.setParameter("cobrand", "number1expert");
        _sessionContextUtil.updateFromParams(_request, _response, _sessionContext);
        assertEquals("number1expert.dev.greatschools.net", _sessionContext.getHostName());
        assertTrue(_sessionContext.isCobranded());

        _request.setParameter("cobrand", "yahoo");
        _sessionContextUtil.updateFromParams(_request, _response, _sessionContext);
        assertTrue(_sessionContext.isYahooCobrand());

        _request.setParameter("cobrand", "family");
        _sessionContextUtil.updateFromParams(_request, _response, _sessionContext);
        assertTrue(_sessionContext.isFamilyCobrand());

    }

    /**
     * Make sure we can get a value back for advertising being online
     */
    public void testAdvertising() {
        _sessionContext.setPropertyDao(new IPropertyDao() {
            public String getProperty(String key) {
                return "true";
            }

            public Date getPropertyAsDate(String key) {
                return null;
            }

            public String getProperty(String key, String defaultValue) {
                return "true";
            }

            public void setProperty(String key, String value) throws IllegalArgumentException {
            }

            public void setPropertyAsDate(String key, Date date) {
            }

            public void removeProperty(String key) {
            }
        });
        assertNotNull(_sessionContext);
        assertTrue(_sessionContext.isAdvertisingOnline());
    }

    public void testHostCobrandUrlOnLiveSite() {
        _request.setServerName("sfgate.greatschools.net");
        _sessionContextUtil.updateFromParams(_request, _response, _sessionContext);
        assertEquals("sfgate.greatschools.net", _sessionContext.getHostName());
        assertTrue(_sessionContext.isCobranded());
        assertEquals("sfgate", _sessionContext.getCobrand());
        assertNull(_sessionContext.getState());
        assertEquals(State.CA,  _sessionContext.getStateOrDefault());

        // Add the state parameter
        _request.setParameter("state", "wy");
        _sessionContextUtil.updateFromParams(_request, _response, _sessionContext);
        assertEquals(State.WY, _sessionContext.getState());

        // Now try a non-standard URL cobrand such as babycenter
        _request = new GsMockHttpServletRequest();
        _sessionContext = new SessionContext();
        _request.setServerName("greatschools.babycenter.com");
        _sessionContextUtil.updateFromParams(_request, _response, _sessionContext);
        assertEquals("greatschools.babycenter.com", _sessionContext.getHostName());
        assertTrue(_sessionContext.isCobranded());
    }

    public void testHostMainUrlOnLiveSiteWithCobrandParameter() {
        // Try developer workstation scenario
        _request.setServerName("www.greatschools.net");
        _request.setParameter("cobrand", "framed");
        _sessionContextUtil.updateFromParams(_request, _response, _sessionContext);
        assertEquals("framed.greatschools.net", _sessionContext.getHostName());
        assertTrue(_sessionContext.isCobranded());
        assertEquals("framed", _sessionContext.getCobrand());
    }

    public void testHostCobrandUrlOnDevSite() {
        // Try developer workstation scenario
        _request.setServerName("azcentral.dev.greatschools.net");
        _sessionContextUtil.updateFromParams(_request, _response, _sessionContext);
        assertEquals("azcentral.dev.greatschools.net", _sessionContext.getHostName());
        assertTrue(_sessionContext.isCobranded());
        assertEquals("azcentral", _sessionContext.getCobrand());
    }

    public void testIsYahooCobrand() {
        _request.setServerName("yahoo.greatschools.net");
        _sessionContextUtil.updateFromParams(_request, _response, _sessionContext);
        assertEquals("yahoo.greatschools.net", _sessionContext.getHostName());
        assertTrue(_sessionContext.isCobranded());
        assertTrue(_sessionContext.isYahooCobrand());

        _request.setServerName("yahooed.greatschools.net");
        _sessionContextUtil.updateFromParams(_request, _response, _sessionContext);
        assertEquals("yahooed.greatschools.net", _sessionContext.getHostName());
        assertTrue(_sessionContext.isCobranded());
        assertTrue(_sessionContext.isYahooCobrand());
    }

    public void testIsFamilyCobrand() {
        _request.setServerName("family.greatschools.net");
        _sessionContextUtil.updateFromParams(_request, _response, _sessionContext);
        assertEquals("family.greatschools.net", _sessionContext.getHostName());
        assertTrue(_sessionContext.isCobranded());
        assertTrue(_sessionContext.isFamilyCobrand());
    }

    public void testHostWithoutPeriod() {
        _request.setServerName("maddy");
        _sessionContextUtil.updateFromParams(_request, _response, _sessionContext);
        assertEquals("maddy", _sessionContext.getHostName());
        assertTrue(!_sessionContext.isCobranded());
        assertTrue(!_sessionContext.isYahooCobrand());
    }

    public void testDomainWideCookie() {
        SessionContextUtil util = new SessionContextUtil();
        util.setStateManager(new StateManager());

        CookieGenerator cookieGenSomeCookie = new CookieGenerator();
        cookieGenSomeCookie.setCookiePath(CookieGenerator.DEFAULT_COOKIE_PATH);
        cookieGenSomeCookie.setCookieMaxAge(100);
        cookieGenSomeCookie.setCookieName("someCookie");
        cookieGenSomeCookie.addCookie(_response, "someValue");
        Cookie cookie = _response.getCookie("someCookie");
        assertNotNull(cookie);
        assertEquals("someValue", cookie.getValue());
        assertEquals("/", cookie.getPath());
        assertEquals(100, cookie.getMaxAge());

        _response = new MockHttpServletResponse();

        CookieGenerator cookieGenerator = new CookieGenerator();
        cookieGenerator.setCookiePath(CookieGenerator.DEFAULT_COOKIE_PATH);
        cookieGenerator.setCookieMaxAge(0);
        cookieGenerator.setCookieName("someCookie");
        cookieGenerator.addCookie(_response, "someValue");
        cookie = _response.getCookie("someCookie");
        assertNotNull(cookie);
        assertEquals("someValue", cookie.getValue());
        assertEquals("/", cookie.getPath());
        assertEquals(0, cookie.getMaxAge());
    }

    /** @noinspection OverlyLongMethod*/
    public void testStateSetting() {

        assertNull(_sessionContext.getState());

        //do not set a state variable
        _request.setParameter("bogus", "");
        _sessionContextUtil.updateStateFromParam(_sessionContext, _request, _response);
        assertNull(_sessionContext.getState());
        assertNull(getStateFromMockResponse(_response));

        //bogus state
        _request.setParameter("state", "bo");
        _sessionContextUtil.updateStateFromParam(_sessionContext, _request, _response);
        assertNull(_sessionContext.getState());
        assertNull(getStateFromMockResponse(_response));

        _request.setParameter("state", "");
        _sessionContextUtil.updateStateFromParam(_sessionContext, _request, _response);
        assertNull(_sessionContext.getState());
        assertNull(getStateFromMockResponse(_response));

        _request.setParameter("state", "GA");
        _sessionContextUtil.updateStateFromParam(_sessionContext, _request, _response);
        assertEquals(State.GA, _sessionContext.getState());
        assertEquals(State.GA, getStateFromMockResponse(_response));

        _request.setParameter("state", "fl");
        _sessionContextUtil.updateStateFromParam(_sessionContext, _request, _response);
        assertEquals(State.FL, _sessionContext.getState());
        assertEquals(State.FL, getStateFromMockResponse(_response));

        _request.setParameter("state", "ct/");
        _sessionContextUtil.updateStateFromParam(_sessionContext, _request, _response);
        assertEquals(State.CT, _sessionContext.getState());
        assertEquals(State.CT, getStateFromMockResponse(_response));

        _request.setParameter("state", "x");
        _sessionContextUtil.updateStateFromParam(_sessionContext, _request, _response);
        assertEquals(State.CT, _sessionContext.getState());
        assertEquals(State.CT, getStateFromMockResponse(_response));

        _request.setParameter("state", "xx");
        _sessionContextUtil.updateStateFromParam(_sessionContext, _request, _response);
        assertEquals(State.CT, _sessionContext.getState());
        assertEquals(State.CT, getStateFromMockResponse(_response));

        _request.setParameter("state", "US");
        _sessionContextUtil.updateStateFromParam(_sessionContext, _request, _response);
        assertNull(_sessionContext.getState());
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
            if (STATE_COOKIE.equals(c.getName())) {
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
