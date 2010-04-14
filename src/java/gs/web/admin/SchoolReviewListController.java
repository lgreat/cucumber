package gs.web.admin;

import gs.data.school.review.IReviewDao;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class SchoolReviewListController extends AbstractController {
    public static final String MODEL_REVIEW_LIST="reviews";
    private String _viewName;
    private IReviewDao _reviewDao;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();

        model.put(MODEL_REVIEW_LIST, _reviewDao.findUnprocessedReviews(50));

        return new ModelAndView(getViewName(), model);
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
