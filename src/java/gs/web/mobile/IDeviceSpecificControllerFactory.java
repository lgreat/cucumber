package gs.web.mobile;


import org.springframework.web.servlet.mvc.Controller;

import java.util.List;

public interface IDeviceSpecificControllerFactory {
    public Object getDeviceSpecificController(List<IDeviceSpecificControllerPartOfPair> controllers);
}
