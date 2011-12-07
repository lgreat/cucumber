package gs.web.mobile;


public interface IConntrollerWithDeviceSpecificViews extends IDeviceSpecificController {
    String getViewName();
    String getMobileViewName();
}
