package gs.web.util;

import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interceptor to set http response headers
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class CookieInterceptor implements HandlerInterceptor {
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
        //do nothing
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
        // Set the a/b version - 'a' is the default
        String versionParam = request.getParameter("version");
        if (StringUtils.isNotBlank(versionParam)) {
            // version override takes precedence
            sessionContext.setAbVersion(versionParam.trim());
        } else if (isKnownCrawler(request)) {
            // GS-4614 Ensure crawlers always see the A version in multivariant tests
            sessionContext.setAbVersion("a");
        } else {
            long trnoSecondsSinceEpoch = 0;
            String trnoValue = trno.getValue();
            try {
                // Extract the time from the TRNO (180654739.127.0.0.1 => 180654739)
                trnoSecondsSinceEpoch = Long.valueOf(trnoValue.substring(0, trnoValue.indexOf(".")));
            } catch (Exception e) {
                // do nothing -- defaults to 0
            }
            // Use the time from when TRNO was set to determine what variant the user should get
            VariantConfiguration.determineVariantFromConfiguration(trnoSecondsSinceEpoch, sessionContext);
        }
    }

    protected boolean isKnownCrawler(HttpServletRequest request) {
        return SessionContextUtil.isKnownCrawler(request.getHeader("User-Agent"));
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
}
