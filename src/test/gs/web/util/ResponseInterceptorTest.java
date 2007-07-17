package gs.web.util;

import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.context.SessionContext;

import javax.servlet.http.Cookie;

import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author <a href="mailto:thuss@greatschools.net">Todd Huss</a>
 */
public class ResponseInterceptorTest extends BaseControllerTestCase {

    private ResponseInterceptor _interceptor;
    private String _requestedServer = "someserver.greatschools.net";

    public void setUp() throws Exception {
        super.setUp();

        _interceptor = new ResponseInterceptor();
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

    public void testTrnoValueDefinesABVersion() throws Exception {
        Cookie trnoCookieA = new Cookie("TRNO", "1.192.1.1.1");
        MockHttpServletRequest request = getRequest();
        MockHttpServletResponse response = getResponse();
        request.setCookies(new Cookie[]{trnoCookieA});
        _interceptor.preHandle(request, response, null);

        assertEquals("Odd number before initial dot in TRNO cookie should set A/B version to a", "a", _sessionContext.getABVersion());

        Cookie trnoCookieB = new Cookie("TRNO", "2.192.1.1.1");
        request.setCookies(new Cookie[]{trnoCookieB});
        _interceptor.preHandle(request, response, null);

        assertEquals("Even number before initial dot in TRNO cookie should set A/B version to b", "b", _sessionContext.getABVersion());
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
        assertEquals("Unexpected cobrand cookie age", ResponseInterceptor.EXPIRE_AT_END_OF_SESSION, cobrandCookie.getMaxAge());
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
        assertEquals("Unexpected cobrand cookie age", ResponseInterceptor.EXPIRE_AT_END_OF_SESSION, cobrandCookie.getMaxAge());
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
        assertEquals("Unexpected cobrand cookie age", ResponseInterceptor.EXPIRE_AT_END_OF_SESSION, cobrandCookie.getMaxAge());
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
        assertEquals("Unexpected cobrand cookie age", ResponseInterceptor.EXPIRE_AT_END_OF_SESSION, cobrandCookie.getMaxAge());
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
