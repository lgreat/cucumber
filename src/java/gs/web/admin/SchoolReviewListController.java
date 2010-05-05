package gs.web.admin;

import gs.data.community.IReportedEntityDao;
import gs.data.community.ReportedEntity;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class SchoolReviewListController extends AbstractController {
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String REQUEST_SHOW_UNPROCESSED = "showUnprocessed";
    public static final String MODEL_UNPROCESSED_REVIEW_LIST="unprocessedReviews";
    public static final String MODEL_SHOW_UNPROCESSED="showUnprocessed";
    public static final String MODEL_FLAGGED_REVIEW_LIST="flaggedReviews";
    public static final String MODEL_SHOW_FLAGGED="showFlagged";
    public static final String MODEL_TOTAL_UNPROCESSED="totalUnprocessedReviews";
    public static final String MODEL_TOTAL_FLAGGED="totalFlaggedReviews";
    private String _viewName;
    private IReviewDao _reviewDao;
    private IReportedEntityDao _reportedEntityDao;
    private static final int NUM_REPORTED_REVIEWS_TO_DISPLAY = 75;
    private static final int NUM_UNPROCESSED_REVIEWS_TO_DISPLAY = 75;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();

        if (request.getParameter(REQUEST_SHOW_UNPROCESSED) != null) {
            List<SchoolReviewListBean> unprocessedReviews = getUnprocessedReviews();
            model.put(MODEL_SHOW_UNPROCESSED, true);
            model.put(MODEL_UNPROCESSED_REVIEW_LIST, unprocessedReviews);
            model.put(MODEL_TOTAL_UNPROCESSED, _reviewDao.countUnprocessedReviews());
        } else {
            List<SchoolReviewListBean> flaggedReviews = getFlaggedReviews();
            model.put(MODEL_SHOW_FLAGGED, true);
            model.put(MODEL_FLAGGED_REVIEW_LIST, flaggedReviews);
            model.put(MODEL_TOTAL_FLAGGED, _reportedEntityDao.countFlaggedSchoolReviews());
        }

        return new ModelAndView(getViewName(), model);
    }

    protected List<SchoolReviewListBean> getUnprocessedReviews() {
        List<Review> unprocessedReviews = _reviewDao.findUnprocessedReviews(NUM_UNPROCESSED_REVIEWS_TO_DISPLAY);

        List<SchoolReviewListBean> rval = new ArrayList<SchoolReviewListBean>(unprocessedReviews.size());
        for (Review r: unprocessedReviews) {
            SchoolReviewListBean bean = new SchoolReviewListBean(r);
            bean.setNumReports(_reportedEntityDao.getNumberActiveReports
                    (ReportedEntity.ReportedEntityType.schoolReview, r.getId()));
            rval.add(bean);
        }
        
        return rval;
    }

    protected List<SchoolReviewListBean> getFlaggedReviews() {
        List<Integer> reportedReviewIds = _reportedEntityDao.getSchoolReviewIdsThatHaveReports(
                NUM_REPORTED_REVIEWS_TO_DISPLAY);
        List<SchoolReviewListBean> rval = new ArrayList<SchoolReviewListBean>(NUM_REPORTED_REVIEWS_TO_DISPLAY);

        // asynchronous?
        // group queries?
        for (Integer reviewId: reportedReviewIds) {
            try {
                SchoolReviewListBean bean = new SchoolReviewListBean(_reviewDao.getReview(reviewId));
                bean.setNumReports((_reportedEntityDao.getNumberTimesReported
                        (ReportedEntity.ReportedEntityType.schoolReview, reviewId)));
                bean.setReport(_reportedEntityDao.getOldestReport(ReportedEntity.ReportedEntityType.schoolReview, reviewId));
                rval.add(bean);
            } catch (Exception e) {
                _log.error("Error finding review and related data for report: " + e, e);
            }
        }
        return rval;
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

    public static final class SchoolReviewListBean {
        private Review _review;
        private int _numReports;
        private ReportedEntity _report;

        public SchoolReviewListBean(Review review) {
            _review = review;
        }

        public Review getReview() {
            return _review;
        }

        public int getNumReports() {
            return _numReports;
        }

        public void setNumReports(int numReports) {
            _numReports = numReports;
        }

        public ReportedEntity getReport() {
            return _report;
        }

        public void setReport(ReportedEntity report) {
            _report = report;
        }
    }
}
