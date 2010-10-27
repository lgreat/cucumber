package gs.web.util;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import gs.data.util.email.EmailUtils;
import org.apache.commons.lang.StringUtils;

/**
 * This controller provides a web interface to gs.data.util.email.EmailUtils.isValidEmail();
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.org>
 */
public class IsValidEmailController implements Controller {

    /** Spring BEAN id */
    public static final String BEAN_ID = "isValidEmailController";

    /**
     * @see gs.data.util.email.EmailUtils
     */
    public ModelAndView handleRequest(HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        if(StringUtils.isNotBlank(request.getParameter("emails"))){
            String[] emails = request.getParameter("emails").split("[,\\s]+");
            boolean isValid = true;

            StringBuffer badEmails = new StringBuffer();
            for (String email : emails) {
                if (!EmailUtils.isValidEmail(email)) {
                    isValid = false;
                    if (badEmails.length() > 0) {
                        badEmails.append(", ");
                    }
                    badEmails.append(email);
                }
            }
            if (StringUtils.equals("1", request.getParameter("details"))) {
                out.print(badEmails.toString());
            } else {
                out.print(isValid);
            }
            out.flush();
        }else{
            out.print(EmailUtils.isValidEmail(request.getParameter("email")));
            out.flush();
        }        
        return null;
    }
}
