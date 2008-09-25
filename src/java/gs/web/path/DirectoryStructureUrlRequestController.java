package gs.web.path;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.web.school.SchoolsController;
import gs.web.util.LogUtil;

import java.util.Map;
import java.util.HashMap;

/**
 * Delegates request with directory structure url to the appropriate controller.
 * @author Young Fan
 */
public class DirectoryStructureUrlRequestController extends AbstractController {
    public static final String BEAN_ID = "/directoryStructureUrlRequest.page";
    private static Logger _log = Logger.getLogger(DirectoryStructureUrlRequestController.class);

    private IDirectoryStructureUrlController _controller;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        if (!isRequestURIWithTrailingSlash(request)) {
            // redirect to the same url with a trailing slash appended to the uri
            String uri = request.getRequestURI() + "/";
            String queryString = request.getQueryString();
            String redirectUrl = uri + (!StringUtils.isBlank(queryString) ? "?" + queryString : "");
            return new ModelAndView(new RedirectView(redirectUrl));
        }

        // no controller was found that could handle this request (injected by calling getController on the controller factory)
        if (_controller == null) {
            Map<String, Object> model = new HashMap<String, Object>();
            LogUtil.log(_log, request, "Malformed directory-structure url request: " + request.getRequestURI());
            model.put("showSearchControl", Boolean.TRUE);
            model.put("title", "Invalid request");

            return new ModelAndView("status/error", model);
        }

        return _controller.handleRequest(request, response);
    }

    public static boolean isRequestURIWithTrailingSlash(HttpServletRequest request) {
        if (request.getRequestURI() == null) {
            throw new IllegalArgumentException("Request must have request URI");
        }
        return request.getRequestURI().endsWith("/");
    }

    public static String createURIWithTrailingSlash(HttpServletRequest request) {
        if (request.getRequestURI() == null) {
            throw new IllegalArgumentException("Request must have request URI");
        }
        return request.getRequestURI() +
            (isRequestURIWithTrailingSlash(request) ? "" : "/");
    }

    public void setController(IDirectoryStructureUrlController controller) {
        _controller = controller;
    }
}
