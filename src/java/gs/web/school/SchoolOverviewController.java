 package gs.web.school;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import gs.data.state.State;
import gs.web.util.context.SessionContextUtil;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SchoolOverviewController extends AbstractController {

    /** The allowed length of the parent review blurb */
    public static final int REVIEW_LENGTH = 100;

    private String _viewName;
    private ISchoolDao _schoolDao;
    private IReviewDao _reviewDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        Map model = new HashMap();

        String schoolIdStr = request.getParameter("id");
        if (StringUtils.isNumeric(schoolIdStr)) {
            Integer schoolId = new Integer(schoolIdStr);
            State state = SessionContextUtil.getSessionContext(request).getState();
            School school = _schoolDao.getSchoolById(state, schoolId);
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

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public IReviewDao getReviewDao() {
        return _reviewDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }
}


