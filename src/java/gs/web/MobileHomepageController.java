package gs.web;


import gs.web.mobile.IDeviceSpecificControllerPartOfPair;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

public class MobileHomepageController implements Controller, IDeviceSpecificControllerPartOfPair {

    private boolean _beanSupportsMobileRequests;
    private boolean _beanSupportsDesktopRequests;
    private boolean _controllerHandlesMobileRequests;
    private boolean _controllerHandlesDesktopRequests;


    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView("index-mobile", new HashMap<String,Object>());
    }

    public boolean beanSupportsMobileRequests() {
        return _beanSupportsMobileRequests;
    }

    public boolean beanSupportsDesktopRequests() {
        return _beanSupportsDesktopRequests;
    }

    public void setBeanSupportsMobileRequests(boolean beanSupportsMobileRequests) {
        _beanSupportsMobileRequests = beanSupportsMobileRequests;
    }

    public void setBeanSupportsDesktopRequests(boolean beanSupportsDesktopRequests) {
        _beanSupportsDesktopRequests = beanSupportsDesktopRequests;
    }

    public boolean controllerHandlesMobileRequests() {
        return _controllerHandlesMobileRequests;
    }

    public void setControllerHandlesMobileRequests(boolean controllerHandlesMobileRequests) {
        _controllerHandlesMobileRequests = controllerHandlesMobileRequests;
    }

    public boolean controllerHandlesDesktopRequests() {
        return _controllerHandlesDesktopRequests;
    }

    public void setControllerHandlesDesktopRequests(boolean controllerHandlesDesktopRequests) {
        _controllerHandlesDesktopRequests = controllerHandlesDesktopRequests;
    }
}

