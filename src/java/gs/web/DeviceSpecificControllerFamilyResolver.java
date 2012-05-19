package gs.web;

import gs.web.mobile.IDeviceSpecificControllerPartOfPair;
import gs.web.request.RequestInfo;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class DeviceSpecificControllerFamilyResolver implements IControllerFamilyResolver {
    public ControllerFamily resolveControllerFamily() {
        ControllerFamily family = null;

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        if (request == null) {
            throw new IllegalStateException("Request cannot be null.");
        }
        RequestInfo requestInfo =  RequestInfo.getRequestInfo(request);
        if (requestInfo == null) {
            throw new IllegalStateException("requestInfo was null.");
        }

        IDeviceSpecificControllerPartOfPair chosenController = null;

        // Controller implements IDeviceSpecificControllerPartOfPair
        // OR chooses not to implement any interface (default). Controller is chosen automatically based on
        // whether or not a mobile-capable controller is needed.
        if (requestInfo.shouldRenderMobileView()) {
            family = ControllerFamily.MOBILE;
        }

        return family;
    };
}
