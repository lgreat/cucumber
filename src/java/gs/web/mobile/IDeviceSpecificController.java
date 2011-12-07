package gs.web.mobile;

import org.springframework.web.servlet.mvc.Controller;

public interface IDeviceSpecificController extends Controller {
    public boolean beanSupportsMobileRequests();
    public boolean beanSupportsDesktopRequests();
    public void setBeanSupportsMobileRequests(boolean supportsMobileRequests);
    public void setBeanSupportsDesktopRequests(boolean supportsDesktopRequests);
}
