package gs.web.util;

import gs.data.admin.IPropertyDao;
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
 * @author <a href="mailto:aroy@greatschools.net">Anthony Roy</a>
 */
public class CookieInterceptor extends CookieUtil implements HandlerInterceptor {
    public static final int EXPIRE_AT_END_OF_SESSION = -1;
    public static final int EXPIRE_NOW = 0;

    private IPropertyDao _propertyDao;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        SessionContext sessionContext = (SessionContext) request.getAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME);

        // We don't set cookies for cacheable pages
        if (!(o instanceof CacheablePageController)) {
            Cookie trackingNumber = buildTrackingNumberCookie(request, response);
            buildCobrandCookie(request, sessionContext, response);
            //buildCobrandTypeCookie(request, sessionContext, response);
            determineAbVersion(trackingNumber, request, sessionContext);
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
        Cookie cobrandCookie = getCookie(request, SessionContextUtil.COBRAND_COOKIE);
        String hostName = sessionContext.getHostName();
        if (cobrandCookie == null || !hostName.equals(cobrandCookie.getValue())) {
            cobrandCookie = new Cookie(SessionContextUtil.COBRAND_COOKIE, hostName);
            cobrandCookie.setPath("/");
            cobrandCookie.setDomain(".greatschools.net");
            response.addCookie(cobrandCookie);
        }
    }

    protected void buildCobrandTypeCookie(HttpServletRequest request, SessionContext sessionContext, HttpServletResponse response) {
        PageHelper pageHelper = new PageHelper(sessionContext, request);
        Cookie cobrandTypeCookie = getCookie(request, SessionContextUtil.COBRAND_TYPE_COOKIE);
        StringBuilder value = new StringBuilder();
        value.append(sessionContext.isFramed() ? "framed" : "standard");
        value.append(",");
        if (pageHelper.isAdFree()) {
            value.append("adfree");
        } else if (pageHelper.isAdServedByCobrand()) {
            value.append("adscustom");
        } else {
            value.append("adsgs");
        }

        if (cobrandTypeCookie == null || !value.toString().equals(cobrandTypeCookie.getValue())) {
            cobrandTypeCookie = new Cookie(SessionContextUtil.COBRAND_TYPE_COOKIE, value.toString());
            cobrandTypeCookie.setPath("/");
            cobrandTypeCookie.setDomain(".greatschools.net");
            response.addCookie(cobrandTypeCookie);
        }
    }

    protected Cookie buildTrackingNumberCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = getCookie(request, SessionContextUtil.TRACKING_NUMBER);

        if (cookie == null) {
            String cookieValue = String.valueOf(System.currentTimeMillis() / 1000);
            cookie = new Cookie(SessionContextUtil.TRACKING_NUMBER, cookieValue);
            cookie.setPath("/");
            cookie.setMaxAge(-1);
            if (!UrlUtil.isDeveloperWorkstation(request.getServerName())) {
                // don't set domain for developer workstations so they can still access the cookie!!
                cookie.setDomain(".greatschools.net");
            }
            response.addCookie(cookie);
        }

        return cookie;
    }

    protected void determineAbVersion(Cookie trackingNumber, HttpServletRequest request, SessionContext sessionContext) {
        // Set the a/b version - 'a' is the default
        String versionParam = request.getParameter("version");
        if (StringUtils.isNotBlank(versionParam)) {
            // version override takes precedence
            sessionContext.setAbVersion(versionParam.trim());
        } else if (isKnownCrawler(request)) {
            // GS-4614 Ensure crawlers always see the A version in multivariant tests
            sessionContext.setAbVersion("a");
        } else {
            long secondsSinceEpoch = 0;
            String cookieValue = trackingNumber.getValue();
            try {
                // Extract the time from the tracking number cookie (e.g. 180654739)
                secondsSinceEpoch = Long.valueOf(cookieValue);
            } catch (Exception e) {
                // do nothing -- defaults to 0
            }
            // Use the time from when tracking number was set to determine what variant the user should get
            sessionContext.setAbVersion(VariantConfiguration.getVariant(secondsSinceEpoch, getPropertyDao()));
        }
    }

    protected boolean isKnownCrawler(HttpServletRequest request) {
        return SessionContextUtil.isKnownCrawler(request.getHeader("User-Agent"));
    }

    public IPropertyDao getPropertyDao() {
        return _propertyDao;
    }

    public void setPropertyDao(IPropertyDao propertyDao) {
        _propertyDao = propertyDao;
    }
}
