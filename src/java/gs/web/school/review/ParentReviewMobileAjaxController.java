package gs.web.school.review;

import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Poster;
import gs.data.school.review.Review;
import gs.web.jsp.Util;
import gs.web.school.AbstractSchoolController;
import gs.web.util.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;

public class ParentReviewMobileAjaxController extends AbstractController {

    public static final String BEAN_ID = "mobileParentReviewsController";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMMM d, yyyy");

    private IReviewDao _reviewDao;
    private String _viewName;
    private ParentReviewHelper _parentReviewHelper;
    private boolean _controllerHandlesDesktopRequests;
    private boolean _controllerHandlesMobileRequests;

    protected static final String PARAM_PAGE = "page";
    protected static final Log _log = LogFactory.getLog(ParentReviewController.class.getName());

    /**
     * Handle getting parent reviews for mobile devices
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        Map<String,Object> model = new HashMap<String,Object>();

        School school = (School) request.getAttribute(AbstractSchoolController.SCHOOL_ATTRIBUTE);
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        response.setContentType("application/json");


        // make sure school object is not null
        if (null != school) {

            // always get all the reviews for mobile
            Set<Poster> reviewsBy = new HashSet<Poster>();
            List<Review> reviews = _reviewDao.getPublishedReviewsBySchool(school, reviewsBy);

            // presort the list
            _parentReviewHelper.handleSortReviews(request, reviews);

            // reviews to show
            int page = _parentReviewHelper.findCurrentPage(request);
            int fromIndex = _parentReviewHelper.findFromIndex(page, reviews);
            int toIndex = _parentReviewHelper.findToIndex(page, fromIndex, reviews);

            // return the sublist of reviews
            List<Review> reviewsToShow = _parentReviewHelper.handlePagination(request, reviews, page, fromIndex, toIndex);

            model.put("reviews", serialize(school, reviewsToShow));
            model.put("page", page);
        }


        return new ModelAndView("json", model);
    }

    private List<Map> serialize(School school, List<Review> reviews){
        List<Map> list = new ArrayList<Map>();
        for (int i=0; i<reviews.size(); i++){
            Review review = reviews.get(i);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("id", review.getId());
            map.put("who", review.getWho());

            if ( school.getLevelCode().equals("p") && review.getPOverall()!=null ){
                map.put("curStarRating", review.getPOverall().getName());
            }
            else if ( review.getQuality()!=null ) {
                map.put("curStarRating", review.getQuality().getName());
            }

            map.put("posted", review.getPosted());

            map.put("postedFormatted", DATE_FORMAT.format(review.getPosted()) );
            map.put("comments", review.getComments());

            String[] splitComments = Util.splitAfterXWords(review.getComments(), 70);
            if (StringUtils.isBlank(splitComments[1])) {
                map.put("isTruncated", false);
            } else {
                map.put("isTruncated", true);
            }
            map.put("part1", splitComments[0]);
            map.put("part2", splitComments[1]);

            list.add(map);
        }
        return list;
    }

    public IReviewDao getReviewDao() {
        return _reviewDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public boolean controllerHandlesMobileRequests() {
        return _controllerHandlesMobileRequests;
    }

    public void setControllerHandlesMobileRequests(boolean controllerHandlesMobileRequests) {
        _controllerHandlesMobileRequests = controllerHandlesMobileRequests;
    }

    public boolean controllerHandlesDesktopRequests() {
        return _controllerHandlesDesktopRequests;
    }

    public void setControllerHandlesDesktopRequests(boolean controllerHandlesDesktopRequests) {
        _controllerHandlesDesktopRequests = controllerHandlesDesktopRequests;
    }

    public ParentReviewHelper getParentReviewHelper() {
        return _parentReviewHelper;
    }

    public void setParentReviewHelper(ParentReviewHelper parentReviewHelper) {
        _parentReviewHelper = parentReviewHelper;
    }

}
