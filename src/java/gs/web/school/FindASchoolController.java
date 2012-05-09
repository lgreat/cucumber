package gs.web.school;

import gs.web.mobile.IDeviceSpecificControllerPartOfPair;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

/**
 * @author aroy@greatschools.org
 */
public class FindASchoolController extends ParameterizableViewController implements IDeviceSpecificControllerPartOfPair {

    public boolean controllerHandlesMobileRequests() {
        return false;
    }

    public boolean controllerHandlesDesktopRequests() {
        return true;
    }

    public void setControllerHandlesMobileRequests(boolean handlesMobileRequests) {}

    public void setControllerHandlesDesktopRequests(boolean handlesDesktopRequests) {}
}
