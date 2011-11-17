package gs.web.mobile;


import gs.web.request.RequestInfo;
import gs.web.request.Subdomain;
import gs.web.util.RedirectView301;
import org.springframework.mobile.device.site.SitePreference;
import org.springframework.mobile.device.site.SitePreferenceHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

public class MobileHelper {
    public static final String STANDARD_MOBILE_VIEW_NAME_SUFFIX = "-mobile";

    public static void switchToMobileViewIfNeeded(HttpServletRequest request, ModelAndView mAndV) {
        RequestInfo requestInfo = (RequestInfo) request.getAttribute(RequestInfo.REQUEST_ATTRIBUTE_NAME);

        if (requestInfo.shouldRenderMobileView()) {
            mAndV.setViewName(mAndV.getViewName() + STANDARD_MOBILE_VIEW_NAME_SUFFIX);
        }
    }

    public static ModelAndView getRedirectToMobileSiteIfNeeded(HttpServletRequest request) {
        RequestInfo requestInfo = (RequestInfo) request.getAttribute(RequestInfo.REQUEST_ATTRIBUTE_NAME);
        ModelAndView mAndV = null;

        if (!requestInfo.isOnMobileSite() && requestInfo.isFromMobileDevice() && requestInfo.isMobileSiteEnabled()) {
            String newUrl = requestInfo.getFullUrlAtNewSubdomain(Subdomain.MOBILE);
            mAndV = new ModelAndView(new RedirectView301(newUrl));
        }

        return mAndV;
    }

    public static ModelAndView getRedirectToDesktopSiteIfNeeded(HttpServletRequest request) {
        RequestInfo requestInfo = (RequestInfo) request.getAttribute(RequestInfo.REQUEST_ATTRIBUTE_NAME);
        ModelAndView mAndV = null;

        if (requestInfo.isOnMobileSite() && !requestInfo.isFromMobileDevice() && requestInfo.isMobileSiteEnabled()) {
            String newUrl = requestInfo.getFullUrlAtNewSubdomain(Subdomain.WWW);
            mAndV = new ModelAndView(new RedirectView301(newUrl));
        }

        return mAndV;
    }

    public static ModelAndView getSiteRedirectIfNeeded(HttpServletRequest request) {
        ModelAndView modelAndView = getRedirectToMobileSiteIfNeeded(request);
        if (modelAndView == null) {
            modelAndView = getRedirectToDesktopSiteIfNeeded(request);
        }
        return modelAndView;
    }

}
