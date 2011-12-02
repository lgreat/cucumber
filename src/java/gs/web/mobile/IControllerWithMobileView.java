package gs.web.mobile;


/**
 * Marker interface that signifies that a controller can be asked to handle logic and a view for mobile devices, in
 * addition to default desktop logic / view
 * Controllers not implementing IMobileOnlyController or IControllerWithMobileView are considered to be
 * "Desktop only controllers"
 */
public interface IControllerWithMobileView {
    String getMobileViewName();
}
