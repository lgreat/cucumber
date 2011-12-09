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
 * Analyzes the request and the controller to determine if a redirect to the mobile version of the site (m.greatschools.org)
 * needs to occur, or vice versa.
 *
 * Two cases for redirecting:
 * 1. User tries to access a page on the "wrong" subdomain (i.e. access a mobile-only page on www)
 * 2. User tries to access a page from the "wrong" user-agent (i.e. accesses www on a mobile device without setting site preference cookie)
 */
public class MobileRedirectHandlerInterceptor implements HandlerInterceptor {

    public static final String sitePreferenceUrlForAlternateSite = "sitePreferenceUrlForAlternateSite";

    private RequestInfo _requestInfo;

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        RequestInfo requestInfo = _requestInfo;

        IDeviceSpecificController controller;
        boolean controllerSupportsMobile = false;
        boolean controllerSupportsMobileOnly = false;
        boolean controllerSupportsDesktop = true;
        boolean controllerSupportsDesktopOnly = true;

        if (handler instanceof IDeviceSpecificController) {
            controller = (IDeviceSpecificController) handler;
            controllerSupportsMobile = controller.beanSupportsMobileRequests();
            controllerSupportsMobileOnly = !controller.beanSupportsDesktopRequests();
            controllerSupportsDesktop = !controllerSupportsMobileOnly; //readability
            controllerSupportsDesktopOnly = !controllerSupportsMobile; //readability
        }

        if (requestInfo.isDeveloperWorkstation()) {
            return true;
        }

        Subdomain subdomainToRedirectTo = null;

        /*
        If request is on the mobile version of the site, and there's a desktop version of the current page (controller
        if not a mobile-only controller) AND (the user is either on a desktop OR prefers the desktop version) AND doesn't prefer the mobile version,
        then redirect to desktop version of site (www).
        Also redirect if mobile site is disabled or controller doesn't have a mobile view
         */
        if (
            requestInfo.isOnMobileSite()
            && (
                ( controllerSupportsDesktop && (!requestInfo.isFromMobileDevice() || requestInfo.getSitePreference() == SitePreference.NORMAL) && requestInfo.getSitePreference() != SitePreference.MOBILE)
                || controllerSupportsDesktopOnly
                || !requestInfo.isMobileSiteEnabled()
            )
        ) {
            subdomainToRedirectTo = Subdomain.WWW;
        } else if (
                !requestInfo.isOnMobileSite()
                && (
                    (controllerSupportsMobile && (requestInfo.isFromMobileDevice() || requestInfo.getSitePreference() == SitePreference.MOBILE ) && requestInfo.getSitePreference() != SitePreference.NORMAL)
                    && requestInfo.isMobileSiteEnabled()
                )
        ) {
            subdomainToRedirectTo = Subdomain.MOBILE;
        }

        if (subdomainToRedirectTo != null) {
            String newHostname = _requestInfo.getHostnameForTargetSubdomain(subdomainToRedirectTo);
            String newUrl = UrlUtil.getRequestURLAtNewHostname(request, newHostname);
            response.sendRedirect(newUrl);
            return false;
        }

		return true;
	}

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object o, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null && modelAndView.getModel() != null) {
            RequestInfo requestInfo = (RequestInfo) request.getAttribute(RequestInfo.REQUEST_ATTRIBUTE_NAME);

            if (requestInfo.isMobileSiteEnabled()) {
                modelAndView.getModel().put(sitePreferenceUrlForAlternateSite, requestInfo.getSitePreferenceUrlForAlternateSite());
            }
        }
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object o, Exception e) throws Exception {
    }

    public RequestInfo getRequestInfo() {
        return _requestInfo;
    }

    public void setRequestInfo(RequestInfo requestInfo) {
        _requestInfo = requestInfo;
    }
}

