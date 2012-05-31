package gs.web;


import gs.web.mobile.IDeviceSpecificControllerPartOfPair;
import org.springframework.web.servlet.mvc.Controller;

import java.util.List;

public interface IControllerFamilyFactory {
    public IControllerFamilySpecifier getController(List<IControllerFamilySpecifier> controllers);
}