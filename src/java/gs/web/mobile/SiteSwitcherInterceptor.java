package gs.web.mobile;


import gs.web.request.RequestInfo;
import gs.web.request.Subdomain;
import org.springframework.mobile.device.site.SitePreference;
import org.springframework.mobile.device.site.SitePreferenceHandler;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SiteSwitcherInterceptor implements HandlerInterceptor {

    public static final String sitePreferenceUrlForAlternateSite = "sitePreferenceUrlForAlternateSite";

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        RequestInfo requestInfo = (RequestInfo) request.getAttribute(RequestInfo.REQUEST_ATTRIBUTE_NAME);
        boolean controllerHasMobileView = handler instanceof IControllerWithMobileView;
        boolean mobileOnlyController = handler instanceof IMobileOnlyController;
        boolean controllerHasDesktopView = !mobileOnlyController; //readability
        boolean desktopOnlyController = !controllerHasMobileView; //readability

        /*
        If request is on the mobile version of the site, and there's a desktop version of the current page (controller
        if not a mobile-only controller) AND (the user is either on a desktop OR prefers the desktop version) AND doesn't prefer the mobile version,
        then redirect to desktop version of site (www).
        Also redirect if mobile site is disabled or controller doesn't have a mobile view
         */
        if (
            requestInfo.isOnMobileSite()
            && (
                ( controllerHasDesktopView && (!requestInfo.isFromMobileDevice() || requestInfo.getSitePreference() == SitePreference.NORMAL) && requestInfo.getSitePreference() != SitePreference.MOBILE)
                || desktopOnlyController
                || !requestInfo.isMobileSiteEnabled()
            )
        ) {
            String newUrl = requestInfo.getFullUrlAtNewSubdomain(Subdomain.WWW);
            response.sendRedirect(newUrl);
            return false;
        } else if (
                !requestInfo.isOnMobileSite()
                && (
                    (controllerHasMobileView && (requestInfo.isFromMobileDevice() || requestInfo.getSitePreference() == SitePreference.MOBILE ) && requestInfo.getSitePreference() != SitePreference.NORMAL)
                    && requestInfo.isMobileSiteEnabled()
                )
        ) {
            String newUrl = requestInfo.getFullUrlAtNewSubdomain(Subdomain.MOBILE);
            response.sendRedirect(newUrl);
            return false;
        }

		return true;
	}

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object o, ModelAndView modelAndView) throws Exception {
        RequestInfo requestInfo = (RequestInfo) request.getAttribute(RequestInfo.REQUEST_ATTRIBUTE_NAME);

        modelAndView.getModel().put(sitePreferenceUrlForAlternateSite, requestInfo.getSitePreferenceUrlForAlternateSite());

    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object o, Exception e) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}

