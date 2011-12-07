package gs.web.mobile;

public interface IDeviceSpecificControllerPartOfPair extends IDeviceSpecificController {
    public boolean controllerHandlesMobileRequests();
    public boolean controllerHandlesDesktopRequests();

    public void setControllerHandlesMobileRequests(boolean handlesMobileRequests);
    public void setControllerHandlesDesktopRequests(boolean handlesDesktopRequests);
}
