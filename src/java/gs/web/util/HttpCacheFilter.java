package gs.web.util;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Set HTTP cache headers via web.xml
 *
 * @author thuss
 */
public class HttpCacheFilter implements Filter {

    protected static HttpCacheInterceptor cacheInterceptor = new HttpCacheInterceptor();

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        cacheInterceptor.setCacheHeaders((HttpServletResponse) res);
        // pass the request/response on
        chain.doFilter(req, res);
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void destroy() {
    }
}
