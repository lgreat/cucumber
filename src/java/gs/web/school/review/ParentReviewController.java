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
public class ParentReviewController extends AbstractController {

    public static final String BEAN_ID = "parentReviews";

    private IReviewDao _reviewDao;
    private String _viewName;
    private IReportedEntityDao _reportedEntityDao;
    private ISubscriptionDao _subscriptionDao;
    private SchoolProfileHeaderHelper _schoolProfileHeaderHelper;
    private ISchoolDao _schoolDao;
    private NearbySchoolsHelper _nearbySchoolsHelper;
    private RatingHelper _ratingHelper;
    private ILocalBoardDao _localBoardDao;
    private IGeoDao _geoDao;

    protected static final int MAX_REVIEWS_PER_PAGE = 4; //number of reviews per page
    protected static final String PARAM_PAGE = "page";
    protected static final String PARAM_SORT_BY = "sortBy";
    protected static final String PARAM_PAGER_OFFSET = "pager.offset";
    protected static final String PARAM_VIEW_ALL = "lr";
    protected static final String PARAM_REVIEWS_BY = "reviewsBy";
    protected static final String MODEL_URI = "uri";
    protected static final Log _log = LogFactory.getLog(ParentReviewController.class.getName());

    public static final String MODEL_LOCAL_BOARD_ID = "localBoardId";
    public static final String MODEL_LOCAL_CITY_NAME = "localCityName";
    public static final String MODEL_RYH_CONTENT_KEY = "ryhContentKey";

    //Compare on who, then overall rating descending, then date posted descending
    private static final Comparator<Review> PRINCIPAL_OVERALL_RATING_DESC_DATE_DESC =
            Review.createCompositeComparator(
                    Collections.reverseOrder(Review.POSTED_BY_PRINCIPAL_COMPARATOR),
                    Collections.reverseOrder(Review.OVERALL_RATING_COMPARATOR),
                    Collections.reverseOrder(Review.DATE_POSTED_COMPARATOR));

    //Compare on who, then overall rating ascending, then date posted descending
    private static final Comparator<Review> PRINCIPAL_OVERALL_RATING_ASC_DATE_DESC =
            Review.createCompositeComparator(
                    Collections.reverseOrder(Review.POSTED_BY_PRINCIPAL_COMPARATOR),
                    Review.OVERALL_RATING_COMPARATOR,
                    Collections.reverseOrder(Review.DATE_POSTED_COMPARATOR));

    //Compare on who, then date posted descending
    private static final Comparator<Review> PRINCIPAL_DATE_DESC =
            Review.createCompositeComparator(
                    Collections.reverseOrder(Review.POSTED_BY_PRINCIPAL_COMPARATOR),
                    Collections.reverseOrder(Review.DATE_POSTED_COMPARATOR));

