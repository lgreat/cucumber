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
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
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
            String[] emails = request.getParameter("emails").split("[,\\s]");
            boolean isValid = true;
            for(int i=0;i<emails.length;i++){
                if(!EmailUtils.isValidEmail(emails[i])){
                    isValid = false;
                }
            }
            out.print(isValid);
            out.flush();
        }else{
            out.print(EmailUtils.isValidEmail(request.getParameter("email")));
            out.flush();
        }        
        return null;
    }
}
