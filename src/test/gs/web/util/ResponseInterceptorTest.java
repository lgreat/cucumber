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
        _mockSessionContext = MockClassControl.createNiceControl(SessionContext.class);
        _sessionContext = (SessionContext) _mockSessionContext.getMock();
        _request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, _sessionContext);
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
            if (ResponseInterceptor.TRNO_COOKIE.equals(cookies[i].getName())) hasCookie = true;
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
        assertEquals("Unexpected cobrand cookie age", -1, cobrandCookie.getMaxAge());
        assertEquals("Unexpected cobrand cookie domain", "greatschools.net", cobrandCookie.getDomain());
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
        _request.setCookies(new Cookie[]{new Cookie(ResponseInterceptor.COBRAND_COOKIE, _requestedServer)});
        _request.setServerName(_requestedServer);
        setUpSessionContext(false, false);

        assertTrue("preHandle should always return true", _interceptor.preHandle(_request, _response, null));

        Cookie cobrandCookie = findCobrandCookie();
        assertEquals("Expected age 0 (delete) for cobrand cookie", 0, cobrandCookie.getMaxAge());
    }

    public void testCobrandCookieUnSetForFramedCobrand() throws Exception {
        _request.setCookies(new Cookie[]{new Cookie(ResponseInterceptor.COBRAND_COOKIE, _requestedServer)});
        _request.setServerName(_requestedServer);
        setUpSessionContext(true, true);

        assertTrue("preHandle should always return true", _interceptor.preHandle(_request, _response, null));

        Cookie cobrandCookie = findCobrandCookie();
        assertEquals("Expected age 0 (delete) for cobrand cookie", 0, cobrandCookie.getMaxAge());
    }


    public void testPreHandleUpdatesCobrandCookie() throws Exception {
        _request.setCookies(new Cookie[]{new Cookie(ResponseInterceptor.COBRAND_COOKIE, "some.other.domain")});
        _request.setServerName(_requestedServer);
        setUpSessionContext(true, false);

        assertTrue("preHandle should always return true", _interceptor.preHandle(_request, _response, null));

        Cookie cobrandCookie = findCobrandCookie();
        assertEquals("Unexpected cobrand cookie value", _requestedServer, cobrandCookie.getValue());
        assertEquals("Unexpected cobrand cookie path", "/", cobrandCookie.getPath());
        assertEquals("Unexpected cobrand cookie age", -1, cobrandCookie.getMaxAge());
    }

    private void setUpSessionContext(boolean isCobranded, boolean isFramed) {
        _sessionContext.isCobranded();
        _mockSessionContext.setReturnValue(isCobranded);
        _sessionContext.isFramed();
        _mockSessionContext.setReturnValue(isFramed);
        _sessionContext.getHostName();
        _mockSessionContext.setReturnValue(_requestedServer);
        _mockSessionContext.replay();
    }

    private Cookie findCobrandCookie() {
        Cookie cookies[] = _response.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                if (ResponseInterceptor.COBRAND_COOKIE.equals(cookies[i].getName())) {
                    return cookies[i];
                }
            }
        }
        return null;
    }
}
