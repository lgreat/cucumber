package gs.web.path;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Delegates request with directory structure url to the appropriate controller.
 * @author Young Fan
 */
public class DirectoryStructureUrlRequestController extends AbstractController {
    private Controller _controller;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return _controller.handleRequest(request, response);
    }

    public void setController(Controller controller) {
        _controller = controller;
    }
}
