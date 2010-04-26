package gs.web.admin;

import gs.data.community.IReportedEntityDao;
import gs.data.community.ReportedEntity;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
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
    public static final String MODEL_UNPROCESSED_REVIEW_LIST="unprocessedReviews";
    public static final String MODEL_FLAGGED_REVIEW_LIST="flaggedReviews";
    private String _viewName;
    private IReviewDao _reviewDao;
    private IReportedEntityDao _reportedEntityDao;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();

        List<SchoolReviewListBean> unprocessedReviews = getUnprocessedReviews();

        List<SchoolReviewListBean> flaggedReviews = getFlaggedReviews();

        model.put(MODEL_UNPROCESSED_REVIEW_LIST, unprocessedReviews);
        model.put(MODEL_FLAGGED_REVIEW_LIST, flaggedReviews);

        return new ModelAndView(getViewName(), model);
    }

    protected List<SchoolReviewListBean> getUnprocessedReviews() {
        List<Review> unprocessedReviews = _reviewDao.findUnprocessedReviews(25);

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
        List<Long> reportedReviewIds = _reportedEntityDao.getSchoolReviewIdsThatHaveReports(100);
        List<SchoolReviewListBean> rval = new ArrayList<SchoolReviewListBean>(reportedReviewIds.size());

        for (Long reviewId: reportedReviewIds) {
            try {
                SchoolReviewListBean bean = new SchoolReviewListBean(_reviewDao.getReview(reviewId.intValue()));
                if (StringUtils.length(bean.getReview().getStatus()) == 2) {
                    continue;
                }
                bean.setNumReports((_reportedEntityDao.getNumberTimesReported
                        (ReportedEntity.ReportedEntityType.schoolReview, reviewId)));
                bean.setReport(_reportedEntityDao.getOldestReport(ReportedEntity.ReportedEntityType.schoolReview, reviewId));
                rval.add(bean);
                if (rval.size() == 25) {
                    break;
                }
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
