package gs.web.util;

import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.Cookie;

/**
 * @author <a href="mailto:thuss@greatschools.net">Todd Huss</a>
 */
public class CookieInterceptorTest extends BaseControllerTestCase {

    private CookieInterceptor _interceptor;
    private String _requestedServer = "someserver.greatschools.net";

    public void setUp() throws Exception {
        super.setUp();

        _interceptor = new CookieInterceptor();
        _sessionContext = new SessionContext();
        _request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, _sessionContext);
    }

    private void setUpSessionContext(boolean isCobranded, boolean isFramed) {
        if (isCobranded) _sessionContext.setCobrand("sfgate");
        if (isFramed) _sessionContext.setCobrand("framed");
        _sessionContext.setHostName(_requestedServer);
    }

    public void testPreHandleShouldSetTrnoCookie() throws Exception {
        MockHttpServletRequest request = getRequest();
        MockHttpServletResponse response = getResponse();
        assertTrue(_interceptor.preHandle(request, response, null));

        // Verify that a TRNO cookie was set
        boolean hasCookie = false;
        Cookie cookies[] = response.getCookies();
        assertNotNull(cookies);
        for (Cookie cooky : cookies) {
            if (SessionContextUtil.TRNO_COOKIE.equals(cooky.getName())) {
                hasCookie = true;
            }
        }
        assertTrue(hasCookie);
    }

    public void testConvertABConfigToArray() {
        CookieInterceptor._abCutoffs = null;
        assertNull(CookieInterceptor._abCutoffs);
        CookieInterceptor.convertABConfigToArray("70/15/15");
        assertNotNull(CookieInterceptor._abCutoffs);
        int[] ar = CookieInterceptor._abCutoffs;
        assertEquals(3, ar.length);
        assertEquals(70, ar[0]);
        assertEquals(15, ar[1]);
        assertEquals(15, ar[2]);
        assertEquals(100, CookieInterceptor._cutoffTotal);

        CookieInterceptor._abCutoffs = null;
        CookieInterceptor.convertABConfigToArray("33/33/33");
        assertNotNull(CookieInterceptor._abCutoffs);
        ar = CookieInterceptor._abCutoffs;
        assertEquals(3, ar.length);
        assertEquals(33, ar[0]);
        assertEquals(33, ar[1]);
        assertEquals(33, ar[2]);
        assertEquals(99, CookieInterceptor._cutoffTotal);

        CookieInterceptor._abCutoffs = null;
        CookieInterceptor.convertABConfigToArray("1/1");
        assertNotNull(CookieInterceptor._abCutoffs);
        ar = CookieInterceptor._abCutoffs;
        assertEquals(2, ar.length);
        assertEquals(1, ar[0]);
        assertEquals(1, ar[1]);
        assertEquals(2, CookieInterceptor._cutoffTotal);

        CookieInterceptor._abCutoffs = null;
        CookieInterceptor.convertABConfigToArray("50");
        assertNull(CookieInterceptor._abCutoffs);

        CookieInterceptor._abCutoffs = null;
        CookieInterceptor.convertABConfigToArray("70/20/20");
        assertNull(CookieInterceptor._abCutoffs);

        CookieInterceptor._abCutoffs = null;
        CookieInterceptor.convertABConfigToArray("110/5/5");
        assertNull("Expect no result from values over 100", CookieInterceptor._abCutoffs);

        CookieInterceptor._abCutoffs = null;
        CookieInterceptor.convertABConfigToArray("0/5/5");
        assertNull("Expect no result from values less than 1", CookieInterceptor._abCutoffs);

        CookieInterceptor._abCutoffs = null;
        CookieInterceptor.convertABConfigToArray(null); // no crash on null
        assertNull(CookieInterceptor._abCutoffs);
    }

    public void testDetermineVariantFromConfiguration() {
        CookieInterceptor._abCutoffs = new int[] {50,50};
        CookieInterceptor._cutoffTotal = 100;
        _interceptor.determineVariantFromConfiguration(0, _sessionContext);
        assertEquals("a", _sessionContext.getABVersion());
        _interceptor.determineVariantFromConfiguration(49, _sessionContext);
        assertEquals("a", _sessionContext.getABVersion());
        _interceptor.determineVariantFromConfiguration(50, _sessionContext);
        assertEquals("b", _sessionContext.getABVersion());
        _interceptor.determineVariantFromConfiguration(99, _sessionContext);
        assertEquals("b", _sessionContext.getABVersion());
        _interceptor.determineVariantFromConfiguration(100, _sessionContext);
        assertEquals("a", _sessionContext.getABVersion());

        CookieInterceptor._abCutoffs = new int[] {70,15,15};
        _interceptor.determineVariantFromConfiguration(0, _sessionContext);
        assertEquals("a", _sessionContext.getABVersion());
        _interceptor.determineVariantFromConfiguration(70, _sessionContext);
        assertEquals("b", _sessionContext.getABVersion());
        _interceptor.determineVariantFromConfiguration(85, _sessionContext);
        assertEquals("c", _sessionContext.getABVersion());

        CookieInterceptor._abCutoffs = new int[] {1,1};
        CookieInterceptor._cutoffTotal = 2;
        _interceptor.determineVariantFromConfiguration(0, _sessionContext);
        assertEquals("a", _sessionContext.getABVersion());
        _interceptor.determineVariantFromConfiguration(1, _sessionContext);
        assertEquals("b", _sessionContext.getABVersion());
        _interceptor.determineVariantFromConfiguration(2, _sessionContext);
        assertEquals("a", _sessionContext.getABVersion());
        _sessionContext.setAbVersion(null);
        _interceptor.determineVariantFromConfiguration(System.currentTimeMillis() / 1000, _sessionContext);
        assertNotNull(_sessionContext.getABVersion());

        CookieInterceptor._abCutoffs = new int[] {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
        CookieInterceptor._cutoffTotal = 26;
        _interceptor.determineVariantFromConfiguration(0, _sessionContext);
        assertEquals("a", _sessionContext.getABVersion());
        _interceptor.determineVariantFromConfiguration(17, _sessionContext);
        assertEquals("r", _sessionContext.getABVersion());
        _interceptor.determineVariantFromConfiguration(14, _sessionContext);
        assertEquals("o", _sessionContext.getABVersion());
        _interceptor.determineVariantFromConfiguration(24, _sessionContext);
        assertEquals("y", _sessionContext.getABVersion());
        _interceptor.determineVariantFromConfiguration(25, _sessionContext);
        assertEquals("z", _sessionContext.getABVersion());
        _interceptor.determineVariantFromConfiguration(26, _sessionContext);
        assertEquals("a", _sessionContext.getABVersion());
    }

    private static GsMockHttpServletRequest getRequestWithUserAgent(String userAgent) {
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setServerName("www.greatschools.net");
        request.addHeader("User-Agent", userAgent);
        return request;
    }

    public void testIsKnownCrawler() {
        assertFalse(_interceptor.isKnownCrawler(getRequestWithUserAgent("Mozilla/4.0 (compatible; MSIE 4.01; Windows 95)")));
        assertTrue(_interceptor.isKnownCrawler(getRequestWithUserAgent("Mozilla/5.0 (compatible; Googlebot/2.1; http://www.google.com/bot.html)")));
        assertTrue(_interceptor.isKnownCrawler(getRequestWithUserAgent("Mozilla/5.0 (compatible; Yahoo! Slurp; http://help.yahoo.com/help/us/ysearch/slurp)")));
    }

    public void testKnownCrawlerOverridesABValue() {
        CookieInterceptor._abCutoffs = new int[] {1,1};
        CookieInterceptor._cutoffTotal = 2;

        Cookie trnoCookieA = new Cookie("TRNO", "1.192.1.1.1");
        MockHttpServletRequest request = getRequest();
        request.setCookies(new Cookie[]{trnoCookieA});
        _interceptor.determineAbVersion(trnoCookieA, request, _sessionContext);
        assertEquals("Expect b variant from normal user agent", "b", _sessionContext.getABVersion());

        _interceptor.determineAbVersion(trnoCookieA, getRequestWithUserAgent("Mozilla/5.0 (compatible; Yahoo! Slurp; http://help.yahoo.com/help/us/ysearch/slurp)"), _sessionContext);
        assertEquals("Expect a variant from crawler user agent", "a", _sessionContext.getABVersion());
    }

    public void testConvertABConfigurationToString() {
        CookieInterceptor._abCutoffs = new int[] {1,1};
        CookieInterceptor._cutoffTotal = 2;
        assertEquals("A/B: 50/50", CookieInterceptor.convertABConfigurationToString());

        CookieInterceptor._abCutoffs = new int[] {4,1};
        CookieInterceptor._cutoffTotal = 5;
        assertEquals("A/B: 80/20", CookieInterceptor.convertABConfigurationToString());

        CookieInterceptor._abCutoffs = new int[] {14,3,3};
        CookieInterceptor._cutoffTotal = 20;
        assertEquals("A/B/C: 70/15/15", CookieInterceptor.convertABConfigurationToString());
    }

    public void testVersionParameterShouldOverrideABValueFromTrnoCooki() throws Exception {
        Cookie trnoCookieA = new Cookie("TRNO", "1.192.1.1.1");
        MockHttpServletRequest request = getRequest();
        MockHttpServletResponse response = getResponse();
        request.setCookies(new Cookie[]{trnoCookieA});
        request.setParameter(SessionContextUtil.VERSION_PARAM, "b");
        _interceptor.preHandle(request, response, null);

        assertEquals("Version parameter should override A/B version from TRNO cookie", "b", _sessionContext.getABVersion());

        Cookie trnoCookieB = new Cookie("TRNO", "2.192.1.1.1");
        request.setCookies(new Cookie[]{trnoCookieB});
        request.setParameter(SessionContextUtil.VERSION_PARAM, "a");
        _interceptor.preHandle(request, response, null);

        assertEquals("Version parameter should override A/B version from TRNO cookie", "a", _sessionContext.getABVersion());
    }

    public void testPreHandleSetsCobrandCookie() throws Exception {
        _request.setServerName(_requestedServer);
        setUpSessionContext(true, false);

        assertTrue("preHandle should always return true", _interceptor.preHandle(_request, _response, null));

        Cookie cobrandCookie = findCobrandCookie();
        assertEquals("Unexpected cobrand cookie value", _requestedServer, cobrandCookie.getValue());
        assertEquals("Unexpected cobrand cookie path", "/", cobrandCookie.getPath());
        assertEquals("Unexpected cobrand cookie age", CookieInterceptor.EXPIRE_AT_END_OF_SESSION, cobrandCookie.getMaxAge());
        assertEquals("Unexpected cobrand cookie domain", ".greatschools.net", cobrandCookie.getDomain());
    }

    public void testCobrandCookieNotReSetForCobrand() throws Exception {
        _request.setCookies(new Cookie[]{new Cookie(SessionContextUtil.COBRAND_COOKIE, _requestedServer)});
        _request.setServerName(_requestedServer);
        setUpSessionContext(true, false);

        assertTrue("preHandle should always return true", _interceptor.preHandle(_request, _response, null));

        Cookie cobrandCookie = findCobrandCookie();
        assertNull("Cobrand cookie should not be set again for cobrand domains", cobrandCookie);
    }

    public void testCobrandCookieSetForNonCobrand() throws Exception {
        _request.setServerName(_requestedServer);
        setUpSessionContext(false, false);

        assertTrue("preHandle should always return true", _interceptor.preHandle(_request, _response, null));

        Cookie cobrandCookie = findCobrandCookie();
        assertNotNull("Cobrand cookie should be set even for non-cobrand domains", cobrandCookie);
        assertEquals("Unexpected cobrand cookie value", _requestedServer, cobrandCookie.getValue());
        assertEquals("Unexpected cobrand cookie path", "/", cobrandCookie.getPath());
        assertEquals("Unexpected cobrand cookie age", CookieInterceptor.EXPIRE_AT_END_OF_SESSION, cobrandCookie.getMaxAge());
        assertEquals("Unexpected cobrand cookie domain", ".greatschools.net", cobrandCookie.getDomain());
    }

    public void testCobrandCookieSetForFramedCobrand() throws Exception {
        _request.setServerName(_requestedServer);
        setUpSessionContext(true, true);

        assertTrue("preHandle should always return true", _interceptor.preHandle(_request, _response, null));

        Cookie cobrandCookie = findCobrandCookie();
        assertNotNull("Cobrand cookie should be set even for framed domains", cobrandCookie);
        assertEquals("Unexpected cobrand cookie value", _requestedServer, cobrandCookie.getValue());
        assertEquals("Unexpected cobrand cookie path", "/", cobrandCookie.getPath());
        assertEquals("Unexpected cobrand cookie age", CookieInterceptor.EXPIRE_AT_END_OF_SESSION, cobrandCookie.getMaxAge());
        assertEquals("Unexpected cobrand cookie domain", ".greatschools.net", cobrandCookie.getDomain());
    }

    public void testCobrandCookieNotReSetForNonCobrand() throws Exception {
        _request.setCookies(new Cookie[]{new Cookie(SessionContextUtil.COBRAND_COOKIE, _requestedServer)});
        _request.setServerName(_requestedServer);
        setUpSessionContext(false, false);

        assertTrue("preHandle should always return true", _interceptor.preHandle(_request, _response, null));

        Cookie cobrandCookie = findCobrandCookie();
        assertNull("Cobrand cookie should not be set again for any domain", cobrandCookie);
    }

    public void testCobrandCookieNotReSetForFramedCobrand() throws Exception {
        _request.setCookies(new Cookie[]{new Cookie(SessionContextUtil.COBRAND_COOKIE, _requestedServer)});
        _request.setServerName(_requestedServer);
        setUpSessionContext(true, true);

        assertTrue("preHandle should always return true", _interceptor.preHandle(_request, _response, null));

        Cookie cobrandCookie = findCobrandCookie();
        assertNull("Cobrand cookie should not be set again even for framed cobrand domains", cobrandCookie);
    }


    public void testPreHandleUpdatesCobrandCookie() throws Exception {
        _request.setCookies(new Cookie[]{new Cookie(SessionContextUtil.COBRAND_COOKIE, "some.other.domain")});
        _request.setServerName(_requestedServer);
        setUpSessionContext(true, false);

        assertTrue("preHandle should always return true", _interceptor.preHandle(_request, _response, null));

        Cookie cobrandCookie = findCobrandCookie();
        assertEquals("Unexpected cobrand cookie value", _requestedServer, cobrandCookie.getValue());
        assertEquals("Unexpected cobrand cookie path", "/", cobrandCookie.getPath());
        assertEquals("Unexpected cobrand cookie age", CookieInterceptor.EXPIRE_AT_END_OF_SESSION, cobrandCookie.getMaxAge());
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
