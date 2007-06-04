package gs.web.util;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;

/**
 * Interceptor to set http response headers
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class ResponseInterceptor implements HandlerInterceptor {

    public static final String HEADER_CACHE_CONTROL = "Cache-Control";

    public static final String HEADER_PRAGMA = "Pragma";

    public static final String HEADER_EXPIRES = "Expires";

    public static final int EXPIRE_AT_END_OF_SESSION = -1;
    public static final int EXPIRE_NOW = 0;


    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        SessionContext sessionContext = (SessionContext) request.getAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME);

        // TRNO Cookie
        long trnoSecondsSinceEpoch = 0;
        Cookie trno = findCookie(request, SessionContextUtil.TRNO_COOKIE);
        if (trno == null) {
            trnoSecondsSinceEpoch = System.currentTimeMillis() / 1000;
            String ipAddress = request.getHeader("x_forwarded_for");

            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }

            String cookieValue = String.valueOf(trnoSecondsSinceEpoch) + "." + ipAddress;

            //cookie expires approx. two years from now
            Cookie c = new Cookie(SessionContextUtil.TRNO_COOKIE, cookieValue);
            c.setPath("/");
            c.setMaxAge(63113852);
            response.addCookie(c);
        } else {
            String trnoValue = trno.getValue();
            try {
                // Extract the time from the TRNO (180654739.127.0.0.1 => 180654739)  
                trnoSecondsSinceEpoch = Long.valueOf(trnoValue.substring(0, trnoValue.indexOf(".")));
            } catch (Exception e) {
                // do nothing
            }
        }

        // Use the time from when TRNO was set to determine if user is A or B variant
        if (trnoSecondsSinceEpoch % 2 == 0) {
            sessionContext.setAbVersion("b");
        }

        // COBRAND cookie
        Cookie cobrandCookie = findCookie(request, SessionContextUtil.COBRAND_COOKIE);
        if (sessionContext.isCobranded() && !sessionContext.isFramed()) {
            String hostName = sessionContext.getHostName();
            if (cobrandCookie == null || !hostName.equals(cobrandCookie.getValue())) {
                cobrandCookie = new Cookie(SessionContextUtil.COBRAND_COOKIE, hostName);
                cobrandCookie.setPath("/");
                cobrandCookie.setDomain(".greatschools.net");
                response.addCookie(cobrandCookie);
            }
        } else {
            if (cobrandCookie != null) {
                cobrandCookie.setValue("");
                cobrandCookie.setMaxAge(EXPIRE_NOW);
                cobrandCookie.setPath("/");
                cobrandCookie.setDomain(".greatschools.net");
                response.addCookie(cobrandCookie);
            }
        }

        return true;
    }


    private Cookie findCookie(HttpServletRequest request, String cookieName) {
        Cookie cookies[] = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
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
