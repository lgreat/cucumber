package gs.web.community;

import gs.data.school.review.IReviewDao;
import gs.data.school.review.Review;
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
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class ReportContentService extends SimpleFormController
        implements IReportContentService, ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());
    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private IDiscussionDao _discussionDao;
    private IDiscussionReplyDao _discussionReplyDao;
    private IUserDao _userDao;
    private IReportedEntityDao _reportedEntityDao;
    private IReviewDao _reviewDao;
    private JavaMailSender _mailSender;
    private String _moderationEmail;
    private static final String SCHOOL_REVIEW_DISABLED_STATUS = "u";

    public void reportContent(User reporter, User reportee, HttpServletRequest request, int contentId, 
                              ReportedEntity.ReportedEntityType type, String reason) {
        try {
            String urlToContent = null;
            switch (type) {
                case discussion:
                    Discussion d = _discussionDao.findById(contentId);
                    urlToContent = getLinkForEntity(request, d);
                    if (d != null && urlToContent != null) {
                        int numTimesReported = _reportedEntityDao.getNumberTimesReported(type, contentId);
                        _reportedEntityDao.reportEntity(reporter, type, contentId, reason);
                        if (numTimesReported == 1) {
                            // if this is the second time this discussion is reported, disable it
                            d.setActive(false);
                            _discussionDao.save(d);
                        }
                    }
                    break;
                case reply:
                    DiscussionReply reply = _discussionReplyDao.findById(contentId);
                    urlToContent = getLinkForEntity(request, reply);
                    if (reply != null && urlToContent != null) {
                        int numTimesReported = _reportedEntityDao.getNumberTimesReported(type, contentId);
                        _reportedEntityDao.reportEntity(reporter, type, contentId, reason);
                        if (numTimesReported == 1) {
                            // if this is the second time this reply is reported, disable it
                            reply.setActive(false);
                            _discussionReplyDao.save(reply);

                            // update number of replies for discussion
                            Discussion discussion = reply.getDiscussion();
                            int numReplies = _discussionReplyDao.getTotalReplies(discussion);
                            discussion.setNumReplies(numReplies);
                            _discussionDao.saveKeepDates(discussion);
                        }
                    }
                    break;
                case member:
                    User user = null;
                    try {
                        user = _userDao.findUserFromId(contentId);
                    } catch (ObjectRetrievalFailureException orfe) {
                        // handled below
                    }
                    urlToContent = getLinkForEntity(request, user);
                    if (user != null && user.getUserProfile() != null && urlToContent != null) {
                        _reportedEntityDao.reportEntity(reporter, type, contentId, reason);
                    }
                    break;
                case schoolReview:
                    Review review = _reviewDao.getReview(contentId);
                    int numTimesReported = _reportedEntityDao.getNumberTimesReported(type, contentId);
                    _reportedEntityDao.reportEntity(reporter, type, contentId, reason);
                    if (numTimesReported == 1) {
                        // if this is the second time this review is reported, disable it
                        review.setStatus(SCHOOL_REVIEW_DISABLED_STATUS);
                        _reviewDao.saveReview(review);
                    }
                    break;
            }
            if (type != ReportedEntity.ReportedEntityType.schoolReview) {
                // don't send emails for reporting school reviews
                if (urlToContent != null) {
                    sendEmail(urlToContent, type, reporter, reportee, reason);
                } else {
                    _log.warn("Unable to determine URL for reported content " + type + ":" + contentId +
                            ", reason \"" + reason + "\"");
                }
            }
        } catch (Exception e) {
            _log.error("Error reporting content " + type + ": " + contentId, e);
        }
    }
    
    protected void sendEmail(String urlToContent, ReportedEntity.ReportedEntityType contentType,
                             User reporter, User reportee, String reason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(_moderationEmail);
        message.setFrom(_moderationEmail);
        message.setSentDate(new Date());
        message.setSubject("Reported content alert");
        StringBuffer body = new StringBuffer();
        body.append("User ").append(formatUserString(reporter));
        body.append(" has reported ");
        if (contentType != ReportedEntity.ReportedEntityType.member) {
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

    protected String getLinkForEntity(HttpServletRequest request, Discussion d) {
        if (d == null || d.getBoardId() == null) {
            return null;
        }
        UrlBuilder urlBuilder = getUrlBuilderForDiscussion(d);
        if (urlBuilder != null) {
            return urlBuilder.asFullUrl(request);
        }
        return null;
    }

    protected String getLinkForEntity(HttpServletRequest request, DiscussionReply reply) {
        if (reply == null || reply.getDiscussion() == null || reply.getDiscussion().getBoardId() == null) {
            return null;
        }
        UrlBuilder urlBuilder = getUrlBuilderForDiscussion(reply.getDiscussion());
        if (urlBuilder != null) {
            urlBuilder.setParameter("discussionReplyId", String.valueOf(reply.getId()));
            return urlBuilder.asFullUrl(request) + "#reply_" + reply.getId();
        }
        return null;
    }

    protected String getLinkForEntity(HttpServletRequest request, User u) {
        if (u == null || u.getUserProfile() == null) {
            return null;
        }
        return new UrlBuilder(u, UrlBuilder.USER_PROFILE).asFullUrl(request);
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

    public IReportedEntityDao getReportedEntityDao() {
        return _reportedEntityDao;
    }

    public void setReportedEntityDao(IReportedEntityDao reportedEntityDao) {
        _reportedEntityDao = reportedEntityDao;
    }

    public IReviewDao getReviewDao() {
        return _reviewDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }
}