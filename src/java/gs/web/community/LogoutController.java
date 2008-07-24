package gs.web.community;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.web.util.PageHelper;

/**
 * This controller logs a user out by clearing all login-related cookies and
 * removing the user from the SessionContext.  If an optional redirect
 * query parameter is provided, then the value of that parameter is used
 * as the redirect target. Otherwise a default ("/") redirect target is used.
 */
public class LogoutController extends AbstractController {

    public static final String DEFAULT_VIEW = "/";

    /** optional parameter for the redirect target */
    public static final String PARAM_REDIRECT = "redirect";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        PageHelper.logout(request, response);
        return new ModelAndView(getRedirectView(request));
    }

    protected RedirectView getRedirectView(HttpServletRequest request) {
        String redirect = request.getParameter(PARAM_REDIRECT);
        RedirectView rView;
        if (StringUtils.isNotBlank(redirect)) {
            rView = new RedirectView(redirect);
        } else {
            rView = new RedirectView(DEFAULT_VIEW);
        }
        return rView;
    }
}