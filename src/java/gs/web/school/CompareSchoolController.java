package gs.web.school;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles requests from a "compare school" form as found on the school overview page.
 * Simply packages the request parameters in a redirect url.
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class CompareSchoolController extends AbstractController {

    public static final String BEAN_ID = "/school/compareSchool.page";

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {

        StringBuffer urlBuffer = new StringBuffer();
        urlBuffer.append("/cgi-bin/cs_compare/");
        urlBuffer.append((String)request.getParameter("state")).append("?compare_type=");
        urlBuffer.append((String)request.getParameter("type")).append("&city=");
        urlBuffer.append((String)request.getParameter("city")).append("&school_selected=");
        urlBuffer.append((String)request.getParameter("id")).append("&level=");
        urlBuffer.append((String)request.getParameter("level"));        
        return new ModelAndView(new RedirectView(urlBuffer.toString()));
    }
}
