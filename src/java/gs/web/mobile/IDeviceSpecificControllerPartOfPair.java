package gs.web.mobile;

/**
 * Implement this controller if the page that your controller handles can support a mobile request,
 * but your specific controller only ever handles either a mobile request OR a desktop request.
 * Then the bean which uses the controller must be configured in spring with the properties below.
 */
public interface IDeviceSpecificControllerPartOfPair {
    /** Whether or not any instance of the controller can handle a mobile request */
    public boolean controllerHandlesMobileRequests();

    /** Whether or not any instance of the controller can handle a desktop request */
    public boolean controllerHandlesDesktopRequests();

    public void setControllerHandlesMobileRequests(boolean handlesMobileRequests);
    public void setControllerHandlesDesktopRequests(boolean handlesDesktopRequests);
}
