package gs.web.school.review;

import gs.data.community.*;
import gs.data.community.local.ILocalBoardDao;
import gs.data.community.local.LocalBoard;
import gs.data.content.cms.CmsConstants;
import gs.data.content.cms.ContentKey;
import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.NearbySchool;
import gs.data.school.School;
import gs.data.school.review.*;
import gs.data.security.Permission;
import gs.data.util.NameValuePair;
import gs.web.ControllerFamily;
import gs.web.IControllerFamilySpecifier;
import gs.web.request.RequestInfo;
import gs.web.school.*;
import gs.web.util.PageHelper;
import gs.web.util.RedirectView301;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Parent review page
 *
 * @author <a href="mailto:dlee@greatschools.org">David Lee</a>
 */
public class ParentReviewController extends AbstractController implements IControllerFamilySpecifier {

    public static final String BEAN_ID = "desktopParentReviewsController";

    private IReviewDao _reviewDao;
    private String _viewName;
    private IReportedEntityDao _reportedEntityDao;
    private ISubscriptionDao _subscriptionDao;
    private SchoolProfileHeaderHelper _schoolProfileHeaderHelper;
    private ISchoolDao _schoolDao;
    private NearbySchoolsHelper _nearbySchoolsHelper;
    private RatingHelper _ratingHelper;
    private ILocalBoardDao _localBoardDao;
    private ParentReviewHelper _parentReviewHelper;
    private IGeoDao _geoDao;

    private ControllerFamily _controllerFamily;

    protected static final Log _log = LogFactory.getLog(ParentReviewController.class.getName());

