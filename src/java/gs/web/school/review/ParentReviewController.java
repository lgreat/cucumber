package gs.web.school.review;

import gs.data.school.School;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import gs.data.school.review.Review;
import gs.web.school.AbstractSchoolController;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Parent review page
 *
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class ParentReviewController extends AbstractSchoolController {

    public static final String BEAN_ID = "/school/parentReviews.page";

    private IReviewDao _reviewDao;
    private String _viewName;

    protected static final int MAX_REVIEWS_PER_PAGE = 4; //number of reviews per page
    protected static final String PARAM_SORT_BY = "sortBy";


    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map model = new HashMap();
        School school = (School) request.getAttribute(SCHOOL_ATTRIBUTE);

        if (null != school) {
            List reviews = _reviewDao.getPublishedReviewsBySchool(school);
            ParentReviewCommand cmd = new ParentReviewCommand();
            cmd.setSortBy(request.getParameter(PARAM_SORT_BY));

            Ratings ratings = _reviewDao.findRatingsBySchool(school);            
            cmd.setRatings(ratings);

            if (StringUtils.isBlank(cmd.getSortBy())) {
                cmd.setSortBy("dd");
            }
            /**
             * dd - date descending
             * da - date ascending
             * rd - overall rating descending
             * ra - overall rating ascending
             */
            if ("da".equals(cmd.getSortBy())) {
                Collections.reverse(reviews);
            } else if ("rd".equals(cmd.getSortBy())) {
                Collections.sort(reviews, Review.OVERALL_RATING_COMPARATOR);
                Collections.reverse(reviews);
            } else if ("ra".equals(cmd.getSortBy())) {
                Collections.sort(reviews, Review.OVERALL_RATING_COMPARATOR);
            } else {
                cmd.setSortBy("dd");
            }
            
            cmd.setSchool(school);
            cmd.setReviews(reviews);
            cmd.setTotalReviews(reviews.size());
            cmd.setCurrentDate(new Date());

            SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
            if (sessionContext.isCrawler()) {
                cmd.setMaxReviewsPerPage(reviews.size());
            } else {
                cmd.setMaxReviewsPerPage(MAX_REVIEWS_PER_PAGE);
            }

            model.put("cmd", cmd);
            model.put("param_sortby", PARAM_SORT_BY);
        }
        return new ModelAndView(getViewName(), model);
    }


    public static class ParentReviewCommand {
        private School _school;
        private List<Review> _reviews;
        private Date _currentDate;
        private int _totalReviews = 0;
        private String _sortBy;
        private Ratings _ratings;
        private int _maxReviewsPerPage;


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
}
