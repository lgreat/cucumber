package gs.web.util;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import gs.data.util.email.EmailUtils;

/**
 * This controller provides a web interface to gs.data.util.email.EmailUtils.isValidEmail();
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class IsValidEmailController implements Controller {

    /** Spring BEAN id */
    public static final String BEAN_ID = "/util/isValidEmail.page";

    /**
     * @see gs.data.util.email.EmailUtils
     */
    public ModelAndView handleRequest(HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        out.print(EmailUtils.isValidEmail(request.getParameter("email")));
        out.flush();
        return null;
    }
}
