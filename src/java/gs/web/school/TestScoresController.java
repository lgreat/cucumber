package gs.web.school;

import gs.web.ControllerFamily;
import gs.web.IControllerFamilySpecifier;

public class TestScoresController extends PerlFetchController implements IControllerFamilySpecifier {

    private ControllerFamily _controllerFamily;

    public ControllerFamily getControllerFamily() {
        return _controllerFamily;
    }

    public void setControllerFamily(ControllerFamily controllerFamily) {
        _controllerFamily = controllerFamily;
    }
}