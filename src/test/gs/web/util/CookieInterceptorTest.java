package gs.web.util;

import gs.data.admin.IPropertyDao;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.easymock.EasyMock.*;

import javax.servlet.http.Cookie;

/**
 * @author <a href="mailto:thuss@greatschools.org">Todd Huss</a>
 * @author <a href="mailto:aroy@greatschools.org">Anthony Roy</a>
 */
public class CookieInterceptorTest extends BaseControllerTestCase {

    private CookieInterceptor _interceptor;
    private String _requestedServer = "someserver.greatschools.org";

    public void setUp() throws Exception {
        super.setUp();

        _interceptor = new CookieInterceptor();
        _sessionContext = new SessionContext();
        IPropertyDao propertyDao = createMock(IPropertyDao.class);
        _interceptor.setPropertyDao(propertyDao);
        _sessionContext.setPropertyDao(propertyDao);
        _request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, _sessionContext);
    }

    private void setUpSessionContext(boolean isCobranded, boolean isFramed) {
        if (isCobranded) _sessionContext.setCobrand("sfgate");
        if (isFramed) _sessionContext.setCobrand("framed");
        _sessionContext.setHostName(_requestedServer);
    }

    public void testPreHandleShouldSetTrackingNumberCookie() throws Exception {
        MockHttpServletRequest request = getRequest();
        MockHttpServletResponse response = getResponse();

        expect(_interceptor.getPropertyDao().getProperty(IPropertyDao.VARIANT_CONFIGURATION)).andReturn("1/1");
        expect(_interceptor.getPropertyDao().getProperty(IPropertyDao.ADVERTISING_ENABLED_KEY, "true")).andReturn("true");
        replay(_interceptor.getPropertyDao());
        
        assertTrue(_interceptor.preHandle(request, response, null));
        verify(_interceptor.getPropertyDao());

        // Verify that a Tracking number cookie was set
        boolean hasCookie = false;
        Cookie cookies[] = response.getCookies();
        assertNotNull(cookies);
        for (Cookie cooky : cookies) {
            if (SessionContextUtil.TRACKING_NUMBER.equals(cooky.getName())) {
                hasCookie = true;
            }
        }
        assertTrue(hasCookie);
    }

    private static GsMockHttpServletRequest getRequestWithUserAgent(String userAgent) {
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setServerName("www.greatschools.org");
        request.addHeader("User-Agent", userAgent);
        return request;
    }

    public void testIsKnownCrawler() {
        assertFalse(_interceptor.isKnownCrawler(getRequestWithUserAgent("Mozilla/4.0 (compatible; MSIE 4.01; Windows 95)")));
        assertTrue(_interceptor.isKnownCrawler(getRequestWithUserAgent("Mozilla/5.0 (compatible; Googlebot/2.1; http://www.google.com/bot.html)")));
        assertTrue(_interceptor.isKnownCrawler(getRequestWithUserAgent("Mozilla/5.0 (compatible; Yahoo! Slurp; http://help.yahoo.com/help/us/ysearch/slurp)")));
    }

    public void testKnownCrawlerOverridesABValue() {
        Cookie trnoCookieA = new Cookie("TRACKING_NUMBER", "1");
        MockHttpServletRequest request = getRequest();
        request.setCookies(new Cookie[]{trnoCookieA});
        _interceptor.determineAbVersion(trnoCookieA, request, _sessionContext);
        assertEquals("Expect b variant from normal user agent", "b", _sessionContext.getABVersion());

        _interceptor.determineAbVersion(trnoCookieA, getRequestWithUserAgent("Mozilla/5.0 (compatible; Yahoo! Slurp; http://help.yahoo.com/help/us/ysearch/slurp)"), _sessionContext);
        assertEquals("Expect a variant from crawler user agent", "a", _sessionContext.getABVersion());
    }

    public void testVersionParameterShouldOverrideABValueFromTrackingNumberCookie() throws Exception {
        Cookie trnoCookieA = new Cookie("TRACKING_NUMBER", "1");
        MockHttpServletRequest request = getRequest();
        MockHttpServletResponse response = getResponse();
        request.setCookies(new Cookie[]{trnoCookieA});

        expect(_interceptor.getPropertyDao().getProperty(IPropertyDao.VARIANT_CONFIGURATION)).andReturn("1/1");
        expect(_interceptor.getPropertyDao().getProperty(IPropertyDao.ADVERTISING_ENABLED_KEY, "true")).andReturn("true");
        replay(_interceptor.getPropertyDao());

        _interceptor.preHandle(request, response, null);
        verify(_interceptor.getPropertyDao());

        reset(_interceptor.getPropertyDao());

        assertEquals("Expect b from trno value 1", "b", _sessionContext.getABVersion());

        request.setParameter(SessionContextUtil.VERSION_PARAM, "a");

        expect(_interceptor.getPropertyDao().getProperty(IPropertyDao.ADVERTISING_ENABLED_KEY, "true")).andReturn("true");
        replay(_interceptor.getPropertyDao());

        _interceptor.preHandle(request, response, null);
        verify(_interceptor.getPropertyDao());

        assertEquals("Version parameter should override A/B version from TRNO cookie", "a", _sessionContext.getABVersion());
    }

    public void testPreHandleSetsCobrandCookie() throws Exception {
        _request.setServerName(_requestedServer);
        setUpSessionContext(true, false);

        expect(_interceptor.getPropertyDao().getProperty(IPropertyDao.VARIANT_CONFIGURATION)).andReturn("1/1");
        expect(_interceptor.getPropertyDao().getProperty(IPropertyDao.ADVERTISING_ENABLED_KEY, "true")).andReturn("true");
        replay(_interceptor.getPropertyDao());

        assertTrue("preHandle should always return true", _interceptor.preHandle(_request, _response, null));
        verify(_interceptor.getPropertyDao());

        Cookie cobrandCookie = findCobrandCookie();
        assertEquals("Unexpected cobrand cookie value", _requestedServer, cobrandCookie.getValue());
        assertEquals("Unexpected cobrand cookie path", "/", cobrandCookie.getPath());
        assertEquals("Unexpected cobrand cookie age", CookieInterceptor.EXPIRE_AT_END_OF_SESSION, cobrandCookie.getMaxAge());
        assertEquals("Unexpected cobrand cookie domain", ".greatschools.org", cobrandCookie.getDomain());
    }

    public void testCobrandCookieNotReSetForCobrand() throws Exception {
        _request.setCookies(new Cookie[]{new Cookie(SessionContextUtil.COBRAND_COOKIE, _requestedServer)});
        _request.setServerName(_requestedServer);
        setUpSessionContext(true, false);

        expect(_interceptor.getPropertyDao().getProperty(IPropertyDao.VARIANT_CONFIGURATION)).andReturn("1/1");
        expect(_interceptor.getPropertyDao().getProperty(IPropertyDao.ADVERTISING_ENABLED_KEY, "true")).andReturn("true");
        replay(_interceptor.getPropertyDao());

        assertTrue("preHandle should always return true", _interceptor.preHandle(_request, _response, null));
        verify(_interceptor.getPropertyDao());

        Cookie cobrandCookie = findCobrandCookie();
        assertNull("Cobrand cookie should not be set again for cobrand domains", cobrandCookie);
    }

    public void testCobrandCookieSetForNonCobrand() throws Exception {
        _request.setServerName(_requestedServer);
        setUpSessionContext(false, false);

        expect(_interceptor.getPropertyDao().getProperty(IPropertyDao.VARIANT_CONFIGURATION)).andReturn("1/1");
        expect(_interceptor.getPropertyDao().getProperty(IPropertyDao.ADVERTISING_ENABLED_KEY, "true")).andReturn("true");
        replay(_interceptor.getPropertyDao());

        assertTrue("preHandle should always return true", _interceptor.preHandle(_request, _response, null));
        verify(_interceptor.getPropertyDao());

        Cookie cobrandCookie = findCobrandCookie();
        assertNotNull("Cobrand cookie should be set even for non-cobrand domains", cobrandCookie);
        assertEquals("Unexpected cobrand cookie value", _requestedServer, cobrandCookie.getValue());
        assertEquals("Unexpected cobrand cookie path", "/", cobrandCookie.getPath());
        assertEquals("Unexpected cobrand cookie age", CookieInterceptor.EXPIRE_AT_END_OF_SESSION, cobrandCookie.getMaxAge());
        assertEquals("Unexpected cobrand cookie domain", ".greatschools.org", cobrandCookie.getDomain());
    }

    public void testCobrandCookieSetForFramedCobrand() throws Exception {
        _request.setServerName(_requestedServer);
        setUpSessionContext(true, true);

        expect(_interceptor.getPropertyDao().getProperty(IPropertyDao.VARIANT_CONFIGURATION)).andReturn("1/1");
        expect(_interceptor.getPropertyDao().getProperty(IPropertyDao.ADVERTISING_ENABLED_KEY, "true")).andReturn("true");
        replay(_interceptor.getPropertyDao());

        assertTrue("preHandle should always return true", _interceptor.preHandle(_request, _response, null));
        verify(_interceptor.getPropertyDao());

        Cookie cobrandCookie = findCobrandCookie();
        assertNotNull("Cobrand cookie should be set even for framed domains", cobrandCookie);
        assertEquals("Unexpected cobrand cookie value", _requestedServer, cobrandCookie.getValue());
        assertEquals("Unexpected cobrand cookie path", "/", cobrandCookie.getPath());
        assertEquals("Unexpected cobrand cookie age", CookieInterceptor.EXPIRE_AT_END_OF_SESSION, cobrandCookie.getMaxAge());
        assertEquals("Unexpected cobrand cookie domain", ".greatschools.org", cobrandCookie.getDomain());
    }

    public void testCobrandCookieNotReSetForNonCobrand() throws Exception {
        _request.setCookies(new Cookie[]{new Cookie(SessionContextUtil.COBRAND_COOKIE, _requestedServer)});
        _request.setServerName(_requestedServer);
        setUpSessionContext(false, false);

        expect(_interceptor.getPropertyDao().getProperty(IPropertyDao.VARIANT_CONFIGURATION)).andReturn("1/1");
        expect(_interceptor.getPropertyDao().getProperty(IPropertyDao.ADVERTISING_ENABLED_KEY, "true")).andReturn("true");
        replay(_interceptor.getPropertyDao());

        assertTrue("preHandle should always return true", _interceptor.preHandle(_request, _response, null));
        verify(_interceptor.getPropertyDao());

        Cookie cobrandCookie = findCobrandCookie();
        assertNull("Cobrand cookie should not be set again for any domain", cobrandCookie);
    }

    public void testCobrandCookieNotReSetForFramedCobrand() throws Exception {
        _request.setCookies(new Cookie[]{new Cookie(SessionContextUtil.COBRAND_COOKIE, _requestedServer)});
        _request.setServerName(_requestedServer);
        setUpSessionContext(true, true);

        expect(_interceptor.getPropertyDao().getProperty(IPropertyDao.VARIANT_CONFIGURATION)).andReturn("1/1");
        expect(_interceptor.getPropertyDao().getProperty(IPropertyDao.ADVERTISING_ENABLED_KEY, "true")).andReturn("true");
        replay(_interceptor.getPropertyDao());

        assertTrue("preHandle should always return true", _interceptor.preHandle(_request, _response, null));
        verify(_interceptor.getPropertyDao());

        Cookie cobrandCookie = findCobrandCookie();
        assertNull("Cobrand cookie should not be set again even for framed cobrand domains", cobrandCookie);
    }


    public void testPreHandleUpdatesCobrandCookie() throws Exception {
        _request.setCookies(new Cookie[]{new Cookie(SessionContextUtil.COBRAND_COOKIE, "some.other.domain")});
        _request.setServerName(_requestedServer);
        setUpSessionContext(true, false);

        expect(_interceptor.getPropertyDao().getProperty(IPropertyDao.VARIANT_CONFIGURATION)).andReturn("1/1");
        expect(_interceptor.getPropertyDao().getProperty(IPropertyDao.ADVERTISING_ENABLED_KEY, "true")).andReturn("true");
        replay(_interceptor.getPropertyDao());

        assertTrue("preHandle should always return true", _interceptor.preHandle(_request, _response, null));
        verify(_interceptor.getPropertyDao());

        Cookie cobrandCookie = findCobrandCookie();
        assertEquals("Unexpected cobrand cookie value", _requestedServer, cobrandCookie.getValue());
        assertEquals("Unexpected cobrand cookie path", "/", cobrandCookie.getPath());
        assertEquals("Unexpected cobrand cookie age", CookieInterceptor.EXPIRE_AT_END_OF_SESSION, cobrandCookie.getMaxAge());
    }

    public void testRegressionGS_9049() {
        SessionContext sessionContext = (SessionContext) _request.getAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME);
        _request.setServerName("res2.greatschools.org");
        sessionContext.setCobrand("res2");
        sessionContext.setHostName("res2.greatschools.org");

        _interceptor.buildCobrandCookie(_request,
                (SessionContext) _request.getAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME), _response);

        Cookie cobrandCookie = findCobrandCookie();
        assertNull(cobrandCookie);
    }

    public void testSetAnalyticsIdCookieIfNecessarySetsCookie() {
        Cookie cookie = _interceptor.setAnalyticsIdCookieIfNecessary(_request, _response);
        assertNotNull(cookie);
        assertTrue("Expect a uuid of at least 10 characters", cookie.getValue().length() > 10);
        assertEquals(SessionContextUtil.ANALYTICS_ID_COOKIE_NAME, cookie.getName());
    }

    public void testSetAnalyticsIdCookieIfNecessaryReadsExistingCookie() {
        _request.setCookies(new Cookie(SessionContextUtil.ANALYTICS_ID_COOKIE_NAME, "1234567"));
        setUpSessionContext(true, true);
        Cookie cookie = _interceptor.setAnalyticsIdCookieIfNecessary(_request, _response);
        assertNotNull(cookie);
        assertEquals("1234567", cookie.getValue());
        assertEquals(SessionContextUtil.ANALYTICS_ID_COOKIE_NAME, cookie.getName());
    }

    private Cookie findCobrandCookie() {
        Cookie cookies[] = _response.getCookies();
        if (cookies != null) {
            for (Cookie cooky : cookies) {
                if (SessionContextUtil.COBRAND_COOKIE.equals(cooky.getName())) {
                    return cooky;
                }
            }
        }
        return null;
    }
}
