package gs.web.school;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Poster;
import gs.data.school.review.Ratings;
import gs.data.school.review.Review;
import gs.web.school.review.ParentReviewHelper;
import gs.web.util.PageHelper;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author aroy@greatschools.org
 */
@Controller
@RequestMapping("/school/profileReviews.page")
public class SchoolProfileReviewsController extends AbstractSchoolProfileController {

    private static final int MAX_NUMBER_OF_REVIEWS_PER_PAGE = 20;

    private ParentReviewHelper _parentReviewHelper;
    private RatingHelper _ratingHelper;
    private ISchoolDao _schoolDao;
    private IReviewDao _reviewDao;
    private SchoolProfileDataHelper _schoolProfileDataHelper;

    @RequestMapping(method= RequestMethod.GET)
    /**
     * @see gs.web.school.review.ParentReviewController
     */
    public Map<String,Object> handle(HttpServletRequest request, HttpServletResponse response,
             @RequestParam(value = ParentReviewHelper.PARAM_PAGE, required = false, defaultValue = "0") Integer page,
             @RequestParam(value = ParentReviewHelper.PARAM_SORT_BY, required = false ) String sortBy,
             @RequestParam(value = ParentReviewHelper.PARAM_PAGER_OFFSET, required = false) Integer pagerOffset,
             @RequestParam(value = ParentReviewHelper.PARAM_VIEW_ALL, required = false) String viewAll,
             @RequestParam(value = ParentReviewHelper.PARAM_REVIEWS_BY, required = false, defaultValue = "") String reviewsByParam
    ) throws IOException {
        Map<String,Object> model = new HashMap<String, Object>();
        School school = getSchool(request);
        model.put("school", school);
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);

        KindercareLeadGenHelper.checkForKindercare(request,response,school,model);

        if (null != school) {
            SessionContext sessionContext = SessionContextUtil.getSessionContext(request);

            boolean includeInactive = _parentReviewHelper.handleMssUser(school, sessionContext, request, model);
            Set<Poster> reviewsBy = _parentReviewHelper.handleGetReviewsBy(reviewsByParam);
            Map<Poster,Integer> numReviewsBy;

            ParentReviewHelper.ParentReviewCommand cmd = new ParentReviewHelper.ParentReviewCommand();

            Ratings ratings = _schoolProfileDataHelper.getCommunityRatings(request);
            cmd.setRatings(ratings);

            List<Review> reviews;
            Long numberOfNonPrincipalReviews;
            if (!includeInactive) {
                reviews = _reviewDao.getPublishedReviewsBySchool(school, reviewsBy);
                numReviewsBy = _reviewDao.getNumPublishedReviewsBySchool(school, reviewsBy);
                numberOfNonPrincipalReviews = (ratings.getNumberOfReviews()!=null?ratings.getNumberOfReviews():new Long(0));
            } else {
                reviews = _reviewDao.getPublishedDisabledReviewsBySchool(school, reviewsBy);
                numReviewsBy = _reviewDao.getNumPublishedDisabledReviewsBySchool(school, reviewsBy);
                numberOfNonPrincipalReviews = _reviewDao.countPublishedDisabledNonPrincipalReviewsBySchool(school, reviewsBy);
            }


            _parentReviewHelper.handleLastModifiedDateInModel(model, school, reviewsBy, reviews);
            _parentReviewHelper.handleSortReviews(sortBy, reviews);

            cmd.setSortBy(sortBy);
            cmd.setSchool(school);
            cmd.setReviews(reviews);
            cmd.setTotalReviews(numberOfNonPrincipalReviews.intValue());
            cmd.setCurrentDate(new Date());

            _parentReviewHelper.handleReviewReports(reviews, model, request);
            model.put("loginRedirectUrl", _parentReviewHelper.getLoginRedirectUrl(school, model, request));
            _parentReviewHelper.handleNumberOfReviewsCount(numReviewsBy, model);
            _parentReviewHelper.handleGetReviewsByCsv(reviewsBy, model);

            int serverPort = request.getServerPort();
            String url = "http://" + request.getServerName() +
                    ((serverPort != 80) ? ":" + serverPort : "") + request.getRequestURI();

            model.put(ParentReviewHelper.MODEL_URI, url);
            model.put("reviewsTotalPages", _parentReviewHelper.getReviewsTotalPages(numberOfNonPrincipalReviews.intValue(), MAX_NUMBER_OF_REVIEWS_PER_PAGE));
            model.put("param_reviewsby", ParentReviewHelper.PARAM_REVIEWS_BY);

            _parentReviewHelper.handleCombinedSubcategoryRatings(model, school, ratings);

            page = _parentReviewHelper.findCurrentPage(request);
            int fromIndex = _parentReviewHelper.findFromIndex(page, MAX_NUMBER_OF_REVIEWS_PER_PAGE, reviews);
            int toIndex = _parentReviewHelper.findToIndex(page, fromIndex, MAX_NUMBER_OF_REVIEWS_PER_PAGE, reviews);

            // page param is invalid number -- too high or too low, so redirect to first page and preserve any request params
            List<Review> reviewsToShow = new ArrayList<Review>();
            if (reviews.size()==0 || sessionContext.isCrawler() || StringUtils.isNotEmpty(request.getParameter(ParentReviewHelper.PARAM_VIEW_ALL))){
                reviewsToShow = reviews;
            }
            else {
                if (fromIndex == 1 && toIndex == 1) {
                    // the only published review is a principal review, so no other reviews to show
                } else if (fromIndex >= toIndex || fromIndex < 0) {
                    String queryString = request.getQueryString();
                    if (queryString != null) {
                        queryString = UrlUtil.putQueryParamIntoQueryString(queryString, "page", "1");
                    }
                    response.sendRedirect(request.getRequestURI() + (queryString !=null ? "?" + queryString : ""));
                } else {
                    reviewsToShow = _parentReviewHelper.handlePagination(request, reviews, page, fromIndex, toIndex);
                }

            }
            model.put("reviewsToShow", reviewsToShow);
            model.put("page", page);
            model.put("reviewsFilterSortTracking", _parentReviewHelper.handleReviewsFilterSortTracking(cmd.getTotalReviews(), reviewsBy, sortBy));

            // GS-10633
            _parentReviewHelper.handleOverallRatingsByYear(model, school);

            model.put("cmd", cmd);
            model.put("param_sortby", sortBy);

            boolean useCache = (null != pageHelper && pageHelper.isDevEnvironment() && !pageHelper.isStagingServer());

            Integer gsRating = getRatingHelper().getGreatSchoolsOverallRating(school, useCache);

            model.put("gs_rating", gsRating);
            model.put("ratings", ratings);

        }

        return model;
    }

    public RatingHelper getRatingHelper() {
        return _ratingHelper;
    }

    public void setRatingHelper(RatingHelper ratingHelper) {
        _ratingHelper = ratingHelper;
    }

    public ParentReviewHelper getParentReviewHelper() {
        return _parentReviewHelper;
    }

    public void setParentReviewHelper(ParentReviewHelper parentReviewHelper) {
        _parentReviewHelper = parentReviewHelper;
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
    public SchoolProfileDataHelper getSchoolProfileDataHelper() {
        return _schoolProfileDataHelper;
    }

    public void setSchoolProfileDataHelper(SchoolProfileDataHelper schoolProfileDataHelper) {
        _schoolProfileDataHelper = schoolProfileDataHelper;
    }

}
