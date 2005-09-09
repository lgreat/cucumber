package gs.web.status;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.ServletException;
import javax.servlet.ServletContext;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class LoginController extends SimpleFormController {
    public ModelAndView onSubmit(Object command) throws ServletException {
        Identity ident = (Identity)command;
        ServletContext context = getServletContext();
        context.setAttribute("identity", ident);
        return new ModelAndView(new RedirectView(getSuccessView()));
    }
}
