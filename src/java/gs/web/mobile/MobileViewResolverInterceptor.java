package gs.web.mobile;


import gs.web.request.RequestInfo;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles cases where one controller will be used to serve multiple views depending on the Device the request came from
 * Controllers which implement <code>IControllerWithMobileAndDesktopViews</code> will have a view name set after the
 * controller returns a ModelAndView.
 *
 * This interceptor will choose whichever configured viewName best matches the device the request came from
 */
public class MobileViewResolverInterceptor implements HandlerInterceptor {

    public static final String HAS_MOBILE_VIEW_MODEL_KEY = "hasMobileView";
    public static final String MOBILE_VIEW_NAME_SUFFIX = "-mobile";

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
	}

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        RequestInfo requestInfo = RequestInfo.getRequestInfo(request);
        if (requestInfo == null) {
            throw new IllegalStateException("RequestInfo cannot be null");
        }

        if (modelAndView != null && modelAndView.getModel() != null) {

            IControllerWithMobileSupport controller;
            boolean controllerSupportsMobile = false;
            boolean controllerSupportsMobileOnly = false;
            boolean controllerSupportsDesktop = true;
            boolean controllerSupportsDesktopOnly = true;

            if (handler instanceof IViewSelectionYieldedToInterceptor) {
                controller = (IControllerWithMobileSupport) handler;
                controllerSupportsMobile = controller.beanSupportsMobileRequests();
                controllerSupportsMobileOnly = !controller.beanSupportsDesktopRequests();
                controllerSupportsDesktop = !controllerSupportsMobileOnly; //readability
                controllerSupportsDesktopOnly = !controllerSupportsMobile; //readability

                if (controllerSupportsMobile && controllerSupportsDesktop) {
                    // get the name of the mobile view that was configured for the controller
                    String mobileViewName = ((IViewSelectionYieldedToInterceptor) handler).getMobileViewName();
                    
                    // get the name of the default view that was configured for the controller
                    String viewName =  ((IViewSelectionYieldedToInterceptor) handler).getViewName();

                    if (requestInfo.shouldRenderMobileView()) {
                        if (mobileViewName == null) {
                            // if no mobile name was configured, use the default naming convention
                            modelAndView.setViewName(modelAndView.getViewName() + MOBILE_VIEW_NAME_SUFFIX);
                        } else {
                            // a mobile view name was configured, and we should serve the mobile view
                            modelAndView.setViewName(mobileViewName);
                        }
                    } else {
                        if (viewName != null) {
                            // they configured a default view name, so let's set it on the ModelAndView
                            modelAndView.setViewName(viewName);
                        } // otherwise we'll assume the modelAndView already has the correct view name set
                    }
                }
            }

            if (requestInfo.isMobileSiteEnabled()) {
                // allows view to render link to alternate (mobile | desktop) version if desired
                if (modelAndView.getView() == null || !(modelAndView.getView() instanceof RedirectView)) {
                    modelAndView.getModel().put(HAS_MOBILE_VIEW_MODEL_KEY, controllerSupportsMobile || handler instanceof IDeviceSpecificControllerPartOfPair);
                }
            }
        }
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object o, Exception e) throws Exception {
    }

}
