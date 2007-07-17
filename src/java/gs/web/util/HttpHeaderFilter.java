package gs.web.util;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Set HTTP header params as specified in web.xml
 * Code from: http://www.onjava.com/pub/a/onjava/2004/03/03/filters.html?page=1
 *
 * @author thuss
 */
public class HttpHeaderFilter implements Filter {

    FilterConfig fc;

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        HttpServletResponse response = (HttpServletResponse) res;
        // set the provided HTTP response parameters
        for (Enumeration e = fc.getInitParameterNames(); e.hasMoreElements();) {
            String headerName = (String) e.nextElement();
            response.addHeader(headerName, fc.getInitParameter(headerName));
        }
        // pass the request/response on
        chain.doFilter(req, response);
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        this.fc = filterConfig;
    }

    public void destroy() {
        this.fc = null;
    }
}
