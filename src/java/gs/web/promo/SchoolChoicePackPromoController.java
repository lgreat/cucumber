package gs.web.promo;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * Created by chriskimm@greatschools.net
 */
public class SchoolChoicePackPromoController extends AbstractController {

    public static final String BEAN_ID = "/promo/schoolChoicePackPromo.page";
    
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.println("{\"tester\":\"foo\"}");
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
