package gs.web.mobile;


import gs.web.request.RequestInfo;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

public class MobileHelper {
    public static final String STANDARD_MOBILE_VIEW_NAME_SUFFIX = "-mobile";

    public static final String SITE_PREFERENCE_KEY_NAME = "site_preference"; // determined by spring mobile

    /**
     * Currently, this works by naming convention. By default the controller must be configured to render the normal
     * desktop view. If needed, the view name will be updated so that the mobile "version" of the view is rendered
     * instead. Easy but not ideal...
     *
     * @param request
     * @param mAndV
     */
    public static void switchToMobileViewIfNeeded(HttpServletRequest request, ModelAndView mAndV) {
        RequestInfo requestInfo = (RequestInfo) request.getAttribute(RequestInfo.REQUEST_ATTRIBUTE_NAME);

        if (requestInfo.shouldRenderMobileView()) {
            mAndV.setViewName(mAndV.getViewName() + STANDARD_MOBILE_VIEW_NAME_SUFFIX);
        }
    }

}
