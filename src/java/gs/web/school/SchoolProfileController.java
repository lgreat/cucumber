package gs.web.school;

import gs.data.school.School;
import gs.web.ControllerFamily;
import gs.web.IControllerFamilySpecifier;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.request.RequestInfo;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author Dave Roy <mailto:droy@greatschools.org>
 */
public class SchoolProfileController extends AbstractSchoolController implements IDirectoryStructureUrlController, IControllerFamilySpecifier {
    protected static final Log _log = LogFactory.getLog(SchoolProfileController.class.getName());

    private String _viewName;
    private ControllerFamily _controllerFamily;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();


        School school = _requestAttributeHelper.getSchool(request);
        model.put("school", school);

        // TODO: Audit SchoolOverview2010Controller and refactor all shared logic such as number1expert cobrand.  The
        // new profile and old profile will coexist side by side for a while, so they need to share code.

        // Create a map on the request used to save references to data that is to be reused.
        // This needs to be done early in the request cycle or the item attached to the request can be lost.
        AbstractDataHelper.initialize( request );

        return new ModelAndView(_viewName, model);
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }


    public ControllerFamily getControllerFamily() {
        return _controllerFamily;
    }

    public void setControllerFamily(ControllerFamily controllerFamily) {
        _controllerFamily = controllerFamily;
    }
}