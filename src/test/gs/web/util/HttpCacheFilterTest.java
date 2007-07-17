package gs.web.util;

import gs.web.BaseControllerTestCase;
import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author <a href="mailto:thuss@greatschools.net">Todd Huss</a>
 */
public class HttpCacheFilterTest extends BaseControllerTestCase {

    public void testHeaderFilter() throws IOException, ServletException {
        HttpCacheFilter cacheFilter = new HttpCacheFilter();
        MockHttpServletRequest request = getRequest();
        MockHttpServletResponse response = getResponse();
        MockFilterChain chain = new MockFilterChain();
        cacheFilter.init(null);
        cacheFilter.doFilter(request, response, chain);

        // Verify that cache headers were set
        assertEquals("public; max-age: 600", response.getHeader(HttpCacheInterceptor.HEADER_CACHE_CONTROL));
        assertEquals("", response.getHeader(HttpCacheInterceptor.HEADER_PRAGMA));
        assertTrue(((Long) response.getHeader(HttpCacheInterceptor.HEADER_EXPIRES)) > 0);
        
        cacheFilter.destroy();
    }
}