    //compare on who, then date posted ascending
    private static final Comparator<Review> PRINCIPAL_DATE_ASC =
            Review.createCompositeComparator(
                    Collections.reverseOrder(Review.POSTED_BY_PRINCIPAL_COMPARATOR),
                    Review.DATE_POSTED_COMPARATOR);


    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String,Object> model = new HashMap<String,Object>();
        School school = (School) request.getAttribute(AbstractSchoolController.SCHOOL_ATTRIBUTE);
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);

        // Preschool profile pages should be hosted from pk.greatschools.org (GS-12127). Redirect if needed
        if (LevelCode.PRESCHOOL.equals(school.getLevelCode())) {
            RequestInfo hostnameInfo = (RequestInfo) request.getAttribute(RequestInfo.REQUEST_ATTRIBUTE_NAME);
            if (!hostnameInfo.isOnPkSubdomain() && hostnameInfo.isPkSubdomainSupported()) {
                UrlBuilder urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PARENT_REVIEWS);
                return new ModelAndView(new RedirectView301(urlBuilder.asFullUrl(request)));
            }
        }

        KindercareLeadGenHelper.checkForKindercare(request,response,school,model);

        boolean includeInactive = false;
        if (null != school) {
            SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
            User user = null;
            boolean isUserSubscribedToMss = false;
            if(PageHelper.isMemberAuthorized(request)){
                user = sessionContext.getUser();
                if (user != null) {
                    model.put("validUser", user);
                    Subscription sub = _subscriptionDao.findMssSubscriptionByUserAndSchool(user,school);
                    isUserSubscribedToMss = (sub != null) ? true : false;

                    if (user.hasPermission(Permission.COMMUNITY_VIEW_REPORTED_POSTS)) {
                        includeInactive = true;
                    }
                }
            }
            model.put("isUserSubscribedToMss", isUserSubscribedToMss);
            Set<Poster> reviewsBy = getReviewsBy(request.getParameter(PARAM_REVIEWS_BY));
            Map<Poster,Integer> numReviewsBy;

            List<Review> reviews;
            if (!includeInactive) {
                reviews = _reviewDao.getPublishedReviewsBySchool(school, reviewsBy);
                numReviewsBy = _reviewDao.getNumPublishedReviewsBySchool(school, reviewsBy);
            } else {
                reviews = _reviewDao.getPublishedDisabledReviewsBySchool(school, reviewsBy);
                numReviewsBy = _reviewDao.getNumPublishedDisabledReviewsBySchool(school, reviewsBy);
            }

            ParentReviewCommand cmd = new ParentReviewCommand();

            Ratings ratings = _reviewDao.findRatingsBySchool(school);            
            cmd.setRatings(ratings);

            /**
             * dd - date descending: newer to older
             * da - date ascending: older to newer
             * rd - overall rating descending: higher to lower
             * ra - overall rating ascending: lower to higher
             */
            String paramSortBy = request.getParameter(PARAM_SORT_BY);
            if ("da".equals(paramSortBy)) {
                Collections.sort(reviews, PRINCIPAL_DATE_ASC);
            } else if ("rd".equals(paramSortBy)) {
                Collections.sort(reviews, PRINCIPAL_OVERALL_RATING_DESC_DATE_DESC);
            } else if ("ra".equals(paramSortBy)) {
                Collections.sort(reviews, PRINCIPAL_OVERALL_RATING_ASC_DATE_DESC);
            } else {
                Collections.sort(reviews, PRINCIPAL_DATE_DESC);
                //is user passes in junk, set this as default
                paramSortBy = "dd";
            }

            Long numberOfNonPrincipalReviews = _reviewDao.countPublishedNonPrincipalReviewsBySchool(school, reviewsBy);
            cmd.setSortBy(paramSortBy);
            cmd.setSchool(school);
            cmd.setReviews(reviews);
            cmd.setTotalReviews(numberOfNonPrincipalReviews.intValue());
            cmd.setCurrentDate(new Date());

            if(user != null && PageHelper.isMemberAuthorized(request)){
                model.put("reviewReports", getReportsForReviews(user, reviews));
            }

            if (user == null) {
                String uri = request.getRequestURI();
                uri = UrlUtil.addParameter(uri, "id=" + school.getId());
                uri = UrlUtil.addParameter(uri, "state=" + school.getDatabaseState());
                UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, null, uri);
                model.put("loginRedirectUrl", urlBuilder.asSiteRelative(request));
            } else {
                model.put("loginRedirectUrl", "#");                
            }

            if (numReviewsBy != null) {
                model.put("numParentReviews", numReviewsBy.get(Poster.PARENT));
                model.put("numStudentReviews", numReviewsBy.get(Poster.STUDENT));

                int numTeacherStaffReviews = 0;
                Integer numTeacherReviews = numReviewsBy.get(Poster.TEACHER);
                if (numTeacherReviews != null) {
                    numTeacherStaffReviews += numTeacherReviews;
                }
                Integer numStaffReviews = numReviewsBy.get(Poster.STAFF);
                if (numStaffReviews != null) {
                    numTeacherStaffReviews += numStaffReviews;
                }
                Integer numAdminReviews = numReviewsBy.get(Poster.ADMINISTRATOR);
                if (numAdminReviews != null) {
                    numTeacherStaffReviews += numAdminReviews;
                }

                model.put("numTeacherStaffReviews", numTeacherStaffReviews);
            }
            model.put("reviewsByCsv", getReviewsByCsv(reviewsBy));
            model.put(MODEL_URI, request.getRequestURI());

            model.put("reviewsTotalPages", getReviewsTotalPages(numberOfNonPrincipalReviews.intValue()));
            model.put("param_reviewsby", PARAM_REVIEWS_BY);

            processSubcategoryRatings(model, school, ratings);

            // reviews to show
            List<Review> reviewsToShow;
            int page = 1;
            if (reviews.size() == 0 || sessionContext.isCrawler() || StringUtils.isNotEmpty(request.getParameter(PARAM_VIEW_ALL))) {
                reviewsToShow = reviews;
            } else {
                String pageParam = request.getParameter(PARAM_PAGE);
                if (pageParam != null) {
                    try {
                        page = Integer.parseInt(pageParam);
                    } catch (Exception e) {
                        // do nothing
                    }
                }
                int fromIndex = (page - 1) * MAX_REVIEWS_PER_PAGE;
                int toIndex = fromIndex + MAX_REVIEWS_PER_PAGE;

                if ("principal".equals(reviews.get(0).getWho())) {
                    fromIndex++;
                    toIndex++;
                }

                toIndex = Math.min(toIndex, reviews.size());

                if (fromIndex >= toIndex || fromIndex < 0) {
                    String queryString = request.getQueryString();
                    if (queryString != null) {
                        queryString = UrlUtil.putQueryParamIntoQueryString(queryString, "page", "1");
                    }
                    return new ModelAndView(new RedirectView(request.getRequestURI() + (queryString != null ? "?" + queryString : "")));
                }

                reviewsToShow = reviews.subList(fromIndex, toIndex);
            }
            model.put("reviewsToShow", reviewsToShow);
            model.put("page", page);
            model.put("reviewsFilterSortTracking", getReviewsFilterSortTracking(cmd.getTotalReviews(), reviewsBy, paramSortBy));

            // GS-10709
            UrlBuilder builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PARENT_REVIEWS);
            model.put("relCanonical", builder.asFullUrlXml(request));

            // GS-10633
            processOverallRatingsByYear(model, school);

            model.put("cmd", cmd);
            model.put("param_sortby", PARAM_SORT_BY);

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

    private static String getReviewsByCsv(Set<Poster> reviewsBy) {
        int numMatched = 0;
        StringBuilder s = new StringBuilder();
        if (reviewsBy.contains(Poster.PARENT)) {
            s.append("p");
            numMatched++;
        }
        if (reviewsBy.contains(Poster.STUDENT)) {
            if (s.length() > 0) {
                s.append(",");
            }
            s.append("s");
            numMatched++;
        }
        if (reviewsBy.contains(Poster.TEACHER) || reviewsBy.contains(Poster.STAFF) ||
            reviewsBy.contains(Poster.ADMINISTRATOR)) {
            if (s.length() > 0) {
                s.append(",");
            }
            s.append("t");
            numMatched++;
        }

        if (numMatched == 0) {
            return null;
        }

        return s.toString();
    }

    private static Set<Poster> getReviewsBy(String paramReviewsBy) {
        Set<Poster> reviewsBy = new HashSet<Poster>();
        /**
         * p - parents
         * s - students
         * t - teachers/staff
         * a - all
         */
        if (StringUtils.isBlank(paramReviewsBy)) {
            paramReviewsBy = "a";
        }
        String[] reviewsByArr = StringUtils.split(paramReviewsBy, ",");
        Set<String> reviewsBySet = new HashSet<String>(Arrays.asList(reviewsByArr));

        if (reviewsBySet.contains("a")) {
            return reviewsBy;
        }

        if (reviewsBySet.contains("p")) {
            reviewsBy.add(Poster.PARENT);
        }
        if (reviewsBySet.contains("s")) {
            reviewsBy.add(Poster.STUDENT);
        }
        if (reviewsBySet.contains("t")) {
            reviewsBy.add(Poster.TEACHER);
            reviewsBy.add(Poster.STAFF);
            reviewsBy.add(Poster.ADMINISTRATOR);
        }

        return reviewsBy;
    }

    private Map<Integer, Boolean> getReportsForReviews(User user, List<Review> reviews) {
        if (reviews == null || user == null) {
            return null;
        }
        Map<Integer, Boolean> reports = new HashMap<Integer, Boolean>(reviews.size());
        for (Review review: reviews) {
            reports.put(review.getId(),
                        _reportedEntityDao.hasUserReportedEntity
                                (user, ReportedEntity.ReportedEntityType.schoolReview, review.getId()));
        }
        return reports;
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

}
