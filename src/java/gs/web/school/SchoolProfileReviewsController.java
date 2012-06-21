package gs.web.school;

import gs.data.school.School;
import gs.data.state.State;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author aroy@greatschools.org
 */
@Controller
@RequestMapping("/school/profileReviews.page")
public class SchoolProfileReviewsController extends AbstractSchoolProfileController {
    @RequestMapping(method= RequestMethod.GET)
    /**
     * @see gs.web.school.review.ParentReviewController
     */
    public Map<String,Object> handle(HttpServletRequest request,
                                     @RequestParam(value = "schoolId", required = false) Integer schoolId,
                                     @RequestParam(value = "state", required = false) State state
    ) {
        Map<String,Object> model = new HashMap<String, Object>();
        School school = getSchool(request, state, schoolId);

        return model;
    }
}
