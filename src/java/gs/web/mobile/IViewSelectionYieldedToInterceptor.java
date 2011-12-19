package gs.web.mobile;


/**
 * Opts your controller in to <code>MobileViewResolverInterceptor</code> behavior
 *
 * Implement this class if you have an existing controller that you would like to configure in pages-servlet
 * with a desktop and mobile view.
 * If a controller implements this interface, the MobileViewResolverInterceptor will set a view name after the
 * controller returns ModelAndView. It will choose whichever configured viewName best matches the device the
 * request came from
 *
 * If your controller ever uses logic to choose which view to return at runtime, then it should not implement this
 * interface
 */
public interface IViewSelectionYieldedToInterceptor extends IControllerWithMobileSupport {
    String getViewName();
    String getMobileViewName();
}
