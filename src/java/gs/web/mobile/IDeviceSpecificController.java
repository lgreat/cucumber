package gs.web.mobile;

import gs.web.request.RequestInfo;

import javax.servlet.http.HttpServletRequest;

public interface IDeviceSpecificController {
    public boolean shouldHandleRequest(RequestInfo requestInfo);
}
