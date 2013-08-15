/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: RecentParentReviewsController.java,v 1.23 2009/12/04 22:27:06 chriskimm Exp $
 */

package gs.web.school.review;

import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import gs.data.state.State;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates a model to show recent parent reviews in a geographical region.
 * Inputs:
 * <li>state - required
 * <li>city - optional
 * <li>max - optional limit on the number of reviews to show. Default is 3.
 * Output model:
 * <li>reviews - a List of IParentReviewModel objects
 *
 * @author <a href="mailto:apeterson@greatschools.org">Andrew J. Peterson</a>
 */
public class RecentParentReviewsController extends AbstractController {

    /**
     * Optional city. Otherwise state-wide values are retrieved.
     */
    public static final String PARAM_CITY = "city";
    public static final String PARAM_DISTRICT_ID = "district_id";
    public static final String PARAM_MAX = "max";
    public static final String PARAM_MAX_AGE = "maxage"; // in days
    public static final String PARAM_NO_FOLLOW = "nofollow";
    public static final String MODEL_REL_VALUE = "relvalue";

    public static int DEFAULT_MAX = 3;
    public static int DEFAULT_MAX_AGE = 90;

    private IReviewDao _reviewDao;

    /**
     * List of IParentReviewModel objects.
     */
    public static final String MODEL_REVIEW_LIST = "reviews";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        SessionContext sc = SessionContextUtil.getSessionContext(request);
        State state = sc.getState();

        int maxReviews = DEFAULT_MAX;
        if (request.getParameter(PARAM_MAX) != null) {
            maxReviews = Integer.parseInt(request.getParameter(PARAM_MAX));
        }

        int maxAge = DEFAULT_MAX_AGE;
        if (request.getParameter(PARAM_MAX_AGE) != null) {
            maxAge = Integer.parseInt(request.getParameter(PARAM_MAX_AGE));
        }

        String relValue = "";
        if (!StringUtils.isBlank(request.getParameter(PARAM_NO_FOLLOW))) {
            relValue = "nofollow";
        }
         //Revert Shomi
        List <Integer> reviewIds = new ArrayList<Integer>();
        String city = request.getParameter(PARAM_CITY);
        if (city != null) {
            reviewIds = _reviewDao.findRecentReviewsInCity(state, city, maxReviews, maxAge);
        } else {
            String district_id_param = request.getParameter(PARAM_DISTRICT_ID);
            if (!StringUtils.isBlank(district_id_param) && StringUtils.isNumeric(district_id_param)) {
                int district_id = Integer.parseInt(request.getParameter(PARAM_DISTRICT_ID));
                reviewIds = _reviewDao.findRecentReviewsInDistrict(state, district_id, maxReviews, maxAge);               
            }
        }

        List<ReviewFacade> reviews = new ArrayList<ReviewFacade>();
        for (Integer reviewId : reviewIds) {
            Review review = _reviewDao.getReview(reviewId);

            School school = review.getSchool();
            Integer totalReviews= _reviewDao.countTotalPublishedReviews(school);
            reviews.add(new ReviewFacade(school, review, totalReviews));
        }

        ModelAndView modelAndView = new ModelAndView("/school/review/recentParentReviews");
        modelAndView.addObject(MODEL_REVIEW_LIST, reviews);
        modelAndView.addObject(MODEL_REL_VALUE, relValue);
        return modelAndView;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }

}
