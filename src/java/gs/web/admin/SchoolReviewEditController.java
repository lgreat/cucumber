package gs.web.admin;

import gs.data.community.IReportedEntityDao;
import gs.data.community.IUserDao;
import gs.data.community.ReportedEntity;
import gs.data.community.User;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import gs.web.util.ReadWriteController;
import gs.web.util.UrlUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class SchoolReviewEditController extends SimpleFormController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());
    private IReviewDao _reviewDao;
    private IReportedEntityDao _reportedEntityDao;
    private IUserDao _userDao;

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        Object commandObj = super.formBackingObject(request);
        SchoolReviewEditCommand command = (SchoolReviewEditCommand) commandObj;
        try {
            Review review = _reviewDao.getReview(Integer.parseInt(request.getParameter("id")));
            command.setReview(review);
            command.setReports(_reportedEntityDao.getReports(ReportedEntity.ReportedEntityType.schoolReview, review.getId()));
            if (command.getReports() != null && command.getReports().size() > 0) {
                Map<Integer, User> reportToUserMap = new HashMap<Integer, User>(2);
                for(ReportedEntity report: command.getReports()) {
                    try {
                        reportToUserMap.put(report.getId(), _userDao.findUserFromId(report.getReporterId()));
                    } catch (Exception e) {
                        // ignore
                    }
                }
                command.setReportToUserMap(reportToUserMap);
            }
        } catch (ObjectRetrievalFailureException orfe) {
            // do nothing
        }
        if (StringUtils.isNotBlank(request.getParameter("from"))) {
            command.setFrom(request.getParameter("from"));
        }
        return command;
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object commandObj,
                                    BindException errors) throws Exception {
        SchoolReviewEditCommand command = (SchoolReviewEditCommand) commandObj;
        Review review = command.getReview();
        String listPage = getSuccessView();
        if (StringUtils.equals("ur", command.getFrom())) {
            listPage = UrlUtil.addParameter(listPage, "showUnprocessed=true");
        }
        String editPage = "redirect:/admin/schoolReview/edit.page?id=" + review.getId() + "&from=" + command.getFrom();

        if (request.getParameter("formCancel") != null || review == null) {
            // fall through
        } else if (request.getParameter("resolveReport") != null) {
            // not currently used
            int reportId = Integer.valueOf(request.getParameter("reportId"));
            _log.info("Resolving report " + reportId);
            _reportedEntityDao.resolveReport(reportId);
            return new ModelAndView(editPage);
        } else if (request.getParameter("submitNote") != null) {
            review.setNote(command.getNote());
            _reviewDao.saveReview(review);
            return new ModelAndView(editPage);
        } else if (request.getParameter("disableReview") != null) {
            review.setStatus("d");
            _reviewDao.saveReview(review);
            return new ModelAndView(editPage);
        } else if (request.getParameter("enableReview") != null) {
            review.setStatus("p");
            _reviewDao.saveReview(review);
            return new ModelAndView(editPage);
        } else if (request.getParameter("resolveReports") != null) {
            _reviewDao.saveReview(review);
            _log.info("Resolving reports");
            _reportedEntityDao.resolveReportsFor(ReportedEntity.ReportedEntityType.schoolReview, review.getId());
        }
        return new ModelAndView(listPage);
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

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }
}
