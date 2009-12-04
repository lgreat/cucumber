package gs.web.community;

import gs.web.util.ReadWriteController;
import gs.web.util.UrlBuilder;
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
 */
public class ReportContentAjaxController extends SimpleFormController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());
    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private IDiscussionDao _discussionDao;
    private IDiscussionReplyDao _discussionReplyDao;
    private IUserDao _userDao;
    private JavaMailSender _mailSender;

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
                                    Object commandObj, BindException errors) throws Exception {
        ReportContentCommand command = (ReportContentCommand) commandObj;

        User reporter = null;
        try {
            reporter = _userDao.findUserFromId(command.getReporterId());
        } catch (ObjectRetrievalFailureException orfe) {
            _log.error("Unknown user tried to report " + command.getType() + " " +
                    command.getContentId() + ", reporter id=" + command.getReporterId());
        }
        if (reporter == null || reporter.getUserProfile() == null) {
            return null; // early exit
        }

        String urlToContent = getLinkForContent(request, command.getContentId(), command.getType());
        sendEmail(urlToContent, command.getType(), reporter, command.getReason());

        return null;
    }

    protected void sendEmail(String urlToContent, ReportContentCommand.ReportType contentType,
                             User reporter, String reason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("aroy@greatschools.org");
        message.setFrom("aroy@greatschools.org");
        message.setSentDate(new Date());
        message.setSubject("Reported content alert");
        StringBuffer body = new StringBuffer();
        body.append("User ").append(reporter.getUserProfile().getScreenName());
        body.append(" (id=").append(reporter.getId()).append(", email= ").append(reporter.getEmail());
        body.append(") has reported the following ").append(contentType).append(":\n\n");
        body.append(urlToContent).append("\n\n");
        body.append("Reason provided: ").append(reason);
        message.setText(body.toString());

        try {
            _mailSender.send(message);
        }
        catch (MailException me) {
            _log.error("Error sending content reported email: urlToContent=" +
                    urlToContent + "; reporter=" + reporter.getUserProfile().getScreenName(), me);
        }
    }

    // for a user, the relevant page to go to is the user profile page
    // for a discussion -- the discussion detail page
    // for a discussion reply -- whichever page of the discussion detail page the reply would appear on
    protected String getLinkForContent(HttpServletRequest request, int contentId,
                                       ReportContentCommand.ReportType contentType) {
        Discussion d;
        UrlBuilder urlBuilder;
        switch (contentType) {
            case discussion:
                d = _discussionDao.findById(contentId);
                if (d == null || d.getBoardId() == null) {
                    return null;
                }
                urlBuilder = getUrlBuilderForDiscussion(d);
                if (urlBuilder != null) {
                    return urlBuilder.asFullUrl(request);
                }
                break;
            case reply:
                DiscussionReply reply = _discussionReplyDao.findById(contentId);
                if (reply == null || reply.getDiscussion() == null || reply.getDiscussion().getBoardId() == null) {
                    return null;
                }
                urlBuilder = getUrlBuilderForDiscussion(reply.getDiscussion());
                if (urlBuilder != null) {
                    urlBuilder.setParameter("discussionReplyId", String.valueOf(contentId));
                    return urlBuilder.asFullUrl(request) + "#reply_" + contentId;
                }
                break;
            case member:
                User u = null;

                try {
                    u = _userDao.findUserFromId(contentId);
                } catch (ObjectRetrievalFailureException orfe) {
                    // handled below
                }
                if (u == null || u.getUserProfile() == null) {
                    return null;
                }
                urlBuilder = new UrlBuilder(u, UrlBuilder.USER_PROFILE);
                return urlBuilder.asFullUrl(request);
        }

        return null;
    }

    protected UrlBuilder getUrlBuilderForDiscussion(Discussion d) {
        CmsDiscussionBoard board = _cmsDiscussionBoardDao.get(d.getBoardId());
        if (board == null) {
            return null;
        }
        return new UrlBuilder(UrlBuilder.COMMUNITY_DISCUSSION, board.getFullUri(), (long)d.getId());

    }

    public ICmsDiscussionBoardDao getCmsDiscussionBoardDao() {
        return _cmsDiscussionBoardDao;
    }

    public void setCmsDiscussionBoardDao(ICmsDiscussionBoardDao cmsDiscussionBoardDao) {
        _cmsDiscussionBoardDao = cmsDiscussionBoardDao;
    }

    public IDiscussionDao getDiscussionDao() {
        return _discussionDao;
    }

    public void setDiscussionDao(IDiscussionDao discussionDao) {
        _discussionDao = discussionDao;
    }

    public IDiscussionReplyDao getDiscussionReplyDao() {
        return _discussionReplyDao;
    }

    public void setDiscussionReplyDao(IDiscussionReplyDao discussionReplyDao) {
        _discussionReplyDao = discussionReplyDao;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public JavaMailSender getMailSender() {
        return _mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        _mailSender = mailSender;
    }
}
