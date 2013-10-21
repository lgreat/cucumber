package gs.web.school.review;

import gs.data.community.*;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.review.*;
import gs.data.security.Permission;
import gs.data.util.NameValuePair;
import gs.web.request.RequestInfo;
import gs.web.util.PageHelper;
import gs.web.util.RedirectView301;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * User: cauer
 */
public class ParentReviewHelper {

    public static final int MAX_REVIEWS_PER_PAGE = 4; //number of reviews per page
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_SORT_BY = "sortBy";
    public static final String PARAM_PAGER_OFFSET = "pager.offset";
    public static final String PARAM_VIEW_ALL = "lr";
    public static final String PARAM_REVIEWS_BY = "reviewsBy";
    public static final String MODEL_URI = "uri";



    private IReportedEntityDao _reportedEntityDao;
    private IReviewDao _reviewDao;
    private ISubscriptionDao _subscriptionDao;

    //Compare on who, then overall rating descending, then date posted descending
    public static final Comparator<Review> PRINCIPAL_OVERALL_RATING_DESC_DATE_DESC =
            Review.createCompositeComparator(
                    Collections.reverseOrder(Review.POSTED_BY_PRINCIPAL_COMPARATOR),
                    Collections.reverseOrder(Review.OVERALL_RATING_COMPARATOR),
                    Collections.reverseOrder(Review.DATE_POSTED_COMPARATOR));

    //Compare on who, then overall rating ascending, then date posted descending
    public static final Comparator<Review> PRINCIPAL_OVERALL_RATING_ASC_DATE_DESC =
            Review.createCompositeComparator(
                    Collections.reverseOrder(Review.POSTED_BY_PRINCIPAL_COMPARATOR),
                    Review.OVERALL_RATING_COMPARATOR,
                    Collections.reverseOrder(Review.DATE_POSTED_COMPARATOR));

    //Compare on who, then date posted descending
    public static final Comparator<Review> PRINCIPAL_DATE_DESC =
            Review.createCompositeComparator(
                    Collections.reverseOrder(Review.POSTED_BY_PRINCIPAL_COMPARATOR),
                    Collections.reverseOrder(Review.DATE_POSTED_COMPARATOR));

    //compare on who, then date posted ascending
    public static final Comparator<Review> PRINCIPAL_DATE_ASC =
            Review.createCompositeComparator(
                    Collections.reverseOrder(Review.POSTED_BY_PRINCIPAL_COMPARATOR),
                    Review.DATE_POSTED_COMPARATOR);

    //Compare on who, then overall rating descending, then date posted descending
    public static final Comparator<ISchoolReview> INTERLEAVED_PRINCIPAL_OVERALL_RATING_DESC_DATE_DESC =
            Review.createCompositeComparator(
                    Collections.reverseOrder(SchoolReviewAdapter.POSTED_BY_PRINCIPAL_COMPARATOR),
                    Collections.reverseOrder(SchoolReviewAdapter.OVERALL_RATING_COMPARATOR),
                    Collections.reverseOrder(SchoolReviewAdapter.DATE_POSTED_COMPARATOR));

    //Compare on who, then overall rating ascending, then date posted descending
    public static final Comparator<ISchoolReview> INTERLEAVED_PRINCIPAL_OVERALL_RATING_ASC_DATE_DESC =
            Review.createCompositeComparator(
                    Collections.reverseOrder(SchoolReviewAdapter.POSTED_BY_PRINCIPAL_COMPARATOR),
                    SchoolReviewAdapter.OVERALL_RATING_COMPARATOR,
                    Collections.reverseOrder(SchoolReviewAdapter.DATE_POSTED_COMPARATOR));

    //Compare on who, then date posted descending
    public static final Comparator<ISchoolReview> INTERLEAVED_PRINCIPAL_DATE_DESC =
            Review.createCompositeComparator(
                    Collections.reverseOrder(SchoolReviewAdapter.POSTED_BY_PRINCIPAL_COMPARATOR),
                    Collections.reverseOrder(SchoolReviewAdapter.DATE_POSTED_COMPARATOR));

