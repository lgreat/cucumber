package gs.web.util;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.web.util.context.SessionContext;

/**
 * Interceptor to set http response headers
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class ResponseInterceptor implements HandlerInterceptor {

    /**
     * Cookie name used to track repeat usage and other stats
     */
    public static final String TRNO_COOKIE = "TRNO";
    public static final String COBRAND_COOKIE = "COBRAND";

    public static final String HEADER_CACHE_CONTROL = "Cache-Control";

    public static final String HEADER_PRAGMA = "Pragma";

    public static final String HEADER_EXPIRES = "Expires";

    private static final Log _log = LogFactory.getLog(ResponseInterceptor.class);
    public static final int EXPIRE_AT_END_OF_SESSION = -1;
    public static final int EXPIRE_NOW = 0;


    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        
        /*
         * Don't create a cookie for this URL.
         * This is somewhat poorly done since it's not configurable. I'd prefer some
         * way to turn off certain interceptors for certain URLs, but I don't see these
         * the way things are set up.
         */
        if (request.getRequestURI().indexOf("/content/box/v1") == -1) {
            setTrnoCookie(request, response);
        }

        SessionContext sessionContext = (SessionContext) request.getAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME);
        if (sessionContext.isCobranded() && !sessionContext.isFramed()) {
            Cookie cobrandCookie = new Cookie(COBRAND_COOKIE, sessionContext.getHostName());
            cobrandCookie.setPath("/");
            cobrandCookie.setDomain("greatschools.net");
            response.addCookie(cobrandCookie);
        }
        else {
            Cookie cookies[] = request.getCookies();
            if (cookies != null) {
                for (int i = 0; i < cookies.length; i++) {
                    Cookie cookie = cookies[i];
                    if (COBRAND_COOKIE.equals(cookie.getName())) {
                        cookie.setValue("");
                        cookie.setMaxAge(0);
                        response.addCookie(cookie);
                    }
                }
            }
        }

        return true;
    }


    private void setTrnoCookie(HttpServletRequest request, HttpServletResponse response) {
        boolean hasTrnoCookie = false;
        Cookie cookies [] = request.getCookies();

        if (cookies != null) {

            for (int i = 0; i < cookies.length; i++) {
                Cookie cookie = cookies[i];
                
                if (TRNO_COOKIE.equals(cookie.getName())) {
                    hasTrnoCookie = true;
                    break;
                }
            }
        }

        if (!hasTrnoCookie) {
            Long secondsSinceEpoch = new Long(System.currentTimeMillis() / 1000);
            String ipAddress = request.getHeader("x_forwarded_for");

            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }

            String cookieValue = String.valueOf(secondsSinceEpoch.intValue()) + "." + ipAddress;

            //cookie expires approx. two years from now
            Cookie c = new Cookie(TRNO_COOKIE, cookieValue);
            c.setPath("/");
            c.setMaxAge(63113852);
            response.addCookie(c);
        }
    }

    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse response, Object o, ModelAndView modelAndView) throws Exception {
        if (!response.containsHeader("Cache-Control")) {
            setNoCacheHeaders(response);
        }
    }

    private void setNoCacheHeaders(HttpServletResponse response) {
        /*
        * Set pages to not be cached since almost all pages include the member bar now or some
        * other dynamic content. This should be in the decorator as gsml:nocache but since that
        * tag doesn't work due to a sitemesh bug (see gsml:nocache tag for more info) it's here
        * for the time being.
        */
        response.setHeader(HEADER_CACHE_CONTROL, "no-cache");
        response.setHeader(HEADER_PRAGMA, "no-cache");
        response.setDateHeader(HEADER_EXPIRES, 0);
    }


    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        //do nothing
    }
}
