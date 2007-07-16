package gs.web.util;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

/**
 * Based on http://randomcoder.com/articles/jsessionid-considered-harmful
 */
public class DisableUrlSessionFilter implements Filter {

    /**
     * Filters requests to disable URL-based session identifiers.
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // skip non-http requests
        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
        } else {

            HttpServletResponse httpResponse = (HttpServletResponse) response;

            // wrap response to remove jsessionid URL encoding
            HttpServletResponseWrapper wrappedResponse = new HttpServletResponseWrapper(httpResponse) {
                public String encodeRedirectUrl(String url) {
                    return url;
                }

                public String encodeRedirectURL(String url) {
                    return url;
                }

                public String encodeUrl(String url) {
                    return url;
                }

                public String encodeURL(String url) {
                    return url;
                }
            };
            chain.doFilter(request, wrappedResponse);
        }
    }

    /**
     * Unused.
     */
    public void init(FilterConfig config) throws ServletException {
    }

    /**
     * Unused.
     */
    public void destroy() {
    }
}
