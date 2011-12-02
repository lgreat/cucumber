package gs.web.mobile;


import gs.web.request.RequestInfo;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.mobile.device.Device;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Usage: Create a new spring bean that uses this factory as a spring "factory-bean" and "getDeviceSpecificController"
 * as the factory-method.
 *
 * Iterates over given controllers and simply chooses the one which implements the correct interface
 */
public class DeviceSpecificControllerFactory implements IDeviceSpecificControllerFactory {
    private static Logger _log = Logger.getLogger(DeviceSpecificControllerFactory.class);

    @Autowired
    private RequestInfo _requestInfo;

    public DeviceSpecificControllerFactory(){}

    public DeviceSpecificControllerFactory(List<Controller> controllers) {
    }

    public Controller getDeviceSpecificController(List<Controller> controllers) {
        if (_requestInfo == null) {
            throw new IllegalStateException("requestInfo was null.");
        }

        Controller chosenController = null;

        for (Controller controller : controllers) {
            // option 1: Controller implements IDeviceSpecificController and defines custom logic that looks at
            // RequestInfo and determines if it can handle the request
            if (controller instanceof IDeviceSpecificController) {
                if (((IDeviceSpecificController)controller).shouldHandleRequest(_requestInfo)) {
                    chosenController = controller;
                }
            } else {
                // option 2 (preferred): Controller implements either IControllerWithMobileView or IMobileOnlyController
                // OR chooses not to implement any interface (default). Controller is chosen automatically based on
                // whether or not a mobile-capable controller is needed.
                if (_requestInfo.shouldRenderMobileView()) {
                    if (controller instanceof IControllerWithMobileView) {
                        chosenController = controller;
                    }
                } else {
                    if (!(controller instanceof IControllerWithMobileView)) {
                        chosenController = controller;
                    }
                }
            }
        }

        if (chosenController == null) {
            String msg = "No valid controller configured for request needing ";
            if (_requestInfo.shouldRenderMobileView()) {
                msg += "mobile controller";
            } else {
                msg += "desktop-only controller";
            }
            throw new IllegalStateException(msg);
        }

        return chosenController;
    }

}

