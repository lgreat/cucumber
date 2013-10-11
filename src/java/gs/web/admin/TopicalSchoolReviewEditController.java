package gs.web.admin;

import gs.data.community.IReportedEntityDao;
import gs.data.community.IUserDao;
import gs.data.community.ReportedEntity;
import gs.data.community.User;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.school.ISchoolDao;
import gs.data.school.review.*;
import gs.web.util.ReadWriteController;
import gs.web.util.UrlBuilder;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @author aroy@greatschools.org
 */
public class TopicalSchoolReviewEditController extends SimpleFormController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());
    private ITopicalSchoolReviewDao _topicalSchoolReviewDao;
    private IReportedEntityDao _reportedEntityDao;
    private IUserDao _userDao;
    private ISchoolDao _schoolDao;
    private ExactTargetAPI _exactTargetAPI;

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        Object commandObj = super.formBackingObject(request);
        TopicalSchoolReviewEditCommand command = (TopicalSchoolReviewEditCommand) commandObj;
        try {
            TopicalSchoolReview review = _topicalSchoolReviewDao.get(Long.parseLong(request.getParameter("id")));
            command.setReview(review);
            command.setReports(_reportedEntityDao.getReports(ReportedEntity.ReportedEntityType.topicalSchoolReview, review.getId()));
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
        TopicalSchoolReviewEditCommand command = (TopicalSchoolReviewEditCommand) commandObj;
        TopicalSchoolReview review = command.getReview();
        String listPage = getSuccessView();
        if (StringUtils.equals("ur", command.getFrom())) {
            listPage = UrlUtil.addParameter(listPage, "showUnprocessed=true");
        }
        String editPage = "redirect:/admin/topicalSchoolReview/edit.page?id=" + review.getId() + "&from=" + command.getFrom();

//        if (request.getParameter("resolveReport") != null) {
//            // not currently used
//            int reportId = Integer.valueOf(request.getParameter("reportId"));
//            _log.info("Resolving report " + reportId);
//            _reportedEntityDao.resolveReport(reportId);
//            return new ModelAndView(editPage);
//        } else
        if (request.getParameter("submitNote") != null) {
            review.setNote(command.getNote());
            _topicalSchoolReviewDao.save(review);
            return new ModelAndView(editPage);
        } else if (request.getParameter("disableReview") != null) {
            review.setStatus("d");
            review.setProcessDate(Calendar.getInstance().getTime());
            _topicalSchoolReviewDao.save(review);
            return new ModelAndView(editPage);
        } else if (request.getParameter("enableReview") != null) {
            if (StringUtils.equals("u", review.getStatus())) {
                if (Poster.STUDENT == review.getWho() || Poster.PRINCIPAL == review.getWho()) {
                    Map<String,String> emailAttributes = new HashMap<String,String>();
                    review.setSchoolDao(_schoolDao);
                    review.setUserDao(_userDao);
                    emailAttributes.put("schoolName", review.getSchool().getName());
                    emailAttributes.put("HTML__review", "<p>" + review.getComments() + "</p>");

                    StringBuilder reviewLink = new StringBuilder("<a href=\"");
                    UrlBuilder urlBuilder = new UrlBuilder(review.getSchool(), UrlBuilder.SCHOOL_PARENT_REVIEWS);
                    urlBuilder.addParameter("lr", "true");
                    reviewLink.append(urlBuilder.asFullUrl(request)).append("#tps").append(review.getId());
                    reviewLink.append("\">your review</a>");
                    emailAttributes.put("HTML__reviewLink", reviewLink.toString());

                    _exactTargetAPI.sendTriggeredEmail("review_posted_trigger",review.getUser(), emailAttributes);
                }
            }

            if (StringUtils.equals("h", review.getStatus())) {
                Map<String,String> emailAttributes = new HashMap<String,String>();
                review.setSchoolDao(_schoolDao);
                review.setUserDao(_userDao);
                emailAttributes.put("schoolName", review.getSchool().getName());
                emailAttributes.put("HTML__review", "<p>" + review.getComments() + "</p>");

                StringBuilder reviewLink = new StringBuilder("<a href=\"");
                UrlBuilder urlBuilder = new UrlBuilder(review.getSchool(), UrlBuilder.SCHOOL_PARENT_REVIEWS);
                urlBuilder.addParameter("lr", "true");
                reviewLink.append(urlBuilder.asFullUrl(request)).append("#tps").append(review.getId());
                reviewLink.append("\">your review</a>");
                emailAttributes.put("HTML__reviewLink", reviewLink.toString());

                _exactTargetAPI.sendTriggeredEmail("review_posted_trigger",review.getUser(), emailAttributes);
            }

            review.setStatus("p");
            review.setProcessDate(Calendar.getInstance().getTime());
            _topicalSchoolReviewDao.save(review);
            return new ModelAndView(editPage);
        } else if (request.getParameter("resolveReports") != null) {
            _topicalSchoolReviewDao.save(review);
            _log.info("Resolving reports");
            _reportedEntityDao.resolveReportsFor(ReportedEntity.ReportedEntityType.topicalSchoolReview, review.getId());
        }
        return new ModelAndView(listPage);
    }

    public ITopicalSchoolReviewDao getTopicalSchoolReviewDao() {
        return _topicalSchoolReviewDao;
    }

    public void setTopicalSchoolReviewDao(ITopicalSchoolReviewDao topicalSchoolReviewDao) {
        _topicalSchoolReviewDao = topicalSchoolReviewDao;
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

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public ExactTargetAPI getExactTargetAPI() {
        return _exactTargetAPI;
    }

    public void setExactTargetAPI(ExactTargetAPI exactTargetAPI) {
        _exactTargetAPI = exactTargetAPI;
    }
}
