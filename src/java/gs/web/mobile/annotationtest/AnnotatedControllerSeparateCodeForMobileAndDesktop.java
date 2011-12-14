package gs.web.mobile.annotationtest;

/*
@Controller
@RequestMapping(value = "/mobile/mobiletest.page")
*/
/* public class AnnotatedControllerSeparateCodeForMobileAndDesktop implements IDeviceSpecificController { */
public class AnnotatedControllerSeparateCodeForMobileAndDesktop {
    /*
    private boolean _beanSupportsMobileRequests;
    private boolean _beanSupportsDesktopRequests;

    public String handleDevice(RequestInfo requestInfo, HttpServletRequest request, ModelMap model) {
        if (requestInfo.shouldRenderMobileView()) {
            handle(request, model);
        } else {
            handle2(request, model);
        }
    }

    @RequestMapping(params="!isFromMobileDevice=true")
    public String handleDesktop(HttpServletRequest request, ModelMap model) {
        return "mobile/annotatedControllerPartOfPairOne";
    }

    @RequestMapping(params="isFromMobileDevice=true")
    public String handleMobile(HttpServletRequest request, ModelMap model) {
        return "mobile/annotatedControllerPartOfPairTwo";
    }

    public boolean beanSupportsMobileRequests() {
        return _beanSupportsMobileRequests;
    }

    public void setBeanSupportsMobileRequests(boolean beanSupportsMobileRequests) {
        _beanSupportsMobileRequests = beanSupportsMobileRequests;
    }

    public boolean beanSupportsDesktopRequests() {
        return _beanSupportsDesktopRequests;
    }

    public void setBeanSupportsDesktopRequests(boolean beanSupportsDesktopRequests) {
        _beanSupportsDesktopRequests = beanSupportsDesktopRequests;
    }

*/
}
