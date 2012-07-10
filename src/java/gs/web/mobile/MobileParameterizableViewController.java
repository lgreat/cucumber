package gs.web.mobile;

import gs.web.ControllerFamily;
import gs.web.IControllerFamilySpecifier;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

public class MobileParameterizableViewController extends ParameterizableViewController implements IControllerFamilySpecifier {

    private ControllerFamily _controllerFamily;

    public ControllerFamily getControllerFamily() {
        return _controllerFamily;
    }

    public void setControllerFamily(ControllerFamily controllerFamily) {
        _controllerFamily = controllerFamily;
    }
}