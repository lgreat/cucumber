package gs.web.status;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.validation.BindException;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * This controller handles the login authentication for the SearchManager page.
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class LoginController extends SimpleFormController {

    private static final Logger _log = Logger.getLogger(LoginController.class);

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws ServletException {

        HttpSession session = request.getSession(true);
        Identity ident = (Identity)command;
        _log.info("setting login identity: " + ident.toString());
        session.setAttribute("identity", ident);
        
        return new ModelAndView(new RedirectView(getSuccessView()));
    }
}
