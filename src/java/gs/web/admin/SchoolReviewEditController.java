package gs.web.admin;

import gs.data.community.IReportedEntityDao;
import gs.data.community.ReportedEntity;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import gs.web.util.ReadWriteController;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class SchoolReviewEditController extends SimpleFormController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());
    private IReviewDao _reviewDao;
    private IReportedEntityDao _reportedEntityDao;

    @Override
    protected void onBindOnNewForm(HttpServletRequest request, Object commandObj) throws Exception {
        super.onBindOnNewForm(request, commandObj);

        SchoolReviewEditCommand command = (SchoolReviewEditCommand) commandObj;

        try {
            Review review = _reviewDao.getReview(Integer.parseInt(request.getParameter("id")));
            command.setReview(review);
            command.setReports(_reportedEntityDao.getReports(ReportedEntity.ReportedEntityType.schoolReview, review.getId()));
        } catch (ObjectRetrievalFailureException orfe) {
            // do nothing
        }
    }

    @Override
    protected void onBind(HttpServletRequest request, Object commandObj) throws Exception {
        super.onBind(request, commandObj);
        SchoolReviewEditCommand command = (SchoolReviewEditCommand) commandObj;

        if (request.getParameter("formCancel") != null) {
            command.setCancel(true);
        }
    }

    @Override
    protected void doSubmitAction(Object commandObj) throws Exception {
        SchoolReviewEditCommand command = (SchoolReviewEditCommand) commandObj;

        if (command.isCancel()) {
            return;
        }

        Review review = _reviewDao.getReview(command.getId());

        if (review != null && StringUtils.isNotBlank(command.getStatus())) {
            review.setStatus(command.getStatus());
            review.setNote(command.getNote());

            _reviewDao.saveReview(review);
        }
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
}
