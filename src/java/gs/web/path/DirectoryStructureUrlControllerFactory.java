package gs.web.path;

import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;

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

        // extract request information from the request uri
        DirectoryStructureUrlFields fields = new DirectoryStructureUrlFields(_request);
        // set the fields in the request instead of in the controller itself or else different requests would
        // be using each others' fields!
        _request.setAttribute(IDirectoryStructureUrlController.FIELDS, fields);

        // pick the appropriate controller to handle the request
        for (IDirectoryStructureUrlController controller : _controllers) {
            if (controller.shouldHandleRequest(fields)) {
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
