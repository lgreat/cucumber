package gs.web.util;

import gs.web.BaseControllerTestCase;

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
}
