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

    public void setUp() throws Exception {
        super.setUp();

        _interceptor = new ResponseInterceptor();
        _mockSessionContext = MockClassControl.createControl(SessionContext.class);
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
        String requestedServer = "sfgate.greatschools.net";
        _request.setServerName(requestedServer);
        setUpSessionContext(requestedServer, true, false);

        assertTrue("preHandle should always return true", _interceptor.preHandle(_request, _response, null));

        Cookie cobrandCookie = null;
        Cookie cookies[] = _response.getCookies();
        assertNotNull("Expected to find cookies in response", cookies);
        for (int i = 0; i < cookies.length; i++) {
            if (ResponseInterceptor.COBRAND_COOKIE.equals(cookies[i].getName())) {
                cobrandCookie = cookies[i];
                break;
            }
        }
        assertEquals("Unexpected cobrand cookie value", requestedServer, cobrandCookie.getValue());

        _mockSessionContext.verify();

    }

    private void setUpSessionContext(String requestedServer, boolean isCobranded, boolean isFramed) {
        _sessionContext.isCobranded();
        _mockSessionContext.setReturnValue(isCobranded);
        _sessionContext.isFramed();
        _mockSessionContext.setReturnValue(isFramed);
        _sessionContext.getHostName();
        _mockSessionContext.setReturnValue(requestedServer);
        _mockSessionContext.replay();
    }
}
