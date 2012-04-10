package gs.web.school.review;

import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import gs.data.school.review.Review;
import gs.data.util.NameValuePair;
import gs.web.request.RequestInfo;
import gs.web.util.RedirectView301;
import gs.web.util.UrlBuilder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * User: cauer
 */
public class ParentReviewHelper {

    public static final String PARAM_SORT_BY = "sortBy";

    private IReviewDao _reviewDao;

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

    /**
     * Sort the list of reviews based on the request param
     * @param request
     * @param reviews
     * @return String
     */
    public String handleSortReviews(HttpServletRequest request, List<Review> reviews){
        String paramSortBy = request.getParameter(PARAM_SORT_BY);
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

    private void addNameValueToRatingsList(String displayString, Integer numStars, List<NameValuePair<String, Integer>> communityRatings) {
        NameValuePair<String, Integer> pair;
        if (numStars != null) {
            pair = new NameValuePair(displayString, numStars);
            communityRatings.add(pair);
        }
    }

    public IReviewDao getReviewDao() {
        return _reviewDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }
}
