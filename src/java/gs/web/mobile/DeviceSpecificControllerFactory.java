package gs.web.mobile;


import gs.web.request.RequestInfo;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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

    public DeviceSpecificControllerFactory(){}

    public DeviceSpecificControllerFactory(List<IDeviceSpecificControllerPartOfPair> controllers) {
    }

    public IDeviceSpecificControllerPartOfPair getDeviceSpecificController(List<IDeviceSpecificControllerPartOfPair> controllers) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        if (request == null) {
            throw new IllegalStateException("Request cannot be null.");
        }
        RequestInfo requestInfo =  RequestInfo.getRequestInfo(request);
        if (requestInfo == null) {
            throw new IllegalStateException("requestInfo was null.");
        }

        IDeviceSpecificControllerPartOfPair chosenController = null;

        for (IDeviceSpecificControllerPartOfPair controller : controllers) {
            boolean controllerSupportsMobile = false;
            boolean controllerSupportsMobileOnly = false;
            boolean controllerSupportsDesktop = true;
            boolean controllerSupportsDesktopOnly = true;

            if (controller instanceof IDeviceSpecificControllerPartOfPair) {
                IDeviceSpecificControllerPartOfPair deviceController = (IDeviceSpecificControllerPartOfPair) controller;
                controllerSupportsMobile = deviceController.controllerHandlesMobileRequests();
                controllerSupportsMobileOnly = !deviceController.controllerHandlesDesktopRequests();
                controllerSupportsDesktop = !controllerSupportsMobileOnly; //readability
                controllerSupportsDesktopOnly = !controllerSupportsMobile; //readability
            }

            // Controller implements IDeviceSpecificControllerPartOfPair
            // OR chooses not to implement any interface (default). Controller is chosen automatically based on
            // whether or not a mobile-capable controller is needed.
            if (requestInfo.shouldRenderMobileView()) {
                if (controllerSupportsMobile) {
                    chosenController = controller;
                }
            } else {
                if (controllerSupportsDesktopOnly) {
                    chosenController = controller;
                }
            }
        }

        if (chosenController == null) {
            String msg = "No valid controller configured for request needing ";
            if (requestInfo.shouldRenderMobileView()) {
                msg += "mobile controller";
            } else {
                msg += "desktop-only controller";
            }
            throw new IllegalStateException(msg);
        }

        return chosenController;
    }

}
