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
    protected static final String PARAM_PAGER_OFFSET = "pager.offset";

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
        Map model = new HashMap();
        School school = (School) request.getAttribute(SCHOOL_ATTRIBUTE);

        if (null != school) {
            List reviews = _reviewDao.getPublishedReviewsBySchool(school);
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
            cmd.setSortBy(paramSortBy);
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

            String pagerOffset = request.getParameter(PARAM_PAGER_OFFSET);
            if (StringUtils.isBlank(pagerOffset) || "0".equals(pagerOffset)) {
                cmd.setShowParentReviewForm(true);
            } else {
                cmd.setShowParentReviewForm(false);
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
}
