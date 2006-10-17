package gs.web.util;

import gs.data.dao.hibernate.ThreadLocalTransactionManager;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Cleans up database connections and hibernate sessions after the view has rendered
 * Logs an error exception if there's a problem
 */
public class OpenSessionInViewFilter implements Filter {

    private static final Log _log = LogFactory.getLog(OpenSessionInViewFilter.class);

    private static String _hostname = "unable to resolve";

    static {
        // Set the hostname for logging purposes
        try {
            java.net.InetAddress localMachine =
                    java.net.InetAddress.getLocalHost();
            _hostname = localMachine.getHostName();
        } catch (java.net.UnknownHostException e) {
            // No need to do anything
        }
    }

    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (IOException e) {
            ThreadLocalTransactionManager.setRollbackOnly();
            logException(e, request);
            throw e;
        } catch (ServletException e) {
            ThreadLocalTransactionManager.setRollbackOnly();
            logException(e, request);
            throw e;
        } catch (RuntimeException e) {
            ThreadLocalTransactionManager.setRollbackOnly();
            logException(e, request);
            throw e;
        } finally {
            // Cleanup open database connections
            ThreadLocalTransactionManager.commitOrRollback();
        }
    }

private void logException(Exception e, ServletRequest r) {
        StringBuffer url = null;
        String userAgent = null;
        String remoteIp = null;
        String referrer = null;
        if (r instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) r;
            // Reconstruct the URL
            url = request.getRequestURL();
            if (request.getQueryString() != null) url.append("?").append(request.getQueryString());
            userAgent = request.getHeader("User-Agent");
            remoteIp = request.getRemoteAddr();
            referrer = request.getHeader("Referer");
        }

        _log.error(new Date().toString() + " WEBSERVER: " + _hostname + " REQUEST: " + url + " REFERRER: " + referrer +
                " REMOTEIP: " + remoteIp + " USER-AGENT: " + userAgent, e);
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void destroy() {
    }
}