    public static final String MODEL_LOCAL_BOARD_ID = "localBoardId";
    public static final String MODEL_LOCAL_CITY_NAME = "localCityName";
    public static final String MODEL_RYH_CONTENT_KEY = "ryhContentKey";


    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String,Object> model = new HashMap<String,Object>();
        School school = (School) request.getAttribute(AbstractSchoolController.SCHOOL_ATTRIBUTE);
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);

        // Preschool profile pages should be hosted from pk.greatschools.org (GS-12127). Redirect if needed
        if (LevelCode.PRESCHOOL.equals(school.getLevelCode())) {
            RequestInfo hostnameInfo = (RequestInfo) request.getAttribute(RequestInfo.REQUEST_ATTRIBUTE_NAME);
            if (!hostnameInfo.isOnPkSubdomain() && hostnameInfo.isPkSubdomainSupported()) {
                UrlBuilder urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PARENT_REVIEWS);
                urlBuilder.removeParameter("state");
                urlBuilder.removeParameter("id");
                urlBuilder.addParametersFromRequest(request);
                return new ModelAndView(new RedirectView301(urlBuilder.asFullUrl(request)));
            }
        }

        KindercareLeadGenHelper.checkForKindercare(request,response,school,model);

        if (null != school) {
            SessionContext sessionContext = SessionContextUtil.getSessionContext(request);

            boolean includeInactive = _parentReviewHelper.handleMssUser(school, sessionContext, request, model);

            Set<Poster> reviewsBy = _parentReviewHelper.handleGetReviewsBy(request.getParameter(ParentReviewHelper.PARAM_REVIEWS_BY));
            Map<Poster,Integer> numReviewsBy;

            List<Review> reviews;
            if (!includeInactive) {
                reviews = _reviewDao.getPublishedReviewsBySchool(school, reviewsBy);
                numReviewsBy = _reviewDao.getNumPublishedReviewsBySchool(school, reviewsBy);
            } else {
                reviews = _reviewDao.getPublishedDisabledReviewsBySchool(school, reviewsBy);
                numReviewsBy = _reviewDao.getNumPublishedDisabledReviewsBySchool(school, reviewsBy);
            }

            ParentReviewHelper.ParentReviewCommand cmd = new ParentReviewHelper.ParentReviewCommand();

            Ratings ratings = _reviewDao.findRatingsBySchool(school);            
            cmd.setRatings(ratings);

            _parentReviewHelper.handleLastModifiedDateInModel(model, school, reviewsBy, reviews);

            String paramSortBy = request.getParameter(ParentReviewHelper.PARAM_SORT_BY);
            _parentReviewHelper.handleSortReviews(paramSortBy, reviews);

            Long numberOfNonPrincipalReviews = _parentReviewHelper.handleNumberOfNonPrincipalReviews(school, reviewsBy, includeInactive);
            cmd.setSortBy(paramSortBy);
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

            model.put("reviewsTotalPages", _parentReviewHelper.getReviewsTotalPages(numberOfNonPrincipalReviews.intValue()));
            model.put("param_reviewsby", ParentReviewHelper.PARAM_REVIEWS_BY);

            _parentReviewHelper.handleSubcategoryRatings(model, school, ratings);

            int page = _parentReviewHelper.findCurrentPage(request);
            int fromIndex = _parentReviewHelper.findFromIndex(page, reviews);
            int toIndex = _parentReviewHelper.findToIndex(page, fromIndex, reviews);

            // page param is invalid number -- too high or too low, so redirect to first page and preserve any request params
            List<Review> reviewsToShow = new ArrayList<Review>();
            if (reviews.size()==0 || sessionContext.isCrawler() || StringUtils.isNotEmpty(request.getParameter(ParentReviewHelper.PARAM_VIEW_ALL))){
                reviewsToShow = reviews;
            }
            else {
                if (fromIndex >= toIndex || fromIndex < 0) {
                    String queryString = request.getQueryString();
                    if (queryString != null) {
                        queryString = UrlUtil.putQueryParamIntoQueryString(queryString, "page", "1");
                    }
                    return new ModelAndView(new RedirectView(request.getRequestURI() + (queryString != null ? "?" + queryString : "")));
                }

                reviewsToShow = _parentReviewHelper.handlePagination(request, reviews, page, fromIndex, toIndex);
            }
            model.put("reviewsToShow", reviewsToShow);
            model.put("page", page);
            model.put("reviewsFilterSortTracking", _parentReviewHelper.handleReviewsFilterSortTracking(cmd.getTotalReviews(), reviewsBy, paramSortBy));

            // GS-10709
            UrlBuilder builder = new UrlBuilder(school, page, UrlBuilder.SCHOOL_PARENT_REVIEWS);
            model.put("relCanonical", builder.asFullUrlXml(request));

            // GS-10633
            _parentReviewHelper.handleOverallRatingsByYear(model, school);

            model.put("cmd", cmd);
            model.put("param_sortby", ParentReviewHelper.PARAM_SORT_BY);

            List<NearbySchool> nearbySchools = getSchoolDao().findNearbySchools(school, 20);
            request.setAttribute("mapSchools", getNearbySchoolsHelper().getRatingsForNearbySchools(nearbySchools));

            boolean useCache = (null != pageHelper && pageHelper.isDevEnvironment() && !pageHelper.isStagingServer());

            Integer gsRating = getRatingHelper().getGreatSchoolsOverallRating(school, useCache);

            model.put("gs_rating", gsRating);
            model.put("ratings", ratings);
            _schoolProfileHeaderHelper.updateModel(request, response, school, model);

            // GS-10629
            if (StringUtils.isNotBlank(school.getCity()) && school.getDatabaseState() != null) {
                City localCity = _geoDao.findCity(school.getDatabaseState(), school.getCity());
                if (localCity != null) {
                    LocalBoard localBoard = _localBoardDao.findByCityId(localCity.getId());
                    if (localBoard != null) {
                        model.put(MODEL_LOCAL_BOARD_ID, localBoard.getBoardId());
                        model.put(MODEL_LOCAL_CITY_NAME, localCity.getName());
                    } else {
                        LevelCode levelCode = school.getLevelCode();
                        if (levelCode != null) {
                            ContentKey ryhContentKey = null;
                            if (levelCode.equals(LevelCode.PRESCHOOL)) {
                                ryhContentKey = new ContentKey(CmsConstants.TOPIC_CENTER_CONTENT_TYPE, CmsConstants.PRESCHOOL_TOPIC_CENTER_ID);
                            } else if (levelCode.containsLevelCode(LevelCode.Level.ELEMENTARY_LEVEL)) {
                                ryhContentKey = new ContentKey(CmsConstants.TOPIC_CENTER_CONTENT_TYPE, CmsConstants.ELEMENTARY_SCHOOL_TOPIC_CENTER_ID);
                            } else if (levelCode.containsLevelCode(LevelCode.Level.MIDDLE_LEVEL)) {
                                ryhContentKey = new ContentKey(CmsConstants.TOPIC_CENTER_CONTENT_TYPE, CmsConstants.MIDDLE_SCHOOL_TOPIC_CENTER_ID);
                            } else if (levelCode.containsLevelCode(LevelCode.Level.HIGH_LEVEL)) {
                                ryhContentKey = new ContentKey(CmsConstants.TOPIC_CENTER_CONTENT_TYPE, CmsConstants.HIGH_SCHOOL_TOPIC_CENTER_ID);
                            }

                            if (ryhContentKey != null) {
                                model.put(MODEL_RYH_CONTENT_KEY, ryhContentKey);
                            }
                        }
                    }
                }
            }

        }
        return new ModelAndView(getViewName(), model);
    }

    /**
     * GS-12750
     * Get the most recent of these two dates: school.getModified(), and the most recent published non-principal review
     * Use date of most recent non-principal review instead of any review to stay consistent with last-modified
     * date shown on school overview page.
     * Assumption: reviews list is ORDER BY POSTED DESC. technically, it's fragile to assume this, but the
     *   last-modified date is not critical. If we do have to change that order by in reviewDao, then we could
     *   simply sort reviews in Java using comparator PRINCIPAL_DATE_DESC. opted not to do this even though it
     *   might be more reliable, strictly for performance reasons
     * @param model model for view
     * @param school school for which we want the last-modified date
     * @param reviewsBy who we're filtering reviews by
     * @param reviews list of reviews
     */
    private static void setLastModifiedDateInModel(Map<String, Object> model, School school, Set<Poster> reviewsBy, List<Review> reviews) {
        Date lastModifiedDate = school.getModified();
        int numReviews = reviews.size();
        // ignore most recently posted review if no reviews or if we're filtering what kind of reviewer we're including reviews by
        if (numReviews > 0 && reviewsBy.isEmpty()) {
            int i = 0;
            // skip principal or non-published reviews
            while (i < numReviews && ("principal".equals(reviews.get(i).getWho()) ||
                    !Review.ReviewStatus.PUBLISHED.getStatusCode().equals(reviews.get(i).getStatus()))) {
                i++;
            }
            // the ith review might still be principal or non-published, so only use the date if that's not the case
            // the index i might also be out of bounds if there were no non-principal, published reviews,
            // so check for that too
            Date mostRecentPublishedNonPrincipalReview = null;
            if (i < numReviews && !"principal".equals(reviews.get(i).getWho()) &&
                Review.ReviewStatus.PUBLISHED.getStatusCode().equals(reviews.get(i).getStatus())) {
                mostRecentPublishedNonPrincipalReview = reviews.get(i).getPosted();
            }

            // get most recent of school modified date and most recent published non-principal review
            if (lastModifiedDate == null ||
                (mostRecentPublishedNonPrincipalReview != null &&
                 lastModifiedDate.compareTo(mostRecentPublishedNonPrincipalReview) < 0)) {
                lastModifiedDate = mostRecentPublishedNonPrincipalReview;
            }
        }

        if (lastModifiedDate != null) {
            model.put("lastModifiedDate", lastModifiedDate);
        }
    }

    // GS-10633
    private void processOverallRatingsByYear(Map<String, Object> model, School school) {
        // ** The hover includes the average overall Community rating for
        // the current calendar year and the previous 3 calendar years.
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);

        SortedSet<RatingsForYear> ratingsByYear = _reviewDao.findOverallRatingsByYear(school, currentYear, 4);

        int numRatings = 0;
        for (RatingsForYear ratingsForYear : ratingsByYear) {
            numRatings += ratingsForYear.getNumRatings();
        }

        // The hover should NOT appear if there are no ratings published for the school in
        // the current calendar year or the previous 3 calendar years
        if (numRatings > 0) {
            model.put("ratingsByYear", ratingsByYear);
        }
    }

    final private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private static int getRatingsByYearCurrentYear(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);

        if (month < Calendar.MARCH) {
            return year;
        }
        return year - 1;
    }

    // GS-10495
    private void processSubcategoryRatings(Map<String, Object> model, School school, Ratings ratings) {
        // blue box subcategory ratings
        List<NameValuePair<String, Integer>> communityRatings = new ArrayList<NameValuePair<String, Integer>>();
        List<NameValuePair<String, Integer>> parentRatings = new ArrayList<NameValuePair<String, Integer>>();
        List<NameValuePair<String, Integer>> studentRatings = new ArrayList<NameValuePair<String, Integer>>();

        NameValuePair<String, Integer> pair;

        // State 1 (p-only) There is no "Parent ratings | Student ratings" toggle.
        if (ratings.hasPreschool()) {
            addNameValueToRatingsList("Teacher quality", ratings.getAvgP_Teachers(), communityRatings);
            addNameValueToRatingsList("Parent involvement", ratings.getAvgP_Parents(), communityRatings);
            addNameValueToRatingsList("Facilities & equipment", ratings.getAvgP_Facilities(), communityRatings);
        } else if (ratings.hasGradeschool()) {
            Ratings parentRatingsObj = _reviewDao.findRatingsBySchool(school,"parent");
            Ratings studentRatingsObj = _reviewDao.findRatingsBySchool(school,"student");

            if (school.getLevelCode().containsLevelCode(LevelCode.Level.HIGH_LEVEL) &&
                parentRatingsObj.hasGradeschool() && studentRatingsObj.hasGradeschool()) {
                addNameValueToRatingsList("Overall rating", parentRatingsObj.getOverall(), parentRatings);
                addNameValueToRatingsList("Teacher quality", parentRatingsObj.getAvgTeachers(), parentRatings);
                addNameValueToRatingsList("Principal leadership", parentRatingsObj.getAvgPrincipal(), parentRatings);
                addNameValueToRatingsList("Parent involvement", parentRatingsObj.getAvgParents(), parentRatings);

                addNameValueToRatingsList("Overall rating", studentRatingsObj.getOverall(), studentRatings);
                addNameValueToRatingsList("Teacher quality", studentRatingsObj.getAvgTeachers(), studentRatings);
            } else {
                addNameValueToRatingsList("Teacher quality", ratings.getAvgTeachers(), communityRatings);
                addNameValueToRatingsList("Principal leadership", ratings.getAvgPrincipal(), communityRatings);
                addNameValueToRatingsList("Parent involvement", ratings.getAvgParents(), communityRatings);
            }
        }

        model.put("communityRatings", communityRatings);
        model.put("parentRatings", parentRatings);
        model.put("studentRatings", studentRatings);
    }

    private void addNameValueToRatingsList(String displayString, Integer numStars, List<NameValuePair<String, Integer>> communityRatings) {
        NameValuePair<String, Integer> pair;
        if (numStars != null) {
            pair = new NameValuePair(displayString, numStars);
            communityRatings.add(pair);
        }
    }

    protected static String getReviewsFilterSortTracking(int numReviews, Set<Poster> reviewsBy, String paramSortBy) {
        if (numReviews == 0) {
            return "";
        }

        StringBuilder reviewsFilterSortTracking = new StringBuilder();

        if ("da".equals(paramSortBy)) {
            reviewsFilterSortTracking.append("Oldest first");
        } else if ("rd".equals(paramSortBy)) {
            reviewsFilterSortTracking.append("Highest rating first");
        } else if ("ra".equals(paramSortBy)) {
            reviewsFilterSortTracking.append("Lowest rating first");
        }

        if (reviewsBy != null) {
            if (reviewsBy.contains(Poster.PARENT)) {
                if (reviewsFilterSortTracking.length() > 0) {
                    reviewsFilterSortTracking.append(",");
                }
                reviewsFilterSortTracking.append("Parent review");
            }
            if (reviewsBy.contains(Poster.STUDENT)) {
                if (reviewsFilterSortTracking.length() > 0) {
                    reviewsFilterSortTracking.append(",");
                }
                reviewsFilterSortTracking.append("Student review");
            }
            if (reviewsBy.contains(Poster.TEACHER)) {
                // not checking staff and admin, for simplicity; it would be redundant
                if (reviewsFilterSortTracking.length() > 0) {
                    reviewsFilterSortTracking.append(",");
                }
                reviewsFilterSortTracking.append("Teacher review");
            }
        }

        if (reviewsFilterSortTracking.length() == 0) {
            return "Default";
        }

        return reviewsFilterSortTracking.toString();
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

    public IReportedEntityDao getReportedEntityDao() {
        return _reportedEntityDao;
    }

    public void setReportedEntityDao(IReportedEntityDao reportedEntityDao) {
        _reportedEntityDao = reportedEntityDao;
    }

    public SchoolProfileHeaderHelper getSchoolProfileHeaderHelper() {
        return _schoolProfileHeaderHelper;
    }

    public void setSchoolProfileHeaderHelper(SchoolProfileHeaderHelper schoolProfileHeaderHelper) {
        _schoolProfileHeaderHelper = schoolProfileHeaderHelper;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public NearbySchoolsHelper getNearbySchoolsHelper() {
        return _nearbySchoolsHelper;
    }

    public void setNearbySchoolsHelper(NearbySchoolsHelper nearbySchoolsHelper) {
        _nearbySchoolsHelper = nearbySchoolsHelper;
    }

    public RatingHelper getRatingHelper() {
        return _ratingHelper;
    }

    public void setRatingHelper(RatingHelper ratingHelper) {
        _ratingHelper = ratingHelper;
    }

    public ILocalBoardDao getLocalBoardDao() {
        return _localBoardDao;
    }

    public void setLocalBoardDao(ILocalBoardDao localBoardDao) {
        _localBoardDao = localBoardDao;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }

    public ParentReviewHelper getParentReviewHelper() {
        return _parentReviewHelper;
    }

    public void setParentReviewHelper(ParentReviewHelper parentReviewHelper) {
        _parentReviewHelper = parentReviewHelper;
    }

    public ControllerFamily getControllerFamily() {
        return _controllerFamily;
    }

    public void setControllerFamily(ControllerFamily controllerFamily) {
        _controllerFamily = controllerFamily;
    }
}
