package gs.web.util;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;

import java.util.Date;

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

        // We don't set cookies for cacheable pages
        if (!(o instanceof CacheablePageController)) {
            Cookie trno = buildTrnoCookie(request, response);
            buildCobrandCookie(request, sessionContext, response);
            determineAbVersion(trno, request, sessionContext);
        }

        return true;
    }

    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse response, Object o, ModelAndView modelAndView) throws Exception {
        if (!response.containsHeader("Cache-Control")) {
            if (o instanceof CacheablePageController) {
                setCacheHeaders(response);
            } else {
                setNoCacheHeaders(response);
            }
        }
    }

    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        //do nothing
    }


    protected void buildCobrandCookie(HttpServletRequest request, SessionContext sessionContext, HttpServletResponse response) {
        Cookie cobrandCookie = findCookie(request, SessionContextUtil.COBRAND_COOKIE);
        String hostName = sessionContext.getHostName();
        if (cobrandCookie == null || !hostName.equals(cobrandCookie.getValue())) {
            cobrandCookie = new Cookie(SessionContextUtil.COBRAND_COOKIE, hostName);
            cobrandCookie.setPath("/");
            cobrandCookie.setDomain(".greatschools.net");
            response.addCookie(cobrandCookie);
        }
    }

    protected Cookie buildTrnoCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie trno = findCookie(request, SessionContextUtil.TRNO_COOKIE);

        if (trno == null) {
            String ipAddress = request.getHeader("x_forwarded_for");

            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }

            String cookieValue = String.valueOf(System.currentTimeMillis() / 1000) + "." + ipAddress;

            //cookie expires approx. two years from now
            trno = new Cookie(SessionContextUtil.TRNO_COOKIE, cookieValue);
            trno.setPath("/");
            trno.setMaxAge(63113852);
            response.addCookie(trno);
        }
        return trno;
    }

    protected void determineAbVersion(Cookie trno, HttpServletRequest request, SessionContext sessionContext) {
        long trnoSecondsSinceEpoch = 0;
        String trnoValue = trno.getValue();
        try {
            // Extract the time from the TRNO (180654739.127.0.0.1 => 180654739)
            trnoSecondsSinceEpoch = Long.valueOf(trnoValue.substring(0, trnoValue.indexOf(".")));
        } catch (Exception e) {
            // do nothing
        }

        // Set the a/b version - 'a' is the default
        String versionParam = request.getParameter("version");
        if (StringUtils.isNotBlank(versionParam)) {
            sessionContext.setAbVersion(versionParam.trim());
        } else {
            // Use the time from when TRNO was set to determine if user is A or B variant
            if (trnoSecondsSinceEpoch % 2 == 0) {
                sessionContext.setAbVersion("b");
            } else {
                sessionContext.setAbVersion("a");
            }
        }
    }

    protected Cookie findCookie(HttpServletRequest request, String cookieName) {
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

    protected void setNoCacheHeaders(HttpServletResponse response) {
        response.setHeader(HEADER_CACHE_CONTROL, "no-cache");
        response.setHeader(HEADER_PRAGMA, "no-cache");
        response.setDateHeader(HEADER_EXPIRES, 0);
    }


    protected void setCacheHeaders(HttpServletResponse response) {
        response.setHeader(HEADER_CACHE_CONTROL, "public; max-age: 600");
        response.setHeader(HEADER_PRAGMA, "");
        Date date = new Date();
        response.setDateHeader(HEADER_EXPIRES, date.getTime() + 600000);
    }
}
