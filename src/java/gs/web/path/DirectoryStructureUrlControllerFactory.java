package gs.web.path;

import org.apache.log4j.Logger;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * @author Young Fan
 */
public class DirectoryStructureUrlControllerFactory implements IDirectoryStructureUrlControllerFactory {
    private static Logger _log = Logger.getLogger(DirectoryStructureUrlControllerFactory.class);

    private List<IDirectoryStructureUrlController> _controllers;

    public IDirectoryStructureUrlController getController() {
        HttpServletRequest request = getHttpServletRequest();
        if (request == null) {
            throw new IllegalStateException("Request was null.");
        }

        // extract request information from the request uri
        DirectoryStructureUrlFields fields = new DirectoryStructureUrlFields(request);
        // set the fields in the request instead of in the controller itself or else different requests would
        // be using each others' fields!
        request.setAttribute(IDirectoryStructureUrlController.FIELDS, fields);

        // pick the appropriate controller to handle the request
        for (IDirectoryStructureUrlController controller : _controllers) {
            if (controller.shouldHandleRequest(fields)) {
                return controller;
            }
        }

        return null;
    }

    public HttpServletRequest getHttpServletRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    // explicitly set in spring config file
    public void setControllers(List<IDirectoryStructureUrlController> controllers) {
        _controllers = controllers;
    }
}
