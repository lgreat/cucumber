package gs.web.school;

import gs.data.school.School;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Puts a school object in model so that you can create a jspx page without having to create your own controller.
 * Similar in concept to ParameterizableViewController but with addition of school in model.
 *
 * Uses SchoolPageInterceptor to get the school from request so you need to set up your mapping
 * in pages-servlet.xml's schoolHandlerMapping' bean
 *
 * This class should not be subclassed.  If you find the need to put something else in the model
 * besides the school it probrably makes more sense to write your own class.
 *
 * @see gs.web.school.SchoolPageInterceptor
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public final class SchoolInModelController extends ParameterizableViewController {
    public static final String MODEL_SCHOOL = "school";
    
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        School s = (School) request.getAttribute(SchoolPageInterceptor.SCHOOL_ATTRIBUTE);
        Map m = new HashMap();
        m.put(MODEL_SCHOOL, s);

        return new ModelAndView(getViewName(), m);
    }    
}
