package gs.web;


import gs.web.mobile.IDeviceSpecificControllerPartOfPair;
import org.springframework.web.servlet.mvc.Controller;

import java.util.List;

public interface IControllerFactory {
    public Controller getController(List<? extends Controller> controllers);
}