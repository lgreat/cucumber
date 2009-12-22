package gs.web.community;

import gs.web.util.ReadWriteController;
import gs.web.util.UrlBuilder;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.data.community.*;
import gs.data.content.cms.CmsDiscussionBoard;
import gs.data.content.cms.ICmsDiscussionBoardDao;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 * @author Dave Roy <mailto:droy@greatschools.org>
 */
public class ReportContentAjaxController extends SimpleFormController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());
    private ReportContentService _reportContentService;

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
            _log.warn("Attempt to submit with no valid user rejected");
            throw new IllegalStateException("Discussion submission occurred but no valid user is cookied!");
        }
        User reporter = sessionContext.getUser();
        if (reporter == null || reporter.getUserProfile() == null) {
            return null; // early exit
        }
        if (reporter.getId() != command.getReporterId()) {
            _log.warn("Content reporter id doesn't match submitter cookies.");
            return null;
        }

        _reportContentService.reportContent(reporter, request, command.getContentId(), command.getType(), command.getReason());
        
        return null;
    }

    public ReportContentService getReportContentService() {
        return _reportContentService;
    }

    public void setReportContentService(ReportContentService reportContentService) {
        _reportContentService = reportContentService;
    }
}
