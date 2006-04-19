package gs.web.util;

import gs.data.dao.hibernate.ThreadLocalTransactionManager;

import javax.servlet.*;
import java.io.IOException;

/**
 * Cleans up database connections and hibernate sessions after the view has rendered
 */
public class OpenSessionInViewFilter implements Filter {

    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } finally {
            ThreadLocalTransactionManager.commitOrRollback();
        }
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void destroy() {
    }
}
