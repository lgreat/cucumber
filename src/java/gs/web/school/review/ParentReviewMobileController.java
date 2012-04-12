package gs.web.school.review;

import gs.data.community.*;
import gs.data.community.local.ILocalBoardDao;
import gs.data.geo.IGeoDao;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.review.*;
import gs.web.mobile.IDeviceSpecificControllerPartOfPair;
import gs.web.school.*;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class ParentReviewMobileController extends AbstractController implements IDeviceSpecificControllerPartOfPair {

    public static final String BEAN_ID = "mobileParentReviewsController";

    private IReviewDao _reviewDao;
    private String _viewName;
    private RatingHelper _ratingHelper;
    private ParentReviewHelper _parentReviewHelper;
    private boolean _controllerHandlesDesktopRequests;
    private boolean _controllerHandlesMobileRequests;

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

        // handle preschool profile pages, redirect if not on preschool subdomain
        // Preschool profile pages should be hosted from pk.greatschools.org (GS-12127). Redirect if needed
        ModelAndView preschool = _parentReviewHelper.handlePreschool(request, school);
        if (preschool!=null){
            return preschool;
        }

        // make sure school object is not null
        if (null != school) {

            // always get all the reviews for mobile
            Set<Poster> reviewsBy = new HashSet<Poster>();
            List<Review> reviews = _reviewDao.getPublishedReviewsBySchool(school, reviewsBy);

            ParentReviewCommand cmd = new ParentReviewCommand();

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
            List<Review> reviewsToShow = reviews;

            // TODO: enable pagination again once method for pagination is determined

            int page = 1;
//            if (reviews.size() == 0 || sessionContext.isCrawler() || StringUtils.isNotEmpty(request.getParameter(PARAM_VIEW_ALL))) {
//                reviewsToShow = reviews;
//            } else {
//                String pageParam = request.getParameter(PARAM_PAGE);
//                if (pageParam != null) {
//                    try {
//                        page = Integer.parseInt(pageParam);
//                    } catch (Exception e) {
//                        // do nothing
//                    }
//                }
//                int fromIndex = (page - 1) * MAX_REVIEWS_PER_PAGE;
//                int toIndex = fromIndex + MAX_REVIEWS_PER_PAGE;
//
//                if ("principal".equals(reviews.get(0).getWho())) {
//                    fromIndex++;
//                    toIndex++;
//                }
//
//                toIndex = Math.min(toIndex, reviews.size());
//
//                if (fromIndex >= toIndex || fromIndex < 0) {
//                    String queryString = request.getQueryString();
//                    if (queryString != null) {
//                        queryString = UrlUtil.putQueryParamIntoQueryString(queryString, "page", "1");
//                    }
//                    return new ModelAndView(new RedirectView(request.getRequestURI() + (queryString != null ? "?" + queryString : "")));
//                }
//
//                reviewsToShow = reviews.subList(fromIndex, toIndex);
//            }
            model.put("reviewsToShow", reviewsToShow);

            model.put("page", page);

            // GS-10709
            UrlBuilder builder = new UrlBuilder(school, page, UrlBuilder.SCHOOL_PARENT_REVIEWS);
            model.put("relCanonical", builder.asFullUrlXml(request));

            model.put("cmd", cmd);
            model.put("param_sortby", PARAM_SORT_BY);

            boolean useCache = (null != pageHelper && pageHelper.isDevEnvironment() && !pageHelper.isStagingServer());

            Integer gsRating = getRatingHelper().getGreatSchoolsOverallRating(school, useCache);

            model.put("gs_rating", gsRating);
            model.put("ratings", ratings);

        }

        return new ModelAndView(getViewName(), model);
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