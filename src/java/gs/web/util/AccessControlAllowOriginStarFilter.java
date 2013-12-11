package gs.web.util;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author aroy@greatschools.org
 */
public class AccessControlAllowOriginStarFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {}

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ((HttpServletResponse)response).setHeader("Access-Control-Allow-Origin", "*");
        chain.doFilter(request, response);
    }

    public void destroy() {}
}
