package gs.web.school;

import gs.data.school.School;
import gs.web.ControllerFamily;
import gs.web.IControllerFamilySpecifier;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestScoresController extends PerlFetchController implements IControllerFamilySpecifier {

    private ControllerFamily _controllerFamily;

    @Override
    public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        School school = (School) request.getAttribute(SCHOOL_ATTRIBUTE);
        // GS-13082 Redirect to new profile if eligible
        if (shouldRedirectToNewProfile(school, request)) {
            return getRedirectToNewProfileModelAndView(school, request, NewProfileTabs.testScores);
        }
        return super.handleRequestInternal(request, response);
    }

    public ControllerFamily getControllerFamily() {
        return _controllerFamily;
    }

    public void setControllerFamily(ControllerFamily controllerFamily) {
        _controllerFamily = controllerFamily;
    }
}