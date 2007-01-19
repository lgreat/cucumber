package gs.web.admin;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;

import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.UrlUtil;

/**
 * Simple controller that only responds to requests from development environments
 */
public class LimitToDevController implements Controller {
    private String _viewName;
    private String _errorViewName;

    public ModelAndView handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(httpServletRequest);
        if (new UrlUtil().isDevEnvironment(sessionContext.getHostName())) {
            return new ModelAndView(_viewName);
        }

        return new ModelAndView(_errorViewName);
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setErrorViewName(String errorViewName) {
        _errorViewName = errorViewName;
    }

    public String getErrorViewName() {
        return _errorViewName;
    }
}
