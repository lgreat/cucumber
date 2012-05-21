package gs.web;

import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.util.UrlUtil;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class MobileHomepageController implements Controller, IDirectoryStructureUrlController, IControllerFamilySpecifier {

    private boolean _beanSupportsMobileRequests;
    private boolean _beanSupportsDesktopRequests;
    private boolean _controllerHandlesMobileRequests;
    private boolean _controllerHandlesDesktopRequests;
    private boolean _isFromStateHome = false;

    private ControllerFamily _controllerFamily;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("fromStateHome", _isFromStateHome);
        if (!_isFromStateHome) {
            model.put("alternateSitePath", "/find-schools/");
        }
        return new ModelAndView("index-mobile", model);
    }

    // This controller supports state home urls, e.g. /california/
    public boolean shouldHandleRequest(DirectoryStructureUrlFields fields) {
        return fields != null &&
                fields.hasState() && !fields.hasCityName() && !fields.hasDistrictName() &&
                !fields.hasLevelCode() && !fields.hasSchoolName();
    }

    public boolean beanSupportsMobileRequests() {
        return _beanSupportsMobileRequests;
    }

    public boolean beanSupportsDesktopRequests() {
        return _beanSupportsDesktopRequests;
    }

    public void setBeanSupportsMobileRequests(boolean beanSupportsMobileRequests) {
        _beanSupportsMobileRequests = beanSupportsMobileRequests;
    }

    public void setBeanSupportsDesktopRequests(boolean beanSupportsDesktopRequests) {
        _beanSupportsDesktopRequests = beanSupportsDesktopRequests;
    }

    public boolean controllerHandlesMobileRequests() {
        return _controllerHandlesMobileRequests;
    }

    public void setControllerHandlesMobileRequests(boolean controllerHandlesMobileRequests) {
        _controllerHandlesMobileRequests = controllerHandlesMobileRequests;
    }

    public boolean controllerHandlesDesktopRequests() {
        return _controllerHandlesDesktopRequests;
    }

    public void setControllerHandlesDesktopRequests(boolean controllerHandlesDesktopRequests) {
        _controllerHandlesDesktopRequests = controllerHandlesDesktopRequests;
    }

    public boolean isFromStateHome() {
        return _isFromStateHome;
    }

    public void setFromStateHome(boolean fromStateHome) {
        _isFromStateHome = fromStateHome;
    }

    public ControllerFamily getControllerFamily() {
        return _controllerFamily;
    }

    public void setControllerFamily(ControllerFamily controllerFamily) {
        _controllerFamily = controllerFamily;
    }
}
