package gs.web.mobile;

import org.springframework.web.servlet.mvc.Controller;

/**
 * Any controller that can respond to a mobile request should implement this controller and configure the
 * properties in spring, so that the <code>MobileRedirectHandlerInterceptor</code> redirects properly.
 * The interceptor will assume that controllers which don't implement this interface are desktop-only
 */
public interface IControllerWithMobileSupport extends Controller {
    /** Whether or not the specific instance of this controller ever responds to mobile requests */
    public boolean beanSupportsMobileRequests();

    /** Whether or not the specific instance of this controller ever responds to desktop requests */
    public boolean beanSupportsDesktopRequests();

    public void setBeanSupportsMobileRequests(boolean beanSupportsMobileRequests);
    public void setBeanSupportsDesktopRequests(boolean beanSupportsDesktopRequests);
}
