package gs.web.util;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by IntelliJ IDEA.
 * User: youngfan
 * Date: Dec 30, 2008
 * Time: 2:20:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class Redirect301Controller implements Controller {
    private String _redirectPath;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String p = request.getParameter("page");
        return new ModelAndView(new RedirectView301(_redirectPath));
    }

    public String getRedirectPath() {
        return _redirectPath;
    }

    public void setRedirectPath(String redirectPath) {
        _redirectPath = redirectPath;
    }
}
