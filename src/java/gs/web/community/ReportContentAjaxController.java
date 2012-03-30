package gs.web.community;

import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
import gs.web.util.ReadWriteController;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.data.community.*;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 * @author Dave Roy <mailto:droy@greatschools.org>
 */
public class ReportContentAjaxController extends SimpleFormController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());
    private IReportContentService _reportContentService;
    private IUserDao _userDao;
    private IUserContentDao _userContentDao;
    private IReviewDao _reviewDao;

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
                                    Object commandObj, BindException errors) throws Exception {
        ReportContentCommand command = (ReportContentCommand) commandObj;

        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        // error checking
        if (sessionContext == null) {
            _log.warn("Attempt to submit with no SessionContext rejected");
            throw new IllegalStateException("No SessionContext found in request");
        }
        if (!PageHelper.isMemberAuthorized(request) || sessionContext.getUser() == null) {
            _log.warn("Attempt to submit with no valid user -- rejected");
            throw new IllegalStateException("Report content submission occurred but no valid user is cookied!");
        }
        User reporter = sessionContext.getUser();
        if (reporter == null || reporter.getUserProfile() == null) {
            return null; // early exit
        }
        if (reporter.getId() != command.getReporterId()) {
            _log.warn("Content reporter id doesn't match submitter cookies.");
            return null;
        }

        User reportee = null;
        Integer authorId = null;
        boolean checkForReporteeUserProfile = true;
        if (command.getType() == ReportedEntity.ReportedEntityType.discussion ||
                command.getType() == ReportedEntity.ReportedEntityType.reply) {
            UserContent uc = _userContentDao.findById(command.getContentId());
            if (uc == null) {
                return null;
            }
            authorId = uc.getAuthorId();
        } else if (command.getType() == ReportedEntity.ReportedEntityType.member) {
            authorId = command.getContentId();
        } else if (command.getType() == ReportedEntity.ReportedEntityType.schoolReview) {
            Review review = _reviewDao.getReview(command.getContentId());
            if (review == null) {
                return null;
            }
            if (review.getUser() != null) {
                authorId = review.getUser().getId();
            }
            checkForReporteeUserProfile = false;
        } else if (command.getType() == ReportedEntity.ReportedEntityType.schoolMedia) {
        } else {
            _log.warn("Unknown report content type: " + command.getType());
            return null;
        }

        if (authorId != null) {
            try {
                reportee = _userDao.findUserFromId(authorId);
            } catch (ObjectRetrievalFailureException orfe) {
                // reportee stays null
            }
        } else if (command.getType() != ReportedEntity.ReportedEntityType.schoolReview &&
                command.getType() != ReportedEntity.ReportedEntityType.schoolMedia) {
            _log.warn("Could not determine reportee's member id for content: '" + command.getContentId() + "'");
            return null;
        }

        if (command.getType() != ReportedEntity.ReportedEntityType.schoolReview
                && command.getType() != ReportedEntity.ReportedEntityType.schoolMedia
                && (reportee == null
                    || (checkForReporteeUserProfile && reportee.getUserProfile() == null))) {
            _log.warn("Reported content created by a user that doesn't exist.  member_id = '" + authorId + "'");
            return null;
        }
        _reportContentService.reportContent(reporter, reportee, request, command.getContentId(),
                                            command.getType(), command.getReason());

        return null;
    }

    public IReportContentService getReportContentService() {
        return _reportContentService;
    }

    public void setReportContentService(IReportContentService reportContentService) {
        _reportContentService = reportContentService;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public IUserContentDao getUserContentDao() {
        return _userContentDao;
    }

    public void setUserContentDao(IUserContentDao userContentDao) {
        _userContentDao = userContentDao;
    }

    public IReviewDao getReviewDao() {
        return _reviewDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }
}
