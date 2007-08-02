package gs.web.school.review;

import gs.data.school.School;
import gs.web.school.AbstractSchoolController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class ThankYouController extends AbstractSchoolController {

    public static final String BEAN_ID = "/school/thankYouHover.page";

    private String _viewName;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        School school = (School) request.getAttribute(SCHOOL_ATTRIBUTE);

        ModelAndView mAndV = new ModelAndView();
        mAndV.getModel().put("school", school);
        mAndV.setViewName(getViewName());

        return mAndV;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
}
