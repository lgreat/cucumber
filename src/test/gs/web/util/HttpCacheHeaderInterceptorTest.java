package gs.web.util;

import gs.web.BaseControllerTestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.mvc.Controller;

/**
 * @author <a href="mailto:thuss@greatschools.net">Todd Huss</a>
 */
public class HttpCacheHeaderInterceptorTest extends BaseControllerTestCase {

    private HttpCacheHeaderInterceptor _interceptor;

    public void setUp() throws Exception {
        super.setUp();
        _interceptor = new HttpCacheHeaderInterceptor();
    }

    public void testNoCacheHeaders() throws Exception {
        MockHttpServletRequest request = getRequest();
        MockHttpServletResponse response = getResponse();
        _interceptor.postHandle(request, response, null, null);

        // Verify that cache headers were set
        assertEquals("no-cache", response.getHeader(HttpCacheHeaderInterceptor.HEADER_CACHE_CONTROL));
        assertEquals("no-cache", response.getHeader(HttpCacheHeaderInterceptor.HEADER_PRAGMA));
        assertEquals((long) 0, response.getHeader(HttpCacheHeaderInterceptor.HEADER_EXPIRES));

    }

    public void testCacheHeaders() throws Exception {
        MockHttpServletRequest request = getRequest();
        MockHttpServletResponse response = getResponse();
        Controller controller = new CacheablePageParameterizableViewController();
        _interceptor.postHandle(request, response, controller, null);

        // Verify that cache headers were set
        assertEquals("public; max-age: 600", response.getHeader(HttpCacheHeaderInterceptor.HEADER_CACHE_CONTROL));
        assertEquals("", response.getHeader(HttpCacheHeaderInterceptor.HEADER_PRAGMA));
        assertTrue(((Long) response.getHeader(HttpCacheHeaderInterceptor.HEADER_EXPIRES)) > 0);
    }


    public void testCacheHeadersAlreadySet() throws Exception {
        MockHttpServletRequest request = getRequest();
        MockHttpServletResponse response = getResponse();
        response.setHeader(HttpCacheHeaderInterceptor.HEADER_CACHE_CONTROL, "XXX");
        _interceptor.postHandle(request, response, null, null);

        // Verify that cache headers were set
        assertEquals("XXX", response.getHeader(HttpCacheHeaderInterceptor.HEADER_CACHE_CONTROL));
        assertNull(response.getHeader(HttpCacheHeaderInterceptor.HEADER_PRAGMA));
        assertNull(response.getHeader(HttpCacheHeaderInterceptor.HEADER_EXPIRES));
    }
}
