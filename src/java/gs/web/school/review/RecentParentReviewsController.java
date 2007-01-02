/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: RecentParentReviewsController.java,v 1.12 2007/01/02 20:09:17 cpickslay Exp $
 */

package gs.web.school.review;

import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import gs.data.state.State;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class RecentParentReviewsController extends AbstractController {

    /**
     * Optional city. Otherwise state-wide values are retrieved.
     */
    public static final String PARAM_CITY = "city";
    public static final String PARAM_MAX = "max";
    public static final String PARAM_MAX_AGE = "maxage"; // in days

    public static int DEFAULT_MAX = 3;
    public static int DEFAULT_MAX_AGE = 90;

    private IReviewDao _reviewDao;
    private ISchoolDao _schoolDao;

    /**
     * List of IParentReviewModel objects.
     */
    public static final String MODEL_REVIEW_LIST = "reviews";

    public interface IParentReviewModel {
        String getSchoolName();

        String getSchoolLink();

        int getStars();

        String getDate();

        String getQuip();
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        SessionContext sc = SessionContextUtil.getSessionContext(request);
        State state = sc.getState();

        String city = request.getParameter(PARAM_CITY);
        int maxReviews = DEFAULT_MAX;
        if (request.getParameter(PARAM_MAX) != null) {
            maxReviews = Integer.parseInt(request.getParameter(PARAM_MAX));
        }

        List reviewIds =
                _reviewDao.findRecentReviewsInCity(state, city, maxReviews, DEFAULT_MAX_AGE);


        List reviews = new ArrayList();
        for (Iterator iter = reviewIds.iterator(); iter.hasNext();) {
            Integer reviewId = (Integer) iter.next();
            Review review = _reviewDao.getReview(reviewId);
            Integer schoolId = review.getSchoolId();
            School school = _schoolDao.getSchoolById(review.getState(), schoolId);

            reviews.add(new ReviewFacade(school, review));
        }

        ModelAndView modelAndView = new ModelAndView("/school/review/recentParentReviews");
        modelAndView.addObject(MODEL_REVIEW_LIST, reviews);
        return modelAndView;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    private static class ReviewFacade implements IParentReviewModel {
        private final School _school;
        private final Review _review;

        public ReviewFacade(School school, Review review) {
            _school = school;
            _review = review;
        }

        public String getSchoolName() {
            return _school.getName();
        }

        public String getSchoolLink() {
            UrlBuilder builder = new UrlBuilder(_school, UrlBuilder.SCHOOL_PARENT_REVIEWS);
            return builder.asSiteRelativeXml(null);
        }

        public int getStars() {
            final String quality = _review.getQuality();
            if (StringUtils.equals(quality, "1")) {
                return 1;
            } else if (StringUtils.equals(quality, "2")) {
                return 2;
            } else if (StringUtils.equals(quality, "3")) {
                return 3;
            } else if (StringUtils.equals(quality, "4")) {
                return 4;
            } else if (StringUtils.equals(quality, "5")) {
                return 5;
            }
            return 0;
        }

        public String getDate() {
            Date today = new Date();
            long diff = Math.abs(today.getTime() - _review.getPosted().getTime());
            if (diff < DateUtils.MILLIS_PER_DAY) {
                return "today";
            }
            if (diff < (DateUtils.MILLIS_PER_DAY + DateUtils.MILLIS_PER_DAY)) {
                return "yesterday";
            } else {
                return "" + Math.round(diff / DateUtils.MILLIS_PER_DAY) + " days ago";
            }
        }

        public String getQuip() {
            String c = _review.getComments();
            String q = StringUtils.abbreviate(c, 50);
            return q;
        }

    }

}
