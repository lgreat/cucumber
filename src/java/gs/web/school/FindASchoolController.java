package gs.web.school;

import gs.web.ControllerFamily;
import gs.web.IControllerFamilySpecifier;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

/**
 * @author aroy@greatschools.org
 */
public class FindASchoolController extends ParameterizableViewController implements IControllerFamilySpecifier {

    public ControllerFamily getControllerFamily() {
        return ControllerFamily.DESKTOP;
    }
}
