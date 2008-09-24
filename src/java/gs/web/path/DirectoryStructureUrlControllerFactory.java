package gs.web.path;

import org.springframework.web.servlet.mvc.Controller;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;

import gs.web.school.SchoolsController;
import gs.web.school.SchoolOverviewController;

/**
 * @author Young Fan
 */
public class DirectoryStructureUrlControllerFactory implements IControllerFactory {
    private static Logger _log = Logger.getLogger(DirectoryStructureUrlControllerFactory.class);

    private HttpServletRequest _request;

    private SchoolsController _schoolsController;
    private SchoolOverviewController _schoolOverviewController;

    public Controller getController() {
        // TODO-7171
        if (_request != null) {
            _log.info("_request.getRequestURL(): " + _request.getRequestURL());
            return _schoolsController;
        } else {
            _log.info("request was null");
            return null;
        }
        //return _schoolOverviewController;
    }

    public void setRequest(HttpServletRequest request) {
        _request = request;
    }

    public void setSchoolsController(SchoolsController schoolsController) {
        _schoolsController = schoolsController;
    }

    public void setSchoolOverviewController(SchoolOverviewController schoolOverviewController) {
        _schoolOverviewController = schoolOverviewController;
    }
}
