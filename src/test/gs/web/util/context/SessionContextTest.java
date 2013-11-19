package gs.web.util.context;

import gs.data.admin.IPropertyDao;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.geo.IGeoDao;
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
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Tests for the SessionContext object
 *
 * @author Todd Huss <mailto:thuss@greatschools.org>
 * @noinspection FeatureEnvy,HardcodedFileSeparator,MagicNumber
 */
public class SessionContextTest extends BaseTestCase {


    private SessionContextUtil _sessionContextUtil;
    private MockHttpServletResponse _response;
    private GsMockHttpServletRequest _request;
    private SessionContext _sessionContext;
    private static final String STATE_COOKIE = "state";
    private IPropertyDao _propertyDao;
    private IGeoDao _geoDao;

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
        _geoDao = createStrictMock(IGeoDao.class);
        _sessionContext.setGeoDao(_geoDao);
    }

    public void testIsUserSeemsValid() throws NoSuchAlgorithmException {
        User user = new User();
        user.setId(123);
        user.setEmail("anEmail@greatschools.org");
        _sessionContext.setMemberId(123);
        _sessionContext.setUser(user);
        assertEquals("anEmail@greatschools.org", _sessionContext.getEmail());
        assertEquals("anEmail%40greatschools.org", _sessionContext.getEmailUrlEncoded());

        Object[] inputs = {User.SECRET_NUMBER, "foobar", user.getId()};
        _sessionContext.setUserHash(DigestUtil.hashObjectArray(inputs) + "123");
        assertTrue(_sessionContext.isUserSeemsValid());
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
        assertEquals("dev.greatschools.org", _sessionContext.getHostName());
        assertFalse(_sessionContext.isCobranded());
        assertFalse(_sessionContext.isYahooCobrand());

        // Add the cobrand parameter
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
        _request.setServerName("sfgate.greatschools.org");
        _sessionContextUtil.updateFromParams(_request, _response, _sessionContext);
        assertEquals("sfgate.greatschools.org", _sessionContext.getHostName());
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
        _request.setServerName("www.greatschools.org");
        _request.setParameter("cobrand", "framed");
        _sessionContextUtil.updateFromParams(_request, _response, _sessionContext);
        assertEquals("framed.greatschools.org", _sessionContext.getHostName());
        assertTrue(_sessionContext.isCobranded());
        assertEquals("framed", _sessionContext.getCobrand());
    }

    public void testPassWWWParamToUnsetCobrand() {
        // Try developer workstation scenario
        _request.setServerName("desktop-60.greatschools.org");
        _request.setParameter("cobrand", "www");
        _sessionContextUtil.updateFromParams(_request, _response, _sessionContext);
        assertFalse(_sessionContext.isCobranded());
    }

    public void testHostCobrandUrlOnDevSite() {
        // Try developer workstation scenario
        _request.setServerName("azcentral.dev.greatschools.org");
        _sessionContextUtil.updateFromParams(_request, _response, _sessionContext);
        assertEquals("azcentral.dev.greatschools.org", _sessionContext.getHostName());
        assertTrue(_sessionContext.isCobranded());
        assertEquals("azcentral", _sessionContext.getCobrand());
    }

    public void testIsYahooCobrand() {
        _request.setServerName("yahoo.greatschools.org");
        _sessionContextUtil.updateFromParams(_request, _response, _sessionContext);
        assertEquals("yahoo.greatschools.org", _sessionContext.getHostName());
        assertTrue(_sessionContext.isCobranded());
        assertTrue(_sessionContext.isYahooCobrand());

        _request.setServerName("yahooed.greatschools.org");
        _sessionContextUtil.updateFromParams(_request, _response, _sessionContext);
        assertEquals("yahooed.greatschools.org", _sessionContext.getHostName());
        assertTrue(_sessionContext.isCobranded());
        assertTrue(_sessionContext.isYahooCobrand());
    }

    public void testIsFamilyCobrand() {
        _request.setServerName("family.greatschools.org");
        _sessionContextUtil.updateFromParams(_request, _response, _sessionContext);
        assertEquals("family.greatschools.org", _sessionContext.getHostName());
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

    public void testEditorialDevShouldNotBeCobranded() {
        _request.setServerName("editorial.dev.greatschools.org");
        _sessionContextUtil.updateFromParams(_request, _response, _sessionContext);
        assertTrue("editorial.dev should not be a cobrand", !_sessionContext.isCobranded());
    }

    public void testCustomCobrandFooter() {
        // Test exception handling
        SessionContext ctx = new SessionContext();
        ctx.setHostName("@$#@$%#"); // Impossible hostname
        ctx.setCobrand("test");
        assertFalse(ctx.isCustomCobrandedFooter());
        assertEquals("", ctx.getCustomCobrandFooter());

        // Test footer handling but with a footer this time
        ctx = new SessionContext() {
            protected String fetchCustomCobrandFooter() throws IOException {
                return "html footer";
            }
        };
        ctx.setCobrand("test");
        assertTrue(ctx.isCustomCobrandedFooter());
        assertEquals("html footer", ctx.getCustomCobrandFooter());
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

    public void testBadCityId() {
        final Integer id = -999;

        expect(_geoDao.findCityById(-999)).andThrow(new ObjectRetrievalFailureException("Can't find it", id));
        replay(_geoDao);
        _sessionContext.setCityId(id);
        assertNull(_sessionContext.getCity());
        verify(_geoDao);
    }


    public String getAllStatesString(){
        StateManager sm = new StateManager();

        StringBuilder sb = new StringBuilder();
        for (State state : sm.getListByAbbreviations()){
            if (sb.length() > 0){
                sb.append(",");
            }
            sb.append(state.getAbbreviation());
        }
        return sb.toString();
    }

    public void testIsInterstitialEnabled() {

        /*
            isCobranded  - true when the site is cobranded
            isCrawler    - true when the user of the site is a crawler
            property.INTERSTITIAL_ENABLED  - true/false set in the property table
            property.INTERSTITIAL_ENABLED_STATES - a list csv containing the state
                    abbreviations that should get the interstitial set in the property table
            state        - the current state in context
                     
            testIsInterstitialEnabled() will return false when:
                isCobranded is true or
                isCrawler is true or
                isAdvertisingOnline is false or
                state is null or
                property.INTERSTITIAL_ENABLED isn't set or
                property.INTERSTITIAL_ENABLED isn't set to 'true' or
                property.INTERSTITIAL_ENABLED_STATES isn't set or
                property.INTERSTITIAL_ENABLED_STATES doesn't contain the abberviation for the current state in context, or
                property.INTERSTITIAL_DISPLAY_RATE isn't set to numeric value greater than 0, or
                property.INTERSTITIAL_DISPLAY_RATE > 0 and < 100 AND a random number is generated that is greater than the value stored
         */

        String allStatesString = getAllStatesString();
        _sessionContext.setState(State.NJ);
        expect(_propertyDao.getProperty(IPropertyDao.ADVERTISING_ENABLED_KEY, "true")).andReturn("true");
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_ENABLED_KEY, "false")).andReturn("false");
        replay(_propertyDao);
        assertFalse("Property is false, expect call to return false", _sessionContext.isInterstitialEnabled());
        verify(_propertyDao);

        reset(_propertyDao);
        expect(_propertyDao.getProperty(IPropertyDao.ADVERTISING_ENABLED_KEY, "true")).andReturn("false");
        // don't expect to check the interstitial enabled key at all because the if statement short-circuits before we get to it
        replay(_propertyDao);
        assertFalse("Advertising is disabled and interstitial is enabled, expect call to return false", _sessionContext.isInterstitialEnabled());
        verify(_propertyDao);

        reset(_propertyDao);
        expect(_propertyDao.getProperty(IPropertyDao.ADVERTISING_ENABLED_KEY, "true")).andReturn("true");
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_ENABLED_KEY, "false")).andReturn("true");
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_DISPLAY_RATE_KEY, "100")).andReturn("100");
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_ENABLED_STATES_KEY, "")).andReturn(allStatesString);
        replay(_propertyDao);
        assertTrue("Advertising is enabled and interstitial is enabled, expect call to return true", _sessionContext.isInterstitialEnabled());
        verify(_propertyDao);

        reset(_propertyDao);
        // Expect this call only three times, despite there being 6 calls to isInterstitialEnabled.
        // This is because the if statement gets short-circuited before the DB call in 3 of the cases
        expect(_propertyDao.getProperty(IPropertyDao.ADVERTISING_ENABLED_KEY, "true")).andReturn("true");
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_ENABLED_KEY, "false")).andReturn("true");
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_ENABLED_STATES_KEY, "")).andReturn(allStatesString);
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_DISPLAY_RATE_KEY, "100")).andReturn("100");
        expect(_propertyDao.getProperty(IPropertyDao.ADVERTISING_ENABLED_KEY, "true")).andReturn("true");
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_ENABLED_KEY, "false")).andReturn("true");
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_ENABLED_STATES_KEY, "")).andReturn(allStatesString);
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_DISPLAY_RATE_KEY, "100")).andReturn("100");
        expect(_propertyDao.getProperty(IPropertyDao.ADVERTISING_ENABLED_KEY, "true")).andReturn("true");
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_ENABLED_KEY, "false")).andReturn("true");
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_ENABLED_STATES_KEY, "")).andReturn(allStatesString);
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_DISPLAY_RATE_KEY, "100")).andReturn("100");
        replay(_propertyDao);

        assertTrue("Property.INTERSTITIAL_ENABLED_KEY is true, Property.INTERSTITIAL_ENABLED_STATES_KEY contains the state, expect call to return true (1)", _sessionContext.isInterstitialEnabled());
        _sessionContext.setCobrand("cobrand");
        assertFalse("Cobrand exists, should override rval to false", _sessionContext.isInterstitialEnabled());
        _sessionContext.setCobrand(null);
        assertTrue("Property.INTERSTITIAL_ENABLED_KEY is true, Property.INTERSTITIAL_ENABLED_STATES_KEY contains the state, expect call to return true (2)", _sessionContext.isInterstitialEnabled());
        _sessionContext.setCrawler(true);
        assertFalse("Crawler exists, should override rval to false", _sessionContext.isInterstitialEnabled());
        _sessionContext.setCobrand("cobrand");
        assertFalse("Both crawler and cobrand exist, should override rval to false",
                _sessionContext.isInterstitialEnabled());
        _sessionContext.setCobrand(null);
        _sessionContext.setCrawler(false);
        assertTrue("Property.INTERSTITIAL_ENABLED_KEY is true, Property.INTERSTITIAL_ENABLED_STATES_KEY contains the state, expect call to return true (3)", _sessionContext.isInterstitialEnabled());
        _sessionContext.setCobrand("cobrand");
        verify(_propertyDao);

        // test null state

        reset(_propertyDao);
        // Expect this call only three times, despite there being 6 calls to isInterstitialEnabled.
        // This is because the if statement gets short-circuited before the DB call in 3 of the cases
        expect(_propertyDao.getProperty(IPropertyDao.ADVERTISING_ENABLED_KEY, "true")).andReturn("true");
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_ENABLED_KEY, "false")).andReturn("true");
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_ENABLED_STATES_KEY, "")).andReturn(allStatesString);

        replay(_propertyDao);

        _sessionContext.setState(null);
        assertFalse("Property.INTERSTITIAL_ENABLED_KEY is true, SessionContext.State is null expect call to return false", _sessionContext.isInterstitialEnabled());

        // test state not enabled

         reset(_propertyDao);
        // Expect this call only three times, despite there being 6 calls to isInterstitialEnabled.
        // This is because the if statement gets short-circuited before the DB call in 3 of the cases
        expect(_propertyDao.getProperty(IPropertyDao.ADVERTISING_ENABLED_KEY, "true")).andReturn("true");
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_ENABLED_KEY, "false")).andReturn("true");
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_ENABLED_STATES_KEY, "")).andReturn("");

        replay(_propertyDao);

        _sessionContext.setState(State.NV);
        assertFalse("Property.INTERSTITIAL_ENABLED_KEY is true, Property.INTERSTITIAL_ENABLED_STATES_KEY doesn't contain the state, NV, expect call to return false", _sessionContext.isInterstitialEnabled());
    }

    public void testIsInterstitialEnabledIgnoreState() {

        /*
            isCobranded  - true when the site is cobranded
            isCrawler    - true when the user of the site is a crawler
            property.INTERSTITIAL_ENABLED  - true/false set in the property table

            testIsInterstitialEnabled() will return false when:
                isCobranded is true or
                isCrawler is true or
                isAdvertisingOnline is false or
                property.INTERSTITIAL_ENABLED isn't set or
                property.INTERSTITIAL_ENABLED isn't set to 'true' or
                property.INTERSTITIAL_DISPLAY_RATE isn't set to numeric value greater than 0, or
                property.INTERSTITIAL_DISPLAY_RATE > 0 and < 100 AND a random number is generated that is greater than the value stored
         */

        expect(_propertyDao.getProperty(IPropertyDao.ADVERTISING_ENABLED_KEY, "true")).andReturn("true");
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_ENABLED_KEY, "false")).andReturn("false");
        replay(_propertyDao);
        assertFalse("Property is false, expect call to return false", _sessionContext.isInterstitialEnabledIgnoreState());
        verify(_propertyDao);

        reset(_propertyDao);
        expect(_propertyDao.getProperty(IPropertyDao.ADVERTISING_ENABLED_KEY, "true")).andReturn("false");
        // don't expect to check the interstitial enabled key at all because the if statement short-circuits before we get to it
        replay(_propertyDao);
        assertFalse("Advertising is disabled and interstitial is enabled, expect call to return false", _sessionContext.isInterstitialEnabledIgnoreState());
        verify(_propertyDao);

        reset(_propertyDao);
        expect(_propertyDao.getProperty(IPropertyDao.ADVERTISING_ENABLED_KEY, "true")).andReturn("true");
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_ENABLED_KEY, "false")).andReturn("true");
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_DISPLAY_RATE_KEY, "100")).andReturn("100");
        replay(_propertyDao);
        assertTrue("Advertising is enabled and interstitial is enabled, expect call to return true", _sessionContext.isInterstitialEnabledIgnoreState());
        verify(_propertyDao);

        reset(_propertyDao);
        // Expect this call only three times, despite there being 6 calls to isInterstitialEnabled.
        // This is because the if statement gets short-circuited before the DB call in 3 of the cases
        expect(_propertyDao.getProperty(IPropertyDao.ADVERTISING_ENABLED_KEY, "true")).andReturn("true");
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_ENABLED_KEY, "false")).andReturn("true");
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_DISPLAY_RATE_KEY, "100")).andReturn("100");
        expect(_propertyDao.getProperty(IPropertyDao.ADVERTISING_ENABLED_KEY, "true")).andReturn("true");
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_ENABLED_KEY, "false")).andReturn("true");
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_DISPLAY_RATE_KEY, "100")).andReturn("100");
        expect(_propertyDao.getProperty(IPropertyDao.ADVERTISING_ENABLED_KEY, "true")).andReturn("true");
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_ENABLED_KEY, "false")).andReturn("true");
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_DISPLAY_RATE_KEY, "100")).andReturn("100");
        replay(_propertyDao);

        assertTrue("Property.INTERSTITIAL_ENABLED_KEY is true, expect call to return true (1)", _sessionContext.isInterstitialEnabledIgnoreState());
        _sessionContext.setCobrand("cobrand");
        assertFalse("Cobrand exists, should override rval to false", _sessionContext.isInterstitialEnabledIgnoreState());
        _sessionContext.setCobrand(null);
        assertTrue("Property.INTERSTITIAL_ENABLED_KEY is true, expect call to return true (2)", _sessionContext.isInterstitialEnabledIgnoreState());
        _sessionContext.setCrawler(true);
        assertFalse("Crawler exists, should override rval to false", _sessionContext.isInterstitialEnabledIgnoreState());
        _sessionContext.setCobrand("cobrand");
        assertFalse("Both crawler and cobrand exist, should override rval to false",
                _sessionContext.isInterstitialEnabled());
        _sessionContext.setCobrand(null);
        _sessionContext.setCrawler(false);
        assertTrue("Property.INTERSTITIAL_ENABLED_KEY is true, expect call to return true (3)", _sessionContext.isInterstitialEnabledIgnoreState());
        _sessionContext.setCobrand("cobrand");
        verify(_propertyDao);
    }


    public void testIsInterstitialWithinTolerance(){
        reset(_propertyDao);
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_DISPLAY_RATE_KEY, "100")).andReturn("0");
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_DISPLAY_RATE_KEY, "100")).andReturn("0");
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_DISPLAY_RATE_KEY, "100")).andReturn("0");
        replay(_propertyDao);

        assertFalse("Expect isInterstitialWithinTolerance when property.INTERSTITIAL_DISPLAY_RATE_KEY is 0, randomValue is 0", _sessionContext.isInterstitialWithinTolerance(0));
        assertFalse("Expect isInterstitialWithinTolerance when property.INTERSTITIAL_DISPLAY_RATE_KEY is 0, randomValue is 99", _sessionContext.isInterstitialWithinTolerance(99));
        assertFalse("Expect isInterstitialWithinTolerance property.INTERSTITIAL_DISPLAY_RATE_KEY is 0, randomValue is 52", _sessionContext.isInterstitialWithinTolerance(52));
        verify(_propertyDao);

        reset(_propertyDao);
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_DISPLAY_RATE_KEY, "100")).andReturn("52");
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_DISPLAY_RATE_KEY, "100")).andReturn("52");
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_DISPLAY_RATE_KEY, "100")).andReturn("52");
        replay(_propertyDao);

        assertTrue("Expect isInterstitialWithinTolerance when property.INTERSTITIAL_DISPLAY_RATE_KEY is 52, randomValue is 0", _sessionContext.isInterstitialWithinTolerance(0));
        assertFalse("Expect isInterstitialWithinTolerance when property.INTERSTITIAL_DISPLAY_RATE_KEY is 52, randomValue is 53", _sessionContext.isInterstitialWithinTolerance(53));
        assertFalse("Expect isInterstitialWithinTolerance when property.INTERSTITIAL_DISPLAY_RATE_KEY is 52, randomValue is 52", _sessionContext.isInterstitialWithinTolerance(52));
        verify(_propertyDao);

        reset(_propertyDao);
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_DISPLAY_RATE_KEY, "100")).andReturn("100");
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_DISPLAY_RATE_KEY, "100")).andReturn("100");
        expect(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_DISPLAY_RATE_KEY, "100")).andReturn("100");
        replay(_propertyDao);

        assertTrue("Expect isInterstitialWithinTolerance when property.INTERSTITIAL_DISPLAY_RATE_KEY is 100, randomValue is 0", _sessionContext.isInterstitialWithinTolerance(0));
        assertTrue("Expect isInterstitialWithinTolerance when property.INTERSTITIAL_DISPLAY_RATE_KEY is 100, randomValue is 99", _sessionContext.isInterstitialWithinTolerance(99));
        assertTrue("Expect isInterstitialWithinTolerance when property.INTERSTITIAL_DISPLAY_RATE_KEY is 100, randomValue is 52", _sessionContext.isInterstitialWithinTolerance(52));
        verify(_propertyDao);
    }

    public void testIsFramed() {
        _sessionContext.setCobrand("test");
        assertTrue("test.greatschools.org should be a framed cobrand", _sessionContext.isFramed());
    }

    public void testGetSurveyDetailsJson() {
        reset(_propertyDao);
        expect(_propertyDao.getProperty(IPropertyDao.SURVEY_DETAILS_ARTICLE, null)).andReturn(null);
        replay(_propertyDao);
        assertNull(_sessionContext.getSurveyDetailsJson(IPropertyDao.SURVEY_DETAILS_ARTICLE));
        verify(_propertyDao);

        String s = "{\"title\":\"title goes here\",\n" +
                "      \"body\":\"body goes here\",\n" +
                "      \"url\":\"http://www.greatschools.org\",\n" +
                "      \"percent\":33\n" +
                "     }";

        reset(_propertyDao);
        expect(_propertyDao.getProperty(IPropertyDao.SURVEY_DETAILS_ARTICLE, null)).andReturn(s);
        replay(_propertyDao);
        assertNotNull(_sessionContext.getSurveyDetailsJson(IPropertyDao.SURVEY_DETAILS_ARTICLE));
        verify(_propertyDao);
    }

    public void testGetSurveyDetails() {
        String s = "{\"title\":\"title goes here\",\n" +
                "      \"body\":\"body goes here\",\n" +
                "      \"url\":\"http://www.greatschools.org\",\n" +
                "      \"percent\":100\n" +
                "     }";

        reset(_propertyDao);
        expect(_propertyDao.getProperty(IPropertyDao.SURVEY_DETAILS_ARTICLE, null)).andReturn(s);
        replay(_propertyDao);
        Map<String,Object> map = _sessionContext.getSurveyDetails("article");
        assertNotNull(map);
        assertTrue((Boolean)map.get("showSurveyHover"));
        assertEquals("title goes here", map.get("title"));
        assertEquals("body goes here", map.get("body"));
        assertEquals("http://www.greatschools.org", map.get("url"));
        verify(_propertyDao);

        reset(_propertyDao);
        expect(_propertyDao.getProperty(IPropertyDao.SURVEY_DETAILS_OVERVIEW, null)).andReturn(s);
        replay(_propertyDao);
        map = _sessionContext.getSurveyDetails("overview");
        assertNotNull(map);
        assertTrue((Boolean)map.get("showSurveyHover"));
        assertEquals("title goes here", map.get("title"));
        assertEquals("body goes here", map.get("body"));
        assertEquals("http://www.greatschools.org", map.get("url"));
        verify(_propertyDao);

        s = "{\"title\":\"title goes here\",\n" +
                "      \"body\":\"body goes here\",\n" +
                "      \"url\":\"http://www.greatschools.org\",\n" +
                "      \"percent\":0\n" +
                "     }";

        reset(_propertyDao);
        expect(_propertyDao.getProperty(IPropertyDao.SURVEY_DETAILS_ARTICLE, null)).andReturn(s);
        replay(_propertyDao);
        map = _sessionContext.getSurveyDetails("article");
        assertNotNull(map);
        assertFalse((Boolean)map.get("showSurveyHover"));
        assertFalse(map.containsKey("title"));
        assertFalse(map.containsKey("body"));
        assertFalse(map.containsKey("url"));
        verify(_propertyDao);
    }

    public void testIsHomepageDownloadHoverEnabled() {
        expect(_propertyDao.getProperty(IPropertyDao.HOMEPAGE_DOWNLOAD_HOVER_PCT, "0")).andReturn("0").anyTimes();
        replay(_propertyDao);
        assertFalse("Expect hover to be disabled when display percent is 0", _sessionContext.isValueValidForHomepageDownloadHover(0));
        assertFalse("Expect hover to be disabled when display percent is 0", _sessionContext.isValueValidForHomepageDownloadHover(10));
        assertFalse("Expect hover to be disabled when display percent is 0", _sessionContext.isValueValidForHomepageDownloadHover(99));
        assertFalse("Expect hover to be disabled when display percent is 0", _sessionContext.isHomepageDownloadHoverEnabled());
        verify(_propertyDao);

        reset(_propertyDao);

        expect(_propertyDao.getProperty(IPropertyDao.HOMEPAGE_DOWNLOAD_HOVER_PCT, "0")).andReturn("1").anyTimes();
        replay(_propertyDao);
        assertTrue(_sessionContext.isValueValidForHomepageDownloadHover(0));
        assertFalse(_sessionContext.isValueValidForHomepageDownloadHover(10));
        assertFalse(_sessionContext.isValueValidForHomepageDownloadHover(99));
        verify(_propertyDao);

        reset(_propertyDao);

        expect(_propertyDao.getProperty(IPropertyDao.HOMEPAGE_DOWNLOAD_HOVER_PCT, "0")).andReturn("50").anyTimes();
        replay(_propertyDao);
        assertTrue(_sessionContext.isValueValidForHomepageDownloadHover(0));
        assertTrue(_sessionContext.isValueValidForHomepageDownloadHover(10));
        assertFalse(_sessionContext.isValueValidForHomepageDownloadHover(99));
        verify(_propertyDao);

        reset(_propertyDao);

        expect(_propertyDao.getProperty(IPropertyDao.HOMEPAGE_DOWNLOAD_HOVER_PCT, "0")).andReturn("100").anyTimes();
        replay(_propertyDao);
        assertTrue("Expect hover to be enabled when display percent is 100", _sessionContext.isValueValidForHomepageDownloadHover(0));
        assertTrue("Expect hover to be enabled when display percent is 100", _sessionContext.isValueValidForHomepageDownloadHover(10));
        assertTrue("Expect hover to be enabled when display percent is 100", _sessionContext.isValueValidForHomepageDownloadHover(99));
        assertTrue("Expect hover to be enabled when display percent is 100", _sessionContext.isHomepageDownloadHoverEnabled());
        verify(_propertyDao);

        reset(_propertyDao);

        expect(_propertyDao.getProperty(IPropertyDao.HOMEPAGE_DOWNLOAD_HOVER_PCT, "0")).andReturn("foo").anyTimes();
        replay(_propertyDao);
        assertFalse("Expect hover to be disabled when display percent is NaN", _sessionContext.isValueValidForHomepageDownloadHover(0));
        assertFalse("Expect hover to be disabled when display percent is NaN", _sessionContext.isValueValidForHomepageDownloadHover(10));
        assertFalse("Expect hover to be disabled when display percent is NaN", _sessionContext.isValueValidForHomepageDownloadHover(99));
        verify(_propertyDao);

    }
}
