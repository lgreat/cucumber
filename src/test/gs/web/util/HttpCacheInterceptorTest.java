package gs.web.util;

import gs.web.BaseControllerTestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.mvc.Controller;

/**
 * @author <a href="mailto:thuss@greatschools.net">Todd Huss</a>
 */
public class HttpCacheInterceptorTest extends BaseControllerTestCase {

    private HttpCacheInterceptor _interceptor;

    public void setUp() throws Exception {
        super.setUp();
        _interceptor = new HttpCacheInterceptor();
    }

    public void testNoCacheHeaders() throws Exception {
        MockHttpServletRequest request = getRequest();
        MockHttpServletResponse response = getResponse();
        _interceptor.postHandle(request, response, null, null);

        // Verify that no-cache headers were set
        assertEquals("no-cache", response.getHeader(HttpCacheInterceptor.HEADER_CACHE_CONTROL));
        assertEquals("no-cache", response.getHeader(HttpCacheInterceptor.HEADER_PRAGMA));
        assertEquals((long) 0, response.getHeader(HttpCacheInterceptor.HEADER_EXPIRES));

    }

    public void testCacheHeaders() throws Exception {
        MockHttpServletRequest request = getRequest();
        MockHttpServletResponse response = getResponse();
        Controller controller = new CacheablePageParameterizableViewController();
        _interceptor.postHandle(request, response, controller, null);

        // Verify that cache headers were set
        assertEquals("public; max-age: 600", response.getHeader(HttpCacheInterceptor.HEADER_CACHE_CONTROL));
        assertEquals("", response.getHeader(HttpCacheInterceptor.HEADER_PRAGMA));
        assertTrue(((Long) response.getHeader(HttpCacheInterceptor.HEADER_EXPIRES)) > 0);
    }


    public void testCacheHeadersAlreadySet() throws Exception {
        MockHttpServletRequest request = getRequest();
        MockHttpServletResponse response = getResponse();
        response.setHeader(HttpCacheInterceptor.HEADER_CACHE_CONTROL, "XXX");
        _interceptor.postHandle(request, response, null, null);

        // Verify that cache headers were set
        assertEquals("XXX", response.getHeader(HttpCacheInterceptor.HEADER_CACHE_CONTROL));
        assertNull(response.getHeader(HttpCacheInterceptor.HEADER_PRAGMA));
        assertNull(response.getHeader(HttpCacheInterceptor.HEADER_EXPIRES));
    }
}
