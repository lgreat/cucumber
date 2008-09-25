package gs.web.path;

import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;

import gs.web.school.SchoolsController;
import gs.web.school.SchoolOverviewController;

import java.util.List;

/**
 * @author Young Fan
 */
public class DirectoryStructureUrlControllerFactory implements IDirectoryStructureUrlControllerFactory {
    private static Logger _log = Logger.getLogger(DirectoryStructureUrlControllerFactory.class);

    private HttpServletRequest _request;

    private List<IDirectoryStructureUrlController> _controllers;

    public IDirectoryStructureUrlController getController() {
        if (_request == null) {
            throw new IllegalStateException("Request was null.");
        }

        for (IDirectoryStructureUrlController controller : _controllers) {
            if (controller.isValidRequest(_request)) {
                return controller;
            }
        }

        return null;
    }

    // auto-wired
    public void setRequest(HttpServletRequest request) {
        _request = request;
    }

    // explicitly set in spring config file
    public void setControllers(List<IDirectoryStructureUrlController> controllers) {
        _controllers = controllers;
    }
}
