package gs.web.mobile;

/**
 * Marker interface to signify a controller has only being capable of handling logic needed to render on mobile devices
 * Controllers not implementing IMobileOnlyController or IControllerWithMobileView are considered to be
 * "Desktop only controllers"
 */
public interface IMobileOnlyController extends IControllerWithMobileView {
}
