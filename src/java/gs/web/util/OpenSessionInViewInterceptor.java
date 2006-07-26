package gs.web.util;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletRequest;

import gs.web.util.context.SessionContextInterceptor;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;

import java.util.Date;

/**
 *  @author <a href="mailto:thuss@greatschools.net">Todd Huss</a>
 */
public class OpenSessionInViewInterceptor implements HandlerInterceptor {

    private static final Log _log = LogFactory.getLog(SessionContextInterceptor.class);

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

    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object controller) throws Exception {
        // Unless it's a read-write controller, default to the read-only databases
        if (!(controller instanceof ReadWriteController)) {
            ThreadLocalTransactionManager.setReadOnly();
        }
        return true;
    }

    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object controller, ModelAndView modelAndView) throws Exception {
        // Do nothing
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object controller, Exception e) throws Exception {
        if (e != null) {
            ThreadLocalTransactionManager.setRollbackOnly();
            logException(e, request);
        }
        ThreadLocalTransactionManager.commitOrRollback();
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
}
