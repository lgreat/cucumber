package gs.web.school.review;

import gs.data.community.IReportedEntityDao;
import gs.data.community.ReportedEntity;
import gs.data.community.User;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.NearbySchool;
import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Poster;
import gs.data.school.review.Ratings;
import gs.data.school.review.Review;
import gs.data.security.Permission;
import gs.data.test.SchoolTestValue;
import gs.data.test.TestManager;
import gs.data.test.rating.IRatingsConfig;
import gs.data.test.rating.IRatingsConfigDao;
import gs.data.util.NameValuePair;
import gs.web.school.*;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    private SchoolProfileHeaderHelper _schoolProfileHeaderHelper;
    private ISchoolDao _schoolDao;
    private NearbySchoolsHelper _nearbySchoolsHelper;
    private RatingHelper _ratingHelper;

    protected static final int MAX_REVIEWS_PER_PAGE = 4; //number of reviews per page
    protected static final String PARAM_PAGE = "page";
    protected static final String PARAM_SORT_BY = "sortBy";
    protected static final String PARAM_PAGER_OFFSET = "pager.offset";
    protected static final String PARAM_VIEW_ALL = "lr";
    protected static final String PARAM_REVIEWS_BY = "reviewsBy";
    protected static final String PARAM_PREV_REVIEWS_BY = "prb";
    protected static final String PARAM_PREV_SORT_BY = "psb";
    protected static final String MODEL_URI = "uri";
    protected static final Log _log = LogFactory.getLog(ParentReviewController.class.getName());

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

        KindercareLeadGenHelper.checkForKindercare(request,response,school,model);

        boolean includeInactive = false;
        if (null != school) {
            SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
            User user = null;
            if(PageHelper.isMemberAuthorized(request)){
                user = sessionContext.getUser();
                if (user != null) {
                    model.put("validUser", user);
                    if (user.hasPermission(Permission.COMMUNITY_VIEW_REPORTED_POSTS)) {
                        includeInactive = true;
                    }
                }
            }

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

            if (sessionContext.isShowReviewsPageRedesign()) {
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


                // TODO-10495
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

                    toIndex = Math.min(toIndex, reviews.size() - 1);

                    reviewsToShow = reviews.subList(fromIndex, toIndex);
                }
                model.put("reviewsToShow", reviewsToShow);
                model.put("page", page);

                if (page == 1) {
                    cmd.setShowParentReviewForm(true);
                } else {
                    cmd.setShowParentReviewForm(false);
                }

                String prevSortBy = request.getParameter(PARAM_PREV_SORT_BY);
                Set<Poster> prevReviewsBy = getReviewsBy(request.getParameter(PARAM_PREV_REVIEWS_BY));

                model.put("reviewsFilterSortTracking", getReviewsFilterSortTracking(reviewsBy, prevReviewsBy, paramSortBy, prevSortBy));

                // GS-10709
                UrlBuilder builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PARENT_REVIEWS);
                model.put("relCanonical", builder.asFullUrlXml(request));
            } else {
                if (sessionContext.isCrawler() || StringUtils.isNotEmpty(request.getParameter(PARAM_VIEW_ALL))) {
                    cmd.setMaxReviewsPerPage(reviews.size());
                } else {
                    cmd.setMaxReviewsPerPage(MAX_REVIEWS_PER_PAGE);
                }

                String pagerOffset = request.getParameter(PARAM_PAGER_OFFSET);
                if (StringUtils.isBlank(pagerOffset) || "0".equals(pagerOffset)) {
                    cmd.setShowParentReviewForm(true);
                } else {
                    cmd.setShowParentReviewForm(false);
                }
            }

            model.put("cmd", cmd);
            model.put("param_sortby", PARAM_SORT_BY);

            List<NearbySchool> nearbySchools = getSchoolDao().findNearbySchools(school, 20);
            request.setAttribute("mapSchools", getNearbySchoolsHelper().getRatingsForNearbySchools(nearbySchools));

            boolean useCache = (null != pageHelper && pageHelper.isDevEnvironment() && !pageHelper.isStagingServer());

            Integer gsRating = getRatingHelper().getGreatSchoolsOverallRating(school, useCache);

            model.put("gs_rating", gsRating);

            _schoolProfileHeaderHelper.updateModel(request, response, school, model);
        }
        return new ModelAndView(getViewName(), model);
    }

    // TODO-10495
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

    private String getReviewsFilterSortTracking(Set<Poster> reviewsBy, Set<Poster> prevReviewsBy, String paramSortBy, String prevSortBy) {
        String reviewsFilterSortTracking = "";
        if (StringUtils.isNotBlank(prevSortBy)) {
            if ("da".equals(paramSortBy) && !"da".equals(prevSortBy)) {
                reviewsFilterSortTracking = "Oldest first";
            } else if ("rd".equals(paramSortBy) && !"rd".equals(prevSortBy)) {
                reviewsFilterSortTracking = "Highest rating first";
            } else if ("ra".equals(paramSortBy) && !"ra".equals(prevSortBy)) {
                reviewsFilterSortTracking = "Lowest rating first";
            } else if ("dd".equals(paramSortBy) && !"dd".equals(prevSortBy)) {
                reviewsFilterSortTracking = "Newest first";
            }
        } else if (prevReviewsBy != null) {
            if (reviewsBy.contains(Poster.PARENT) && !prevReviewsBy.contains(Poster.PARENT)) {
                reviewsFilterSortTracking = "Parent review";
            } else if (reviewsBy.contains(Poster.STUDENT) && !prevReviewsBy.contains(Poster.STUDENT)) {
                reviewsFilterSortTracking = "Student review";
            } else if (reviewsBy.contains(Poster.TEACHER) && !prevReviewsBy.contains(Poster.TEACHER)) {
                // not checking staff and admin, for simplicity; it would be redundant
                reviewsFilterSortTracking = "Teacher review";
            }
        }
        return reviewsFilterSortTracking;
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
        private int _maxReviewsPerPage;
        private boolean _showParentReviewForm;

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

        public int getMaxReviewsPerPage() {
            return _maxReviewsPerPage;
        }

        public void setMaxReviewsPerPage(int maxReviewsPerPage) {
            _maxReviewsPerPage = maxReviewsPerPage;
        }

        public boolean isShowParentReviewForm() {
            return _showParentReviewForm;
        }

        public void setShowParentReviewForm(boolean showParentReviewForm) {
            _showParentReviewForm = showParentReviewForm;
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
}
