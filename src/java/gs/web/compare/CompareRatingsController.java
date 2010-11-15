package gs.web.compare;

import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import gs.data.school.review.Review;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CompareRatingsController extends AbstractCompareSchoolController {
    private String _successView;
    private IReviewDao _reviewDao;

    @Override
    protected void handleCompareRequest(HttpServletRequest request, HttpServletResponse response,
                                        List<ComparedSchoolBaseStruct> schools, Map<String, Object> model) throws
                                                                                                           IOException {
        model.put(MODEL_TAB, "ratings");

        handleCommunityRating(schools);

        handleRecentReview(schools);
    }

    protected void handleRecentReview(List<ComparedSchoolBaseStruct> structs) {
        for (ComparedSchoolBaseStruct baseStruct: structs) {
            ComparedSchoolRatingsStruct struct = (ComparedSchoolRatingsStruct) baseStruct;
            School school = struct.getSchool();
            List<Review> reviews = _reviewDao.getPublishedReviewsBySchool(school, 1);
            if (reviews != null && reviews.size() == 1) {
                struct.setRecentReview(reviews.get(0));
            }
            Long numReviews = _reviewDao.countPublishedNonPrincipalReviewsBySchool(school);
            if (numReviews != null && numReviews > 0) {
                struct.setNumReviews(numReviews.intValue());
            }
        }
    }

    protected void handleCommunityRating(List<ComparedSchoolBaseStruct> structs) {
        for (ComparedSchoolBaseStruct baseStruct: structs) {
            ComparedSchoolRatingsStruct struct = (ComparedSchoolRatingsStruct) baseStruct;
            School school = struct.getSchool();

            Ratings ratings = _reviewDao.findRatingsBySchool(school);
            if (ratings == null) {
                continue;
            }
            if (ratings.getNumberOfReviews() != null) {
                struct.setNumRatings(ratings.getNumberOfReviews().intValue());
            }
            if (ratings.getOverall() != null) {
                struct.setCommunityRating(ratings.getOverall());
            }
            if (ratings.getAvgParents() != null) {
                struct.setParentRating(ratings.getAvgParents());
            }
            if (ratings.getAvgPrincipal() != null) {
                struct.setPrincipalRating(ratings.getAvgPrincipal());
            }
            if (ratings.getAvgTeachers() != null) {
                struct.setTeacherRating(ratings.getAvgTeachers());
            }
        }
    }


    @Override
    protected ComparedSchoolBaseStruct getStruct() {
        return new ComparedSchoolRatingsStruct();
    }

    @Override
    public String getSuccessView() {
        return _successView;
    }

    public void setSuccessView(String successView) {
        _successView = successView;
    }

    public IReviewDao getReviewDao() {
        return _reviewDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }
}