    //compare on who, then date posted ascending
    public static final Comparator<ISchoolReview> INTERLEAVED_PRINCIPAL_DATE_ASC =
            Review.createCompositeComparator(
                    Collections.reverseOrder(SchoolReviewAdapter.POSTED_BY_PRINCIPAL_COMPARATOR),
                    SchoolReviewAdapter.DATE_POSTED_COMPARATOR);


    /**
     * Handle preschool redirect if necessary by returning
     * a model object
     * @param request
     * @param school
     * @return
     */
    public ModelAndView handlePreschool(HttpServletRequest request, School school){
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
        return null;
    }

    public boolean handleMssUser (School school, SessionContext sessionContext, HttpServletRequest request, Map<String,Object> model) {
        User user = null;
        boolean includeInactive = false;
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
        return includeInactive;
    }

    public int getReviewsTotalPages(int numReviews) {
        return getReviewsTotalPages(numReviews, MAX_REVIEWS_PER_PAGE);
    }

    public int getReviewsTotalPages(int numReviews, int max) {
        int totalPages;
        if (numReviews > 0) {
            totalPages = numReviews / max;
            if (numReviews % max > 0) {
                totalPages++;
            }
        } else {
            totalPages = 1;
        }
        return totalPages;
    }

    public Set<Poster> handleGetReviewsBy(String paramReviewsBy) {
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

    public void handleGetReviewsByCsv(Set<Poster> reviewsBy, Map model){
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

        if (numMatched != 0) {
            model.put("reviewsByCsv", s.toString());
        } else {
            model.put("reviewsByCsv", null);
        }
    }

    /**
     * Sort the list of reviews based on the request param
     * @param paramSortBy
     * @param reviews
     * @return String
     */
    public String handleSortReviews(String paramSortBy, List<Review> reviews){
        if ("da".equals(paramSortBy)) {
            Collections.sort(reviews, ParentReviewHelper.PRINCIPAL_DATE_ASC);
        } else if ("rd".equals(paramSortBy)) {
            Collections.sort(reviews, ParentReviewHelper.PRINCIPAL_OVERALL_RATING_DESC_DATE_DESC);
        } else if ("ra".equals(paramSortBy)) {
            Collections.sort(reviews, ParentReviewHelper.PRINCIPAL_OVERALL_RATING_ASC_DATE_DESC);
        } else {
            Collections.sort(reviews, ParentReviewHelper.PRINCIPAL_DATE_DESC);
            //is user passes in junk, set this as default
            paramSortBy = "dd";
        }
        return paramSortBy;
    }

    public String handleSortAllReviews(String paramSortBy, List<ISchoolReview> reviews){
        if ("da".equals(paramSortBy)) {
            Collections.sort(reviews, ParentReviewHelper.INTERLEAVED_PRINCIPAL_DATE_ASC);
        } else if ("rd".equals(paramSortBy)) {
            Collections.sort(reviews, ParentReviewHelper.INTERLEAVED_PRINCIPAL_OVERALL_RATING_DESC_DATE_DESC);
        } else if ("ra".equals(paramSortBy)) {
            Collections.sort(reviews, ParentReviewHelper.INTERLEAVED_PRINCIPAL_OVERALL_RATING_ASC_DATE_DESC);
        } else {
            Collections.sort(reviews, ParentReviewHelper.INTERLEAVED_PRINCIPAL_DATE_DESC);
            //is user passes in junk, set this as default
            paramSortBy = "dd";
        }
        return paramSortBy;
    }

    public void handleReviewReports(List<Review> reviews, Map model, HttpServletRequest request) {
        User user = (User) model.get("validUser");
        if(user != null && PageHelper.isMemberAuthorized(request)){
            if (reviews != null ) {
                Map<Integer, Boolean> reports = new HashMap<Integer, Boolean>(reviews.size());
                for (Review review: reviews) {
                    reports.put(review.getId(),
                            _reportedEntityDao.hasUserReportedEntity
                                    (user, ReportedEntity.ReportedEntityType.schoolReview, review.getId()));
                }
                model.put("reviewReports", reports);
            }
        }
    }

    public void handleTopicalReviewReports(List<TopicalSchoolReview> topicalReviews, Map model, HttpServletRequest request) {
        User user = (User) model.get("validUser");
        if(user != null && PageHelper.isMemberAuthorized(request)){
            if (topicalReviews != null ) {
                Map<Integer, Boolean> reports = new HashMap<Integer, Boolean>(topicalReviews.size());
                for (TopicalSchoolReview topicalReview: topicalReviews) {
                    reports.put(topicalReview.getId(),
                            _reportedEntityDao.hasUserReportedEntity
                                    (user, ReportedEntity.ReportedEntityType.topicalSchoolReview, topicalReview.getId()));
                }
                model.put("topicalReviewReports", reports);
            }
        }
    }

    public String handleReviewsFilterSortTracking(int numReviews, Set<Poster> reviewsBy, String paramSortBy) {
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

    // GS-10633
    public void handleOverallRatingsByYear(Map<String, Object> model, School school) {
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

    /**
     * Update numReviewsBy with counts of topical reviews by Poster
     */
    public void updateCounts(Map<Poster, Integer> numReviewsBy, List<TopicalSchoolReview> topicalReviews) {
        for (TopicalSchoolReview topicalReview: topicalReviews) {
            Integer count = numReviewsBy.get(topicalReview.getPoster());
            if (count == null) {
                count = 0;
            }
            numReviewsBy.put(topicalReview.getPoster(), count+1);
        }
    }

    public void handleNumberOfReviewsCount(Map<Poster, Integer> numReviewsBy, Map model) {
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
    }

    public String getLoginRedirectUrl(School school, Map model, HttpServletRequest request) {
        User user = (User) model.get("validUser");
        String loginRedirectUrl = "#";
        if (user == null) {
            String uri = request.getRequestURI();
            uri = UrlUtil.addParameter(uri, "id=" + school.getId());
            uri = UrlUtil.addParameter(uri, "state=" + school.getDatabaseState());
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, null, uri);
            loginRedirectUrl = urlBuilder.asSiteRelative(request);
        }
        return loginRedirectUrl;
    }

    public Long handleNumberOfNonPrincipalReviews(School school, Set<Poster> reviewsBy, boolean includeInactive) {
        Long numberOfNonPrincipalReviews;
        if (!includeInactive) {
            numberOfNonPrincipalReviews = _reviewDao.countPublishedNonPrincipalReviewsBySchool(school, reviewsBy);
        } else {
            numberOfNonPrincipalReviews = _reviewDao.countPublishedDisabledNonPrincipalReviewsBySchool(school, reviewsBy);
        }
        return numberOfNonPrincipalReviews;
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
    public void handleLastModifiedDateInModel(Map<String, Object> model, School school, Set<Poster> reviewsBy, List<Review> reviews) {
        handleLastModifiedDateInModel(model, school, reviewsBy, reviews, null);
    }

    public void handleLastModifiedDateInModel(Map<String, Object> model, School school, Set<Poster> reviewsBy, List<Review> reviews, List<TopicalSchoolReview> topicalReviews) {
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

        // Again, ignore reviews if they have been filtered
        if (reviewsBy.isEmpty() && topicalReviews != null && topicalReviews.size() > 0) {
            for (TopicalSchoolReview topicalReview: topicalReviews) {
                if (!StringUtils.equalsIgnoreCase("principal", topicalReview.getPoster().getName())
                        && Review.ReviewStatus.PUBLISHED.getStatusCode().equals(topicalReview.getStatus())) {
                    Date posted = topicalReview.getPosted();
                    if (lastModifiedDate == null || (posted != null && posted.after(lastModifiedDate))) {
                        lastModifiedDate = posted;
                    }
                }
            }
        }

        if (lastModifiedDate != null) {
            model.put("lastModifiedDate", lastModifiedDate);
        }
    }

    public List<ISchoolReview> interleaveReviews(List<Review> reviews, List<TopicalSchoolReview> topicalReviews) {
        List<ISchoolReview> interleavedReviews = new ArrayList<ISchoolReview>(reviews.size() + topicalReviews.size());
        for (Review review: reviews) {
            interleavedReviews.add(new SchoolReviewAdapter(review));
        }
        for (TopicalSchoolReview topicalSchoolReview: topicalReviews) {
            interleavedReviews.add(new SchoolReviewAdapter(topicalSchoolReview));
        }
        return interleavedReviews;
    }

    public void handleSubcategoryRatings(Map<String, Object> model, School school, Ratings ratings) {
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

    public void handleCombinedSubcategoryRatings(Map<String, Object> model, School school, Ratings ratings){
        List<NameValuePair<String, Integer>> communityRatings = new ArrayList();
        if (ratings.hasPreschool()){
            addNameValueToRatingsList("Teacher quality", ratings.getAvgP_Teachers(), communityRatings);
            addNameValueToRatingsList("Parent involvement", ratings.getAvgP_Parents(), communityRatings);
            addNameValueToRatingsList("Facilities & equipmentd", ratings.getAvgP_Facilities(), communityRatings);
        } else if (ratings.hasGradeschool()){
            addNameValueToRatingsList("Teacher quality", ratings.getAvgTeachers(), communityRatings);
            addNameValueToRatingsList("Principal leadership", ratings.getAvgPrincipal(), communityRatings);
            addNameValueToRatingsList("Parent involvement", ratings.getAvgParents(), communityRatings);
        }
        model.put("communityRatings", communityRatings);
    }

    public int findCurrentPage(HttpServletRequest request){
        int page = 1;
        String pageParam = request.getParameter(PARAM_PAGE);
        if (pageParam != null) {
            try {
                page = Integer.parseInt(pageParam);
                if (page <= 1)
                    page = 1;
            } catch (Exception e) {
                // do nothing
            }
        }
        return page;
    }

    public int findFromIndex(int page, List<Review> reviews){
        return findFromIndex(page, MAX_REVIEWS_PER_PAGE, reviews);
    }

    public <T extends ISchoolReview> int findFromIndex(int page, int max, List<T> reviews){
        int fromIndex = (page - 1) * max;
        if ( reviews!=null && reviews.size()>0 ) {
            if ("principal".equals(reviews.get(0).getWho())) {
                fromIndex++;
            }
        }
        return fromIndex;
    }

    public int findToIndex(int page, int fromIndex, List reviews){
        return findToIndex(page,  fromIndex, MAX_REVIEWS_PER_PAGE, reviews);
    }


    public int findToIndex(int page, int fromIndex, int max, List reviews) {
        int toIndex = fromIndex + max;
        toIndex = Math.min(toIndex, reviews.size());
        return toIndex;
    }


    public <T> List<T> handlePagination(HttpServletRequest request, List<T> reviews, int page, int fromIndex, int toIndex) {
        List<T> reviewsToShow = new ArrayList<T>();

        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);

        if (reviews.size() == 0 || sessionContext.isCrawler() || StringUtils.isNotEmpty(request.getParameter(ParentReviewHelper.PARAM_VIEW_ALL))) {
            reviewsToShow = reviews;
            return reviewsToShow;
        } else {
            if ( fromIndex < toIndex )
                reviewsToShow = reviews.subList(fromIndex, toIndex);

        }
        return reviewsToShow;
    }

    private void addNameValueToRatingsList(String displayString, Integer numStars, List<NameValuePair<String, Integer>> communityRatings) {
        NameValuePair<String, Integer> pair;
        if (numStars != null) {
            pair = new NameValuePair(displayString, numStars);
            communityRatings.add(pair);
        }
    }

    public IReportedEntityDao getReportedEntityDao() {
        return _reportedEntityDao;
    }

    public void setReportedEntityDao(IReportedEntityDao reportedEntityDao) {
        _reportedEntityDao = reportedEntityDao;
    }

    public IReviewDao getReviewDao() {
        return _reviewDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
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

}
