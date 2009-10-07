package gs.web.community;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.validation.BindException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import gs.web.util.ReadWriteController;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.tracking.OmnitureTracking;
import gs.web.tracking.CookieBasedOmnitureTracking;
import gs.data.community.*;
import gs.data.content.cms.CmsDiscussionBoard;
import gs.data.content.cms.ICmsDiscussionBoardDao;
import gs.data.content.cms.CmsTopicCenter;
import gs.data.cms.IPublicationDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class DiscussionSubmissionController extends SimpleFormController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());

    public final static int REPLY_BODY_MINIMUM_LENGTH = 5;
    public final static int REPLY_BODY_MAXIMUM_LENGTH = 1024;
    public final static int DISCUSSION_BODY_MINIMUM_LENGTH = 5;
    public final static int DISCUSSION_BODY_MAXIMUM_LENGTH = 1024;
    public final static int DISCUSSION_TITLE_MINIMUM_LENGTH = 5;
    public final static int DISCUSSION_TITLE_MAXIMUM_LENGTH = 128;
    public final static String COOKIE_REPLY_BODY_PROPERTY = "replyBody";

    private IDiscussionReplyDao _discussionReplyDao;
    private IDiscussionDao _discussionDao;
    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private IPublicationDao _publicationDao;

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object commandObj, BindException errors) throws Exception {
        DiscussionSubmissionCommand command = (DiscussionSubmissionCommand) commandObj;

        if (command.getDiscussionId() != null) {
            handleDiscussionReplySubmission(request, response, command);
        } else if (command.getTopicCenterId() != null) {
            handleDiscussionSubmission(request, response, command);
        } else {
            _log.warn("Unknown submission type -- has no discussion id or topic center id");
        }

        return new ModelAndView(new RedirectView(command.getRedirect()));
    }

    protected void handleDiscussionSubmission
            (HttpServletRequest request, HttpServletResponse response, DiscussionSubmissionCommand command)
            throws IllegalStateException {
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
        User user = sessionContext.getUser();

        CmsTopicCenter topicCenter = _publicationDao.populateByContentId
                (command.getTopicCenterId(), new CmsTopicCenter());

        if (topicCenter == null) {
            _log.warn("Attempt to submit with unknown topic center id (" +
                    command.getTopicCenterId() + ") rejected");
            throw new IllegalStateException("Discussion submission with unknown topic center id! id=" +
                    command.getTopicCenterId());
        }

        CmsDiscussionBoard board = _cmsDiscussionBoardDao.get(topicCenter.getDiscussionBoardId());

        if (board == null) {
            _log.warn("Attempt to submit with unknown discussion board id (" +
                    topicCenter.getDiscussionBoardId() + ") rejected");
            throw new IllegalStateException("Discussion submission with unknown discussion board id! id=" +
                    topicCenter.getDiscussionBoardId());
        }

        // validation
        if (StringUtils.length(command.getTitle()) < DISCUSSION_TITLE_MINIMUM_LENGTH) {
            UrlBuilder urlBuilder = new UrlBuilder(board.getContentKey(), board.getFullUri());
            command.setRedirect(urlBuilder.asSiteRelative(request));
            _log.warn("Attempt to submit with title length < " + DISCUSSION_TITLE_MINIMUM_LENGTH + " ignored");
        } else if (StringUtils.length(command.getBody()) < DISCUSSION_BODY_MINIMUM_LENGTH) {
            UrlBuilder urlBuilder = new UrlBuilder(board.getContentKey(), board.getFullUri());
            command.setRedirect(urlBuilder.asSiteRelative(request));
            _log.warn("Attempt to submit with body length < " + DISCUSSION_BODY_MINIMUM_LENGTH + " ignored");
        } else {
            // TODO: profanity filter
            // TODO: more validation?
            // TODO: sanitize string (strip HTML? JS? SQL?)?

            Discussion discussion = new Discussion();
            discussion.setAuthorId(user.getId());
            discussion.setBoardId(board.getContentKey().getIdentifier());
            discussion.setBody(StringUtils.abbreviate(command.getBody(), DISCUSSION_BODY_MAXIMUM_LENGTH));
            discussion.setTitle(StringUtils.abbreviate(command.getTitle(), DISCUSSION_TITLE_MAXIMUM_LENGTH));

            _discussionDao.save(discussion);

            OmnitureTracking ot = new CookieBasedOmnitureTracking(request, response);
            ot.addSuccessEvent(CookieBasedOmnitureTracking.SuccessEvent.CommunityDiscussionPost);

            if (StringUtils.isEmpty(command.getRedirect())) {
                // default to forwarding to the discussion board page
                UrlBuilder urlBuilder = new UrlBuilder(board.getContentKey(), board.getFullUri());
                command.setRedirect(urlBuilder.asSiteRelative(request));
            }
        }
    }

    /**
     * @throws IllegalStateException if this method is called with invalid parameters in the request
     */
    protected void handleDiscussionReplySubmission
            (HttpServletRequest request, HttpServletResponse response, DiscussionSubmissionCommand command)
            throws IllegalStateException {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        // error checking
        if (sessionContext == null) {
            _log.warn("Attempt to submit with no SessionContext rejected");
            throw new IllegalStateException("No SessionContext found in request");
        }
        if (!PageHelper.isMemberAuthorized(request) || sessionContext.getUser() == null) {
            _log.warn("Attempt to submit with no valid user rejected");
            throw new IllegalStateException("Discussion reply submission occurred but no valid user is cookied!");
        }
        User user = sessionContext.getUser();
        Discussion discussion = _discussionDao.findById(command.getDiscussionId());
        if (discussion == null) {
            _log.warn("Attempt to submit with unknown discussion id (" + command.getDiscussionId() + ") rejected");
            throw new IllegalStateException("Discussion reply submission with unknown discussion id! id=" +
                    command.getDiscussionId());
        }

        // validation
        if (StringUtils.length(command.getBody()) < REPLY_BODY_MINIMUM_LENGTH) {
            // sample code to store reply body for re-display in form
//            SitePrefCookie sitePrefCookie = new SitePrefCookie(request, response);
//            sitePrefCookie.setProperty(COOKIE_REPLY_BODY_PROPERTY, command.getBody());
            UrlBuilder urlBuilder = new UrlBuilder(discussion);
            command.setRedirect(urlBuilder.asSiteRelative(request));
            _log.warn("Attempt to submit with body length < " + REPLY_BODY_MINIMUM_LENGTH + " ignored");
        } else {

            // TODO: profanity filter
            // TODO: more validation?
            // TODO: sanitize string (strip HTML? JS? SQL?)?

            DiscussionReply reply = new DiscussionReply();
            reply.setDiscussion(discussion);
            reply.setBody(StringUtils.abbreviate(command.getBody(), REPLY_BODY_MAXIMUM_LENGTH));
            reply.setAuthorId(user.getId());
            _discussionReplyDao.save(reply);

            OmnitureTracking ot = new CookieBasedOmnitureTracking(request, response);
            ot.addSuccessEvent(CookieBasedOmnitureTracking.SuccessEvent.CommunityDiscussionReplyPost);

            if (StringUtils.isEmpty(command.getRedirect())) {
                // default to forwarding to the discussion detail page
                UrlBuilder urlBuilder = new UrlBuilder(discussion);
                command.setRedirect(urlBuilder.asSiteRelative(request));
            }
        }
    }

    public IDiscussionReplyDao getDiscussionReplyDao() {
        return _discussionReplyDao;
    }

    public void setDiscussionReplyDao(IDiscussionReplyDao discussionReplyDao) {
        _discussionReplyDao = discussionReplyDao;
    }

    public IDiscussionDao getDiscussionDao() {
        return _discussionDao;
    }

    public void setDiscussionDao(IDiscussionDao discussionDao) {
        _discussionDao = discussionDao;
    }

    public ICmsDiscussionBoardDao getCmsDiscussionBoardDao() {
        return _cmsDiscussionBoardDao;
    }

    public void setCmsDiscussionBoardDao(ICmsDiscussionBoardDao cmsDiscussionBoardDao) {
        _cmsDiscussionBoardDao = cmsDiscussionBoardDao;
    }

    public IPublicationDao getPublicationDao() {
        return _publicationDao;
    }

    public void setPublicationDao(IPublicationDao publicationDao) {
        _publicationDao = publicationDao;
    }
}
