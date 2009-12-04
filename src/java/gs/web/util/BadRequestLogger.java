package gs.web.util;

import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;

/**
 * Utility methods for logging
 * @author Young Fan
 */
public class BadRequestLogger {
    public static boolean isGSReferrer(HttpServletRequest request) {
        String referrer = request.getHeader("Referer");
        if (referrer != null) {
            return referrer.toLowerCase().contains("greatschools.org");
        }
        return false;
    }

    public static boolean isExternalReferrer(HttpServletRequest request) {
        return !isGSReferrer(request);
    }

    public static String getReferrer(HttpServletRequest request) {
        return request.getHeader("Referer");
    }

    public static String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    /**
     * Log a message appended with request URL, referrer, and user-agent
     * If there is a referrer which contains the string "greatschools.org", log an error; otherwise, log a warning.
     * @param log
     * @param request
     * @param message
     */
    public static void logBadRequest(Logger log, HttpServletRequest request, String message) {
        logBadRequest(log, request, message, null, true, true, true);
    }

    /**
     * Log a message appended with request URL, referrer, and user-agent and passing the Throwable to the Logger
     * If there is a referrer which contains the string "greatschools.org", log an error; otherwise, log a warning.
     * @param log
     * @param request
     * @param message
     * @param e
     */
    public static void logBadRequest(Logger log, HttpServletRequest request, String message, Throwable e) {
        logBadRequest(log, request, message, e, true, true, true);
    }

    /**
     * Log a message using the specified logger, optionally logging the request URL, referrer, and user-agent.
     * If there is a referrer which contains the string "greatschools.org", log an error; otherwise, log a warning.
     *
     * Example output:
     *
     * WARN  gs.web.school.SchoolsController  - City not found in state in city/district browse request.
     * [Request URL: http://localhost:8080/north-carolina/camp-lejuene/middle-schools/?s_cid=1234]
     * [Referrer: null]
     * [User-Agent: Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.5; en-US; rv:1.9.0.1) Gecko/2008070206 Firefox/3.0.1]
     * java.lang.NullPointerException
     *     at gs.web.school.SchoolsController.handleRequestInternal(SchoolsController.java:361)
     *     at org.springframework.web.servlet.mvc.AbstractController.handleRequest(AbstractController.java:153)
     *     at org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter.handle(SimpleControllerHandlerAdapter.java:48)
     *
     * @param log
     * @param request
     * @param message
     * @param e
     * @param logRequestURL
     * @param logReferrer
     * @param logUserAgent
     */
    public static void logBadRequest(Logger log, HttpServletRequest request, String message, Throwable e,
                           boolean logRequestURL, boolean logReferrer, boolean logUserAgent) {
        if (message == null) {
            message = "";
        }
        StringBuilder s = new StringBuilder(message);

        if (logRequestURL) {
            s.append(" [Request URL: ");
            s.append(request.getRequestURL());
            if (request.getQueryString() != null) {
                s.append("?");
                s.append(request.getQueryString());
            }
            s.append("]");
        }
        if (logReferrer) {
            s.append(" [Referrer: ");
            s.append(getReferrer(request));
            s.append("]");
        }
        if (logUserAgent) {
            s.append(" [User-Agent: ");
            s.append(getUserAgent(request));
            s.append("]");
        }

        if (isGSReferrer(request)) {
            if (e != null) {
                log.error(s.toString(), e);
            } else {
                log.error(s.toString());
            }
        } else {
            if (e != null) {
                log.warn(s.toString(), e);
            } else {
                log.warn(s.toString());
            }
        }
    }
}
