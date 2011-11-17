package gs.web.mobile;


import gs.web.request.RequestInfo;
import gs.web.request.Subdomain;
import org.springframework.mobile.device.site.SitePreference;
import org.springframework.mobile.device.site.SitePreferenceHandler;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MobileViewSwitcherInterceptor implements HandlerInterceptor {

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
	}

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        boolean controllerHasMobileView = handler instanceof IControllerWithMobileView;
        if (controllerHasMobileView) {
            MobileHelper.switchToMobileViewIfNeeded(request, modelAndView);
        }
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object o, Exception e) throws Exception {
    }

}

