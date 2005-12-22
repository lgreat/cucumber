package gs.web;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;

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

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        /*
         * Set pages to not be cached since almost all pages include the member bar now or some
         * other dynamic content. This should be in the decorator as gsml:nocache but since that
         * tag doesn't work due to a sitemesh bug (see gsml:nocache tag for more info) it's here
         * for the time being.
         */
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

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

        return true;
    }

    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        //do nothing
    }

    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        //do nothing
    }
}
