package gs.web.community;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.web.util.PageHelper;

/**
 * This controller logs a user out by clearing all login-related cookies and removing the user
 * from the SessionContext.
 */
public class LogoutController extends AbstractController {

    public static final String DEFAULT_VIEW = "redirect:/";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        PageHelper.logout(request, response);
        return new ModelAndView(getRedirectView(request));
    }

    protected String getRedirectView(HttpServletRequest request) {
        //request.getParameter()
        // todo
        return DEFAULT_VIEW;
    }
}