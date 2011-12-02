package gs.web.mobile;


import gs.web.request.RequestInfo;
import gs.web.request.Subdomain;
import org.springframework.mobile.device.site.SitePreference;
import org.springframework.mobile.device.site.SitePreferenceHandler;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles cases where one controller will be used to serve multiple views depending on the Device the request came from
 */
public class MobileViewSwitcherInterceptor implements HandlerInterceptor {

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
	}

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null && modelAndView.getModel() != null) {
            boolean controllerHasMobileView = handler instanceof IControllerWithMobileView;
            boolean mobileOnlyController = handler instanceof IMobileOnlyController;
            boolean controllerHasDesktopView = !mobileOnlyController; //readability
            boolean desktopOnlyController = !controllerHasMobileView; //readability

            RequestInfo requestInfo = (RequestInfo) request.getAttribute(RequestInfo.REQUEST_ATTRIBUTE_NAME);

            if (controllerHasMobileView && controllerHasDesktopView) {
                String mobileViewName = ((IControllerWithMobileView) handler).getMobileViewName();
                if (requestInfo.shouldRenderMobileView()) {
                    if (mobileViewName == null) {
                        // only use the "-mobile" view name convention if the controller hasn't already set a mobile view
                        modelAndView.setViewName(modelAndView.getViewName() + MobileHelper.STANDARD_MOBILE_VIEW_NAME_SUFFIX);
                    } else {
                        modelAndView.setViewName(mobileViewName);
                    }
                }
            }

            //TODO: move map key name elsewhere
            if (requestInfo.isMobileSiteEnabled()) {
                // allows view to render link to alternate (mobile | desktop) version if desired
                modelAndView.getModel().put("hasMobileView", controllerHasMobileView);
            }
        }
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object o, Exception e) throws Exception {
    }

}

