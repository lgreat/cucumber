package gs.web.util;

import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextInterceptor;
import gs.web.util.context.SessionContextUtil;

import javax.servlet.http.Cookie;

import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author <a href="mailto:thuss@greatschools.net">Todd Huss</a>
 */
public class ResponseInterceptorTest extends BaseControllerTestCase {

    private ResponseInterceptor _interceptor;

    public void setUp() throws Exception {
        _interceptor = new ResponseInterceptor();
        super.setUp();
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


    // TODO: this test is fragile. shouldn't depend on sfgate being non-framed cobrand. Mock SessionContext
    public void testPreHandleSetsCobrandCookie() throws Exception {
        MockHttpServletRequest request = getRequest();
        MockHttpServletResponse response = getResponse();

        String requestedServer = "sfgate.greatschools.net";
        request.setServerName(requestedServer);

        // set up SessionContext and PageHelper
        SessionContextUtil ctxUtil = (SessionContextUtil) getApplicationContext().getBean(SessionContextUtil.BEAN_ID);
        SessionContextInterceptor sessionContextInterceptor = new SessionContextInterceptor();
        sessionContextInterceptor.setSessionContextUtil(ctxUtil);
        sessionContextInterceptor.preHandle(request, response, null);

        assertTrue("preHandle should always return true", _interceptor.preHandle(request, response, null));

        Cookie cobrandCookie = null;
        Cookie cookies[] = response.getCookies();
        assertNotNull("Expected to find cookies in response", cookies);
        for (int i = 0; i < cookies.length; i++) {
            if (ResponseInterceptor.COBRAND_COOKIE.equals(cookies[i].getName())) {
                cobrandCookie = cookies[i];
                break;
            }
        }
        assertEquals("Unexpected cobrand cookie value", requestedServer, cobrandCookie.getValue());
    }
}
