package gs.web.school;

import gs.web.mobile.IDeviceSpecificControllerPartOfPair;

public class TestScoresController extends PerlFetchController implements IDeviceSpecificControllerPartOfPair {
    private boolean _controllerHandlesMobileRequests;
    private boolean _controllerHandlesDesktopRequests;


    public boolean controllerHandlesMobileRequests() {
        return _controllerHandlesMobileRequests;
    }

    public boolean controllerHandlesDesktopRequests() {
        return _controllerHandlesDesktopRequests;
    }

    public void setControllerHandlesMobileRequests(boolean controllerHandlesMobileRequests) {
        _controllerHandlesMobileRequests = controllerHandlesMobileRequests;
    }

    public void setControllerHandlesDesktopRequests(boolean controllerHandlesDesktopRequests) {
        _controllerHandlesDesktopRequests = controllerHandlesDesktopRequests;
    }

}