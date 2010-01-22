package gs.web.community;

import gs.web.util.ReadWriteController;
import gs.web.util.UrlBuilder;
import gs.data.community.*;
import gs.data.content.cms.CmsDiscussionBoard;
import gs.data.content.cms.ICmsDiscussionBoardDao;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * @author Dave Roy <mailto:droy@greatschools.org>
 */
public class ReportContentService extends SimpleFormController implements IReportContentService {
    protected final Log _log = LogFactory.getLog(getClass());
    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private IDiscussionDao _discussionDao;
    private IDiscussionReplyDao _discussionReplyDao;
    private IUserDao _userDao;
    private JavaMailSender _mailSender;
    private String _moderationEmail;

    public void reportContent(User reporter, User reportee, HttpServletRequest request, int contentId, ReportType type, String reason) {
        String urlToContent = getLinkForContent(request, contentId, type);
        reportContent(reporter, reportee, urlToContent, type, reason);
    }

    public void reportContent(User reporter, User reportee, String urlToContent, ReportType type, String reason) {
        sendEmail(urlToContent, type, reporter, reportee, reason);
    }

    protected void sendEmail(String urlToContent, ReportType contentType,
                             User reporter, User reportee, String reason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(_moderationEmail);
        message.setFrom(_moderationEmail);
        message.setSentDate(new Date());
        message.setSubject("Reported content alert");
        StringBuffer body = new StringBuffer();
        body.append("User ").append(formatUserString(reporter));
        body.append(" has reported ");
        if (contentType != IReportContentService.ReportType.member) {
            body.append("a ").append(contentType).append(" by ");
        }
        body.append("user ").append(formatUserString(reportee));
        body.append(":\n\n");
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

    protected StringBuffer formatUserString(User user) {
        StringBuffer text = new StringBuffer();
        text.append(user.getUserProfile().getScreenName());
        text.append(" (id=").append(user.getId()).append(", email= ").append(user.getEmail());
        text.append(")");

        return text;
    }

    // for a user, the relevant page to go to is the user profile page
    // for a discussion -- the discussion detail page
    // for a discussion reply -- whichever page of the discussion detail page the reply would appear on
    protected String getLinkForContent(HttpServletRequest request, int contentId,
                                       ReportType contentType) {
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

    public String getModerationEmail() {
        return _moderationEmail;
    }

    public void setModerationEmail(String moderationEmail) {
        _moderationEmail = moderationEmail;
    }
}