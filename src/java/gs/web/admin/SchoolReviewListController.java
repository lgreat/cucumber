package gs.web.admin;

import gs.data.community.IReportedEntityDao;
import gs.data.community.ReportedEntity;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.ITopicalSchoolReviewDao;
import gs.data.school.review.Review;
import gs.data.school.review.TopicalSchoolReview;
import org.apache.commons.lang.StringUtils;
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
    public static final String MODEL_PAGE_SIZE="pageSize";
    private String _viewName;
    private IReviewDao _reviewDao;
    private IReportedEntityDao _reportedEntityDao;
    private ITopicalSchoolReviewDao _topicalSchoolReviewDao;
    private boolean _isTopical = false;
    private static final int REPORTED_REVIEWS_PAGE_SIZE = 75;
    private static final int UNPROCESSED_REVIEWS_PAGE_SIZE = 75;
    private static final String PARAM_PAGE = "p";

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();

        int page = 1;
        if (StringUtils.isNotBlank(request.getParameter(PARAM_PAGE))
                && StringUtils.isNumeric(request.getParameter(PARAM_PAGE))) {
            page = Integer.parseInt(request.getParameter(PARAM_PAGE));
        }

        if (request.getParameter(REQUEST_SHOW_UNPROCESSED) != null) {
            List<SchoolReviewListBean> unprocessedReviews;
            int totalUnprocessed;
            if (_isTopical) {
                unprocessedReviews = getUnprocessedTopicalReviews(page);
                totalUnprocessed = _topicalSchoolReviewDao.countUnprocessedReviews();
            } else {
                unprocessedReviews = getUnprocessedReviews(page);
                totalUnprocessed = _reviewDao.countUnprocessedReviews();
            }
            model.put(MODEL_SHOW_UNPROCESSED, true);
            model.put(MODEL_UNPROCESSED_REVIEW_LIST, unprocessedReviews);
            model.put(MODEL_TOTAL_UNPROCESSED, totalUnprocessed);
            model.put(MODEL_PAGE_SIZE, UNPROCESSED_REVIEWS_PAGE_SIZE);
        } else {
            List<SchoolReviewListBean> flaggedReviews = getFlaggedReviews(page);
            model.put(MODEL_SHOW_FLAGGED, true);
            model.put(MODEL_FLAGGED_REVIEW_LIST, flaggedReviews);
            model.put(MODEL_TOTAL_FLAGGED, _reportedEntityDao.countFlaggedSchoolReviews());
            model.put(MODEL_PAGE_SIZE, REPORTED_REVIEWS_PAGE_SIZE);
        }

        return new ModelAndView(getViewName(), model);
    }

    protected List<SchoolReviewListBean> getUnprocessedReviews(int page) {
        int offset = 0;
        if (page > 1) {
            offset = (page-1) * UNPROCESSED_REVIEWS_PAGE_SIZE;
        }
        List<Review> unprocessedReviews = _reviewDao.findUnprocessedReviews(UNPROCESSED_REVIEWS_PAGE_SIZE, offset);

        List<SchoolReviewListBean> rval = new ArrayList<SchoolReviewListBean>(unprocessedReviews.size());
        for (Review r: unprocessedReviews) {
            SchoolReviewListBean bean = new SchoolReviewListBean(r);
            bean.setNumReports(_reportedEntityDao.getNumberActiveReports
                    (ReportedEntity.ReportedEntityType.schoolReview, r.getId()));
            rval.add(bean);
        }
        
        return rval;
    }

    protected List<SchoolReviewListBean> getUnprocessedTopicalReviews(int page) {
        int offset = 0;
        if (page > 1) {
            offset = (page-1) * UNPROCESSED_REVIEWS_PAGE_SIZE;
        }
        List<TopicalSchoolReview> unprocessedReviews = _topicalSchoolReviewDao.findUnprocessedReviews(UNPROCESSED_REVIEWS_PAGE_SIZE, offset);

        List<SchoolReviewListBean> rval = new ArrayList<SchoolReviewListBean>(unprocessedReviews.size());
        for (TopicalSchoolReview r: unprocessedReviews) {
            SchoolReviewListBean bean = new SchoolReviewListBean(r);
            bean.setNumReports(_reportedEntityDao.getNumberActiveReports
                    (ReportedEntity.ReportedEntityType.topicalSchoolReview, r.getId()));
            rval.add(bean);
        }

        return rval;
    }

    protected List<SchoolReviewListBean> getFlaggedReviews(int page) {
        int offset = 0;
        if (page > 1) {
            offset = (page-1) * REPORTED_REVIEWS_PAGE_SIZE;
        }
        List<Integer> reportedReviewIds = _reportedEntityDao.getSchoolReviewIdsThatHaveReports(
                REPORTED_REVIEWS_PAGE_SIZE, offset);
        List<SchoolReviewListBean> rval = new ArrayList<SchoolReviewListBean>(REPORTED_REVIEWS_PAGE_SIZE);

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

    public ITopicalSchoolReviewDao getTopicalSchoolReviewDao() {
        return _topicalSchoolReviewDao;
    }

    public void setTopicalSchoolReviewDao(ITopicalSchoolReviewDao topicalSchoolReviewDao) {
        _topicalSchoolReviewDao = topicalSchoolReviewDao;
    }

    public boolean isTopical() {
        return _isTopical;
    }

    public void setTopical(boolean topical) {
        _isTopical = topical;
    }

    public static final class SchoolReviewListBean {
        private Review _review;
        private int _numReports;
        private ReportedEntity _report;

        public SchoolReviewListBean(Review review) {
            _review = review;
        }

        public SchoolReviewListBean(TopicalSchoolReview review) {
            // TODO: fill in bean
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
