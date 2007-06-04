package gs.web.util;

import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContextInterceptor;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.context.SessionContext;

import javax.servlet.http.Cookie;

import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;

/**
 * @author <a href="mailto:thuss@greatschools.net">Todd Huss</a>
 */
public class ResponseInterceptorTest extends BaseControllerTestCase {

    private ResponseInterceptor _interceptor;
    private SessionContextInterceptor _sessionContextInterceptor;
    private MockControl _mockSessionContext;
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

    public void testPreHandle() throws Exception {
        MockHttpServletRequest request = getRequest();
        MockHttpServletResponse response = getResponse();
        assertTrue(_interceptor.preHandle(request, response, null));

        // Verify that a TRNO cookie was set
        boolean hasCookie = false;
        Cookie cookies[] = response.getCookies();
        assertNotNull(cookies);
        for (int i = 0; i < cookies.length; i++) {
            if (SessionContextUtil.TRNO_COOKIE.equals(cookies[i].getName())) {
                hasCookie = true;
                String trnoValue = cookies[i].getValue();
                long trnoSecondsSinceEpoch = Long.valueOf(trnoValue.substring(0, trnoValue.indexOf(".")));
                if (trnoSecondsSinceEpoch % 2 == 0) {
                    assertEquals("b", _sessionContext.getABVersion());
                } else {
                    assertEquals("a", _sessionContext.getABVersion());
                }
            }
        }
        assertTrue(hasCookie);
    }

    public void testPostHandle() throws Exception {
        MockHttpServletRequest request = getRequest();
        MockHttpServletResponse response = getResponse();
        _interceptor.postHandle(request, response, null, null);

        // Verify that cache headers were set
        assertTrue(response.containsHeader(ResponseInterceptor.HEADER_CACHE_CONTROL));
        assertTrue(response.containsHeader(ResponseInterceptor.HEADER_PRAGMA));
        assertTrue(response.containsHeader(ResponseInterceptor.HEADER_EXPIRES));
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

    public void testCobrandCookieNotSetForNonCobrand() throws Exception {
        _request.setServerName(_requestedServer);
        setUpSessionContext(false, false);

        assertTrue("preHandle should always return true", _interceptor.preHandle(_request, _response, null));

        Cookie cobrandCookie = findCobrandCookie();
        assertNull("Cobrand cookie should not be set for non-cobrand domains", cobrandCookie);
    }

    public void testCobrandCookieNotSetForFramedCobrand() throws Exception {
        _request.setServerName(_requestedServer);
        setUpSessionContext(true, true);

        assertTrue("preHandle should always return true", _interceptor.preHandle(_request, _response, null));

        Cookie cobrandCookie = findCobrandCookie();
        assertNull("Cobrand cookie should not be set for framed domains", cobrandCookie);
    }

    public void testCobrandCookieUnSetForNonCobrand() throws Exception {
        _request.setCookies(new Cookie[]{new Cookie(SessionContextUtil.COBRAND_COOKIE, _requestedServer)});
        _request.setServerName(_requestedServer);
        setUpSessionContext(false, false);

        assertTrue("preHandle should always return true", _interceptor.preHandle(_request, _response, null));

        Cookie cobrandCookie = findCobrandCookie();
        assertEquals("Unexpected cobrand cookie value", "", cobrandCookie.getValue());
        assertEquals("Expected age 0 (delete) for cobrand cookie", ResponseInterceptor.EXPIRE_NOW, cobrandCookie.getMaxAge());
        assertEquals("Unexpected cobrand cookie path", "/", cobrandCookie.getPath());
        assertEquals("Unexpected cobrand cookie domain", ".greatschools.net", cobrandCookie.getDomain());
    }

    public void testCobrandCookieUnSetForFramedCobrand() throws Exception {
        _request.setCookies(new Cookie[]{new Cookie(SessionContextUtil.COBRAND_COOKIE, _requestedServer)});
        _request.setServerName(_requestedServer);
        setUpSessionContext(true, true);

        assertTrue("preHandle should always return true", _interceptor.preHandle(_request, _response, null));

        Cookie cobrandCookie = findCobrandCookie();
        assertEquals("Unexpected cobrand cookie value", "", cobrandCookie.getValue());
        assertEquals("Expected age 0 (delete) for cobrand cookie", ResponseInterceptor.EXPIRE_NOW, cobrandCookie.getMaxAge());
        assertEquals("Unexpected cobrand cookie path", "/", cobrandCookie.getPath());
        assertEquals("Unexpected cobrand cookie domain", ".greatschools.net", cobrandCookie.getDomain());
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
            for (int i = 0; i < cookies.length; i++) {
                if (SessionContextUtil.COBRAND_COOKIE.equals(cookies[i].getName())) {
                    return cookies[i];
                }
            }
        }
        return null;
    }
}
