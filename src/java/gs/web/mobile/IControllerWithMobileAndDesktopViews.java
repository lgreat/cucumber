package gs.web.mobile;


/**
 * Implement this class if you have an existing controller that you would like to configure in pages-servlet
 * with a desktop and mobile view.
 * If a controller implements this controller, the MobileViewResolverInterceptor will set a view name after the
 * controller returns ModelAndView. It will choose whichever configured viewName best matches the device the
 * request came from
 */
public interface IControllerWithMobileAndDesktopViews extends IControllerWithMobileSupport {
    String getViewName();
    String getMobileViewName();
}
