package gs.web.util.context;

import gs.data.admin.IPropertyDao;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.util.DigestUtil;
import gs.web.BaseTestCase;
import gs.web.GsMockHttpServletRequest;
import static org.easymock.EasyMock.*;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.Cookie;
import java.security.NoSuchAlgorithmException;

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
    private IPropertyDao _propertyDao;

    /**
     * @noinspection ProhibitedExceptionDeclared
     */
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

        _propertyDao = createMock(IPropertyDao.class);
        _sessionContext.setPropertyDao(_propertyDao);
    }

    public void testIsUserValid() throws NoSuchAlgorithmException {
        User user = new User();
        user.setId(123);
        user.setEmail("anEmail@greatschools.net");
        _sessionContext.setUser(user);
        assertEquals("anEmail@greatschools.net", _sessionContext.getEmail());
        assertEquals("anEmail%40greatschools.net", _sessionContext.getEmailUrlEncoded());

        Object[] inputs = {User.SECRET_NUMBER, user.getId(), user.getEmail()};
        _sessionContext.setUserHash(DigestUtil.hashObjectArray(inputs));
        assertTrue(_sessionContext.isUserValid());
    }

    public void testIsCrawler() {
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)");
        _sessionContextUtil.updateFromParams(request, _response, _sessionContext);
        assertFalse(_sessionContext.isCrawler());

        request = new GsMockHttpServletRequest();        
        request.addHeader("User-Agent", "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
        _sessionContextUtil.updateFromParams(request, _response, _sessionContext);
        assertTrue(_sessionContext.isCrawler());

        request = new GsMockHttpServletRequest();
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1) Gecko/20060909 Firefox/1.5.0.7");
        _sessionContextUtil.updateFromParams(request, _response, _sessionContext);
        assertFalse(_sessionContext.isCrawler());
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
        assertNotNull(_sessionContext);
        expect(_propertyDao.getProperty(IPropertyDao.ADVERTISING_ENABLED_KEY, "true")).andReturn("true");
        replay(_propertyDao);
        assertTrue(_sessionContext.isAdvertisingOnline());
        verify(_propertyDao);
    }

    public void testHostCobrandUrlOnLiveSite() {
        _request.setServerName("sfgate.greatschools.net");
        _sessionContextUtil.updateFromParams(_request, _response, _sessionContext);
        assertEquals("sfgate.greatschools.net", _sessionContext.getHostName());
        assertTrue(_sessionContext.isCobranded());
        assertEquals("sfgate", _sessionContext.getCobrand());
        assertNull(_sessionContext.getState());
        assertEquals(State.CA, _sessionContext.getStateOrDefault());

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

    public void testPassWWWParamToUnsetCobrand() {
        // Try developer workstation scenario
        _request.setServerName("desktop-60.greatschools.net");
        _request.setParameter("cobrand", "www");
        _sessionContextUtil.updateFromParams(_request, _response, _sessionContext);
        assertFalse(_sessionContext.isCobranded());
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

    /**
     * @noinspection OverlyLongMethod
     */
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
     * MockHttpServletResponse stores cookie values in a list and not a map so it's possible to have cookies in the list
     * with the same name.
     * <p/>
     * This method performs operations on the LAST cookie inserted into the response.
     */
    private State getStateFromMockResponse(MockHttpServletResponse response) {
        StateManager sm = new StateManager();
        Cookie[] cookies = response.getCookies();
        String state = null;

        for (Cookie c : cookies) {
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
        SessionContext ctx = new SessionContext();
        // We just test that it defaults to A here. The real
        // logic for setting this is in ResponseInterceptor
        assertEquals("a", ctx.getABVersion());
    }


    /**
     * Regression test of GS-2259.
     */
    public void testBadMemberId() {

        final Integer id = -999;

        IUserDao userDao = createMock(IUserDao.class);
        expect(userDao.findUserFromId(id)).andThrow(new ObjectRetrievalFailureException("Can't find it", id));
        replay(userDao);

        _sessionContext.setUserDao(userDao);
        _sessionContext.setMemberId(id);
        assertNull(_sessionContext.getUser());
        verify(userDao);
    }

    public void testIsInterstitialEnabled() {
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_ENABLED_KEY, "false")).andReturn("false");
        replay(_propertyDao);
        assertFalse("Property is false, expect call to return false", _sessionContext.isInterstitialEnabled());
        verify(_propertyDao);

        reset(_propertyDao);
        // Expect this call only three times, despite there being 6 calls to isInterstitialEnabled.
        // This is because the if statement gets short-circuited before the DB call in 3 of the cases
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_ENABLED_KEY, "false")).andReturn("true").times(3);
        replay(_propertyDao);

        assertTrue("Property is true, expect call to return true (1)", _sessionContext.isInterstitialEnabled());
        _sessionContext.setCobrand("cobrand");
        assertFalse("Cobrand exists, should override rval to false", _sessionContext.isInterstitialEnabled());
        _sessionContext.setCobrand(null);
        assertTrue("Property is true, expect call to return true (2)", _sessionContext.isInterstitialEnabled());
        _sessionContext.setCrawler(true);
        assertFalse("Crawler exists, should override rval to false", _sessionContext.isInterstitialEnabled());
        _sessionContext.setCobrand("cobrand");
        assertFalse("Both crawler and cobrand exist, should override rval to false",
                _sessionContext.isInterstitialEnabled());
        _sessionContext.setCobrand(null);
        _sessionContext.setCrawler(false);
        assertTrue("Property is true, expect call to return true (3)", _sessionContext.isInterstitialEnabled());
        _sessionContext.setCobrand("cobrand");
        verify(_propertyDao);
    }
}
