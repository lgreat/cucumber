package gs.web.school.review;

import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.web.school.AbstractSchoolController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class ThankYouController extends AbstractSchoolController {

    public static final String BEAN_ID = "/school/thankYouHover.page";

    protected static final String MODEL_HAS_REVIEW = "hasReview";
    protected static final String MODEL_SCHOOL = "school";

    private String _viewName;
    private IReviewDao _reviewDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        School school = (School) request.getAttribute(SCHOOL_ATTRIBUTE);
        List reviews = getReviewDao().getPublishedReviewsBySchool(school);

        ModelAndView mAndV = new ModelAndView();
        mAndV.getModel().put(MODEL_SCHOOL, school);
        mAndV.getModel().put(MODEL_HAS_REVIEW, reviews.size()!=0);
        mAndV.setViewName(getViewName());
        return mAndV;
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
