package gs.web.school;

import gs.data.community.User;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.review.*;
import gs.data.security.Role;
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

    static final int MAX_NUMBER_OF_REVIEWS_PER_PAGE = 20;

    private ParentReviewHelper _parentReviewHelper;
    private RatingHelper _ratingHelper;
    private ISchoolDao _schoolDao;
    private IReviewDao _reviewDao;
    private ITopicalSchoolReviewDao _topicalSchoolReviewDao;
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

            // determine if user has an mss subscription to this school
            // also determine if user is a moderator
            boolean includeInactive = _parentReviewHelper.handleMssUser(school, sessionContext, request, model);
            // Determine filtering by poster
            Set<Poster> reviewsBy = _parentReviewHelper.handleGetReviewsBy(reviewsByParam);
            Map<Poster,Integer> numReviewsBy;

            ParentReviewHelper.ParentReviewCommand cmd = new ParentReviewHelper.ParentReviewCommand();

            // note this object contains both the ratings and the # of reviews
            // At this time, ratings is not calculated using topical reviews
            // But # of reviews SHOULD. Therefore, I will need to edit the # of reviews somewhere
            Ratings ratings = _schoolProfileDataHelper.getCommunityRatings(request);
            cmd.setRatings(ratings);

            List<Review> overallReviews;
            List<TopicalSchoolReview> topicalReviews;
            List<ISchoolReview> allReviews;
            Long numberOfNonPrincipalReviews;
            if (!includeInactive) {
                overallReviews = _reviewDao.getPublishedReviewsBySchool(school, reviewsBy);
                topicalReviews = _topicalSchoolReviewDao.findBySchoolId(school.getDatabaseState(), school.getId(), reviewsBy);
                numReviewsBy = _reviewDao.getNumPublishedReviewsBySchool(school, reviewsBy);
                numberOfNonPrincipalReviews = _reviewDao.countPublishedNonPrincipalReviewsBySchool(school, reviewsBy);
            } else {
                overallReviews = _reviewDao.getPublishedDisabledReviewsBySchool(school, reviewsBy);
                topicalReviews = _topicalSchoolReviewDao.findBySchoolId(school.getDatabaseState(), school.getId(), reviewsBy, true);
                numReviewsBy = _reviewDao.getNumPublishedDisabledReviewsBySchool(school, reviewsBy);
                numberOfNonPrincipalReviews = _reviewDao.countPublishedDisabledNonPrincipalReviewsBySchool(school, reviewsBy);
            }
            allReviews = _parentReviewHelper.interleaveReviews(overallReviews, topicalReviews);
            _parentReviewHelper.updateCounts(numReviewsBy, topicalReviews);
            // topical reviews cannot contain principal reviews
            numberOfNonPrincipalReviews += topicalReviews.size();

            _parentReviewHelper.handleLastModifiedDateInModel(model, school, reviewsBy, overallReviews, topicalReviews);
            _parentReviewHelper.handleSortAllReviews(sortBy, allReviews);

            cmd.setSortBy(sortBy);
            cmd.setSchool(school);
            cmd.setReviews(overallReviews); // TODO: What is this??
            cmd.setTotalReviews(numberOfNonPrincipalReviews.intValue());
            cmd.setCurrentDate(new Date());

            _parentReviewHelper.handleReviewReports(overallReviews, model, request);
            _parentReviewHelper.handleTopicalReviewReports(topicalReviews, model, request);
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
            int fromIndex = _parentReviewHelper.findFromIndex(page, MAX_NUMBER_OF_REVIEWS_PER_PAGE, allReviews);
            int toIndex = _parentReviewHelper.findToIndex(page, fromIndex, MAX_NUMBER_OF_REVIEWS_PER_PAGE, allReviews);

            // page param is invalid number -- too high or too low, so redirect to first page and preserve any request params
            List<ISchoolReview> reviewsToShow = new ArrayList<ISchoolReview>();
            if (allReviews.size()==0 || sessionContext.isCrawler() || StringUtils.isNotEmpty(request.getParameter(ParentReviewHelper.PARAM_VIEW_ALL))){
                reviewsToShow = allReviews;
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
                    // Done
                    reviewsToShow = _parentReviewHelper.handlePagination(request, allReviews, page, fromIndex, toIndex);
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

            // Check if the user is a ??? moderator and if so add it to the model so we can allow moderators for "Report" an issue multiple times
            User user = (User) model.get("validUser");
            if(user != null && PageHelper.isMemberAuthorized(request)){
                boolean moderatorRole = user.hasRole(Role.COMMUNITY_MODERATOR);
                if (moderatorRole) {
                    model.put("moderator", Boolean.TRUE);
                }
            }


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

    public ITopicalSchoolReviewDao getTopicalSchoolReviewDao() {
        return _topicalSchoolReviewDao;
    }

    public void setTopicalSchoolReviewDao(ITopicalSchoolReviewDao topicalSchoolReviewDao) {
        _topicalSchoolReviewDao = topicalSchoolReviewDao;
    }
}
