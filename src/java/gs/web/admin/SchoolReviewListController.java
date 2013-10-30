package gs.web.admin;

import gs.data.community.IReportedEntityDao;
import gs.data.community.ReportedEntity;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.ITopicalSchoolReviewDao;
import gs.data.school.review.Review;
import gs.data.school.review.TopicalSchoolReview;
import gs.data.state.State;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

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
            List<SchoolReviewListBean> flaggedReviews;
            int totalFlagged;
            if (_isTopical) {
                flaggedReviews = getFlaggedTopicalReviews(page);
                totalFlagged = _reportedEntityDao.countFlaggedTopicalSchoolReviews();
            } else {
                flaggedReviews = getFlaggedReviews(page);
                totalFlagged =  _reportedEntityDao.countFlaggedSchoolReviews();
            }
            model.put(MODEL_SHOW_FLAGGED, true);
            model.put(MODEL_FLAGGED_REVIEW_LIST, flaggedReviews);
            model.put(MODEL_TOTAL_FLAGGED, totalFlagged);
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

    protected List<SchoolReviewListBean> getFlaggedTopicalReviews(int page) {
        int offset = 0;
        if (page > 1) {
            offset = (page-1) * REPORTED_REVIEWS_PAGE_SIZE;
        }
        List<Integer> reportedReviewIds = _reportedEntityDao.getTopicalSchoolReviewIdsThatHaveReports(
                REPORTED_REVIEWS_PAGE_SIZE, offset);
        List<SchoolReviewListBean> rval = new ArrayList<SchoolReviewListBean>(REPORTED_REVIEWS_PAGE_SIZE);

        // asynchronous?
        // group queries?
        for (Integer reviewId: reportedReviewIds) {
            try {
                SchoolReviewListBean bean = new SchoolReviewListBean(_topicalSchoolReviewDao.get(reviewId));
                bean.setNumReports((_reportedEntityDao.getNumberTimesReported
                        (ReportedEntity.ReportedEntityType.topicalSchoolReview, reviewId)));
                bean.setReport(_reportedEntityDao.getOldestReport(ReportedEntity.ReportedEntityType.topicalSchoolReview, reviewId));
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
        private int _numReports;
        private ReportedEntity _report;
        private String _screenName;
        private String _email;
        private String _who;
        private Date _posted;
        private Integer _reviewId;
        private String _schoolName;
        private State _schoolState;
        private String _status;
        private String _how;
        private String _ip;
        private String _topic;

        public SchoolReviewListBean(Review review) {
            if (review.getUser() != null) {
                _email = review.getUser().getEmail();
                if (review.getUser().getUserProfile() != null) {
                    _screenName = review.getUser().getUserProfile().getScreenName();
                }
            }
            _who = review.getWho();
            _posted = review.getPosted();
            _reviewId = review.getId();
            if (review.getSchool() != null) {
                _schoolName = review.getSchool().getName();
                _schoolState = review.getSchool().getDatabaseState();
            }
            _status = review.getStatus();
            _how = review.getHow();
            _ip = review.getIp();
        }

        public SchoolReviewListBean(TopicalSchoolReview review) {
            if (review.getUser() != null) {
                _email = review.getUser().getEmail();
                if (review.getUser().getUserProfile() != null) {
                    _screenName = review.getUser().getUserProfile().getScreenName();
                }
            }
            _who = review.getWho();
            _posted = review.getCreated();
            _reviewId = review.getId();
            if (review.getSchool() != null) {
                _schoolName = review.getSchool().getName();
                _schoolState = review.getSchool().getDatabaseState();
            }
            _status = review.getStatus();
            _how = review.getHow();
            _ip = review.getIp();
            _topic = review.getTopic().getName();
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

        public String getScreenName() {
            return _screenName;
        }

        public String getEmail() {
            return _email;
        }

        public String getWho() {
            return _who;
        }

        public Date getPosted() {
            return _posted;
        }

        public Integer getReviewId() {
            return _reviewId;
        }

        public String getSchoolName() {
            return _schoolName;
        }

        public State getSchoolState() {
            return _schoolState;
        }

        public String getStatus() {
            return _status;
        }

        public String getHow() {
            return _how;
        }

        public String getIp() {
            return _ip;
        }

        public String getTopic() {
            return _topic;
        }
    }
}
