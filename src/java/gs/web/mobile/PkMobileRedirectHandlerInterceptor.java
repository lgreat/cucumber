package gs.web.mobile;

import gs.web.request.RequestInfo;
import gs.web.request.Subdomain;
import gs.web.util.UrlUtil;
import org.springframework.mobile.device.site.SitePreference;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Redirects from any pk.greatschools.org or pk.servername.greatschools.org page to  non-pk equivalent if
 * a mobile page would be served
 */
public class PkMobileRedirectHandlerInterceptor implements HandlerInterceptor {

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        RequestInfo requestInfo = RequestInfo.getRequestInfo(request);
        if (requestInfo == null) {
            throw new IllegalStateException("RequestInfo cannot be null");
        }

        Subdomain subdomainToRedirectTo = null;

        if (requestInfo.isOnPkSubdomain() && requestInfo.shouldRenderMobileView()) {
            subdomainToRedirectTo = Subdomain.WWW;
        }

        if (subdomainToRedirectTo != null) {
            String newHostname = requestInfo.getHostnameForTargetSubdomain(subdomainToRedirectTo);
            String newUrl = UrlUtil.getRequestURLAtNewHostname(request, newHostname);
            response.sendRedirect(newUrl);
            return false;
        }

		return true;
	}

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object o, ModelAndView modelAndView) throws Exception {
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object o, Exception e) throws Exception {
    }

}