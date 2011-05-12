package gs.web.path;

import gs.data.url.DirectoryStructureUrlFactory;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.web.util.BadRequestLogger;
import gs.web.util.RedirectView301;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.data.state.State;

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

        SessionContext context = SessionContextUtil.getSessionContext(request);
        State state = context.getState();

        if (state == null) {
            Map<String, Object> model = new HashMap<String, Object>();
            BadRequestLogger.logBadRequest(_log, request, "Missing state in directory-structure url request.");
            model.put("showSearchControl", Boolean.TRUE);
            model.put("title", "State not found");
            return new ModelAndView("status/error", model);
        } else {
            // if state name in request uri was capitalized, 301-redirect to the same url with the state name in lowercase
            String uri = request.getRequestURI();
            String[] pathComponents = uri.split("/");
            if (pathComponents.length > 1) {
                String longStateName = pathComponents[1];               
                if (longStateName.equalsIgnoreCase("district-of-columbia") || !longStateName.equals(longStateName.toLowerCase())) {
                    uri = uri.replaceFirst("/" + longStateName + "/", "/" + DirectoryStructureUrlFactory.getStateNameForUrl(state) + "/");
                    String queryString = request.getQueryString();
                    String redirectUrl = uri + (!StringUtils.isBlank(queryString) ? "?" + queryString : "");
                    return new ModelAndView(new RedirectView301(redirectUrl));
                }
            }
        }

        // no controller was found that could handle this request (injected by calling getController on the controller factory)
        if (_controller == null) {
            Map<String, Object> model = new HashMap<String, Object>();
            BadRequestLogger.logBadRequest(_log, request, "Malformed directory-structure url request: " + request.getRequestURI());
            model.put("showSearchControl", Boolean.TRUE);
            model.put("title", "Invalid request");

            return new ModelAndView("status/error", model);
        }

        return _controller.handleRequest(request, response);
    }

    public static boolean isRequestURIWithCityAndStateOnly(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        if (requestUri == null) {
            throw new IllegalStateException("Cannot have null request uri");
        }

        return requestUri.matches("^/(.*?)/(.*?)/$");
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
