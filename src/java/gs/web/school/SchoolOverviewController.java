 package gs.web.school;

import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * This controller builds handles requests for the School Profile Overview page:
 * http://www.greatschools.net/school/overview.page?state=tx&id=10683
 * 
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SchoolOverviewController extends AbstractSchoolController {

    /** Spring Bean id */
    public static final String BEAN_ID = "/school/overview.page";

    /** The allowed length of the parent review blurb */
    public static final int REVIEW_LENGTH = 100;

    private String _viewName;
    private IReviewDao _reviewDao;

    /**
     * This method must be called using the standard Spring Controller workflow, that
     * is, it must be called by the superclass handleRequest() method in order to
     * assure that a valid school is available with the getSchool() method.
     *
     * @param request provided by servlet container
     * @param response provided by servlet container
     * @return a ModelAndView
     * @throws Exception
     */
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        Map model = new HashMap();

        String schoolIdStr = request.getParameter("id");
        if (StringUtils.isNumeric(schoolIdStr)) {
            School school = getSchool();
            model.put("school", school);
            List reviews = _reviewDao.getPublishedReviewsBySchool(school);
            if (reviews != null && reviews.size() > 0) {
                model.put("reviewCount", new Integer(reviews.size()));
                Review review = (Review)reviews.get(0);
                if (review != null) {
                    model.put("reviewText", StringUtils.abbreviate(review.getComments(), REVIEW_LENGTH));
                }
            }
        }
        return new ModelAndView(_viewName, model);
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public IReviewDao getReviewDao() {
        return _reviewDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }
}


