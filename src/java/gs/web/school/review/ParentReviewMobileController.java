package gs.web.school.review;

import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.review.*;
import gs.data.test.ITestDataSetDao;
import gs.web.ControllerFamily;
import gs.web.IControllerFamilySpecifier;
import gs.web.school.*;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class ParentReviewMobileController extends AbstractController implements IControllerFamilySpecifier {

    public static final String BEAN_ID = "mobileParentReviewsController";

    private IReviewDao _reviewDao;
    private ITestDataSetDao _testDataSetDao;
    private String _viewName;
    private String _ajaxViewName;
    private RatingHelper _ratingHelper;
    private ParentReviewHelper _parentReviewHelper;
    private ControllerFamily _controllerFamily;

    protected static final int MAX_REVIEWS_PER_PAGE = 4; //number of reviews per page
    protected static final String PARAM_PAGE = "page";
    protected static final String PARAM_SORT_BY = "sortBy";
    protected static final String PARAM_REVIEWS_BY = "reviewsBy";
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

        // make sure school object is not null
        if (null != school) {

            // always get all the reviews for mobile
            Set<Poster> reviewsBy = new HashSet<Poster>();
            List<Review> reviews = _reviewDao.getPublishedReviewsBySchool(school, reviewsBy);

            ParentReviewCommand cmd = new ParentReviewCommand();
            cmd.setAjaxRequest((request.getParameter("ajax")!=null && request.getParameter("ajax").equals("true")));

            Ratings ratings = _reviewDao.findRatingsBySchool(school);
            cmd.setRatings(ratings);

            // sort the reviews according to request param
            String paramSortBy = _parentReviewHelper.handleSortReviews(request, reviews);

            Long numberOfNonPrincipalReviews = _reviewDao.countPublishedNonPrincipalReviewsBySchool(school, reviewsBy);

            cmd.setSortBy(paramSortBy);
            cmd.setSchool(school);
            cmd.setReviews(reviews);
            cmd.setTotalReviews(numberOfNonPrincipalReviews.intValue());
            cmd.setCurrentDate(new Date());

            model.put("reviewsTotalPages", getReviewsTotalPages(numberOfNonPrincipalReviews.intValue()));
            model.put("param_reviewsby", PARAM_REVIEWS_BY);

            _parentReviewHelper.handleSubcategoryRatings(model, school, ratings);

            // reviews to show
            int page = _parentReviewHelper.findCurrentPage(request);
            int fromIndex = _parentReviewHelper.findFromIndex(page, reviews);
            int toIndex = _parentReviewHelper.findToIndex(page, fromIndex, reviews);

            // in the mobile controller, we need to display everything prior to
            // the requested page as well because of the ajax-based scroll
            if (page>1 && !cmd.isAjaxRequest()) {
                fromIndex = 0;
            }

            List<Review> reviewsToShow = _parentReviewHelper.handlePagination(request, reviews, page, fromIndex, toIndex);
            if (reviewsToShow.size()!=reviews.size()){
                if (reviews.size()!=1 && (fromIndex >= toIndex || fromIndex < 0)) {
                    String queryString = request.getQueryString();
                    if (queryString != null) {
                        queryString = UrlUtil.putQueryParamIntoQueryString(queryString, "page", "1");
                    }
                    return new ModelAndView(new RedirectView(request.getRequestURI() + (queryString != null ? "?" + queryString : "")));
                }
            }

            model.put("reviewsToShow", reviewsToShow);
            model.put("page", page);

            // GS-10709
            UrlBuilder builder = new UrlBuilder(school, page, UrlBuilder.SCHOOL_PARENT_REVIEWS);
            model.put("relCanonical", builder.asFullCanonicalUrl(request));
            model.put("schoolUrl", new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE).asFullCanonicalUrl(request));

            model.put("cmd", cmd);
            model.put("param_sortby", PARAM_SORT_BY);

            boolean useCache = (null != pageHelper && pageHelper.isDevEnvironment() && !pageHelper.isStagingServer());

            Integer gsRating = getRatingHelper().getGreatSchoolsOverallRating(school, useCache);

            model.put("gs_rating", gsRating);
            model.put("ratings", ratings);
            model.put("hasTestScores", hasTestScores(school));

            if (cmd.isAjaxRequest()){
                return new ModelAndView(getAjaxViewName(), model);
            }

        }

        return new ModelAndView(getViewName(), model);
    }

    public boolean hasTestScores(School school) {
        boolean hasTestScores = true;
        if (StringUtils.equals("private", school.getType().getSchoolTypeName())) {
            hasTestScores = school.getStateAbbreviation().isPrivateTestScoresState() &&
                    _testDataSetDao.hasDisplayableData(school);
        } else if (LevelCode.PRESCHOOL.equals(school.getLevelCode())) {
            hasTestScores = false;
        }
        return hasTestScores;
    }

    protected static int getReviewsTotalPages(int numReviews) {
        int totalPages;
        if (numReviews > 0) {
            totalPages = numReviews / MAX_REVIEWS_PER_PAGE;
            if (numReviews % MAX_REVIEWS_PER_PAGE > 0) {
                totalPages++;
            }
        } else {
            totalPages = 1;
        }
        return totalPages;
    }

    public static class ParentReviewCommand {
        private School _school;
        private List<Review> _reviews;
        private Date _currentDate;
        private int _totalReviews = 0;
        private String _sortBy;
        private Ratings _ratings;
        private boolean _ajaxRequest;

        public School getSchool() {
            return _school;
        }

        public void setSchool(School school) {
            _school = school;
        }

        public List<Review> getReviews() {
            return _reviews;
        }

        public void setReviews(List<Review> reviews) {
            _reviews = reviews;
        }

        public Date getCurrentDate() {
            return _currentDate;
        }

        public void setCurrentDate(Date currentDate) {
            _currentDate = currentDate;
        }


        public int getTotalReviews() {
            return _totalReviews;
        }

        public void setTotalReviews(int totalReviews) {
            _totalReviews = totalReviews;
        }

        public String getSortBy() {
            return _sortBy;
        }

        public void setSortBy(String sortBy) {
            _sortBy = sortBy;
        }

        public Ratings getRatings() {
            return _ratings;
        }

        public void setRatings(Ratings ratings) {
            _ratings = ratings;
        }

        public boolean isAjaxRequest() {
            return _ajaxRequest;
        }

        public void setAjaxRequest(boolean ajaxRequest) {
            _ajaxRequest = ajaxRequest;
        }
    }

    public IReviewDao getReviewDao() {
        return _reviewDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }

    public RatingHelper getRatingHelper() {
        return _ratingHelper;
    }

    public void setRatingHelper(RatingHelper ratingHelper) {
        _ratingHelper = ratingHelper;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public ParentReviewHelper getParentReviewHelper() {
        return _parentReviewHelper;
    }

    public void setParentReviewHelper(ParentReviewHelper parentReviewHelper) {
        _parentReviewHelper = parentReviewHelper;
    }

    public String getAjaxViewName() {
        return _ajaxViewName;
    }

    public void setAjaxViewName(String ajaxViewName) {
        _ajaxViewName = ajaxViewName;
    }

    public void setTestDataSetDao(ITestDataSetDao testDataSetDao) {
        _testDataSetDao = testDataSetDao;
    }

    public ControllerFamily getControllerFamily() {
        return _controllerFamily;
    }

    public void setControllerFamily(ControllerFamily controllerFamily) {
        _controllerFamily = controllerFamily;
    }
}
