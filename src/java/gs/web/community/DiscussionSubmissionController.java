package gs.web.community;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.HtmlUtils;
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
import gs.web.jsp.Util;
import gs.data.community.*;
import gs.data.content.cms.CmsDiscussionBoard;
import gs.data.content.cms.ICmsDiscussionBoardDao;
import gs.data.content.cms.CmsTopicCenter;
import gs.data.cms.IPublicationDao;
import gs.data.search.SolrService;
import gs.data.security.Permission;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
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
    private SolrService _solrService;
    private IUserDao _userDao;
    private IAlertWordDao _alertWordDao;

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object commandObj, BindException errors) throws Exception {
        DiscussionSubmissionCommand command = (DiscussionSubmissionCommand) commandObj;

        /*
        // TODO-8925
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = sessionContext.getUser();
        if (user != null && !user.getUserProfile().isActive()) {
            UrlBuilder urlBuilder = new UrlBuilder(board.getContentKey(), board.getFullUri());
            command.setRedirect(urlBuilder.asSiteRelative(request));

            PageHelper.logout(request, response);
            return new ModelAndView(new RedirectView(command.getRedirect()));
        }
        */

        if (StringUtils.equals("editDiscussion", command.getType())) {
            handleEditDiscussionSubmission(request, response, command);
        } else if (command.getDiscussionId() != null) {
            handleDiscussionReplySubmission(request, response, command);
        } else if (command.getDiscussionBoardId() != null) {
            handleDiscussionSubmission(request, response, command);
        } else if (command.getTopicCenterId() != null) {
            handleDiscussionSubmissionByTopicCenter(request, response, command);
        } else {
            _log.warn("Unknown submission type -- has no discussion id or topic center id");
        }

        return new ModelAndView(new RedirectView(command.getRedirect()));
    }

    protected void handleDiscussionSubmissionByTopicCenter
            (HttpServletRequest request, HttpServletResponse response, DiscussionSubmissionCommand command)
            throws IllegalStateException {
        CmsTopicCenter topicCenter = _publicationDao.populateByContentId
                (command.getTopicCenterId(), new CmsTopicCenter());

        if (topicCenter == null) {
            _log.warn("Attempt to submit with unknown topic center id (" +
                    command.getTopicCenterId() + ") rejected");
            throw new IllegalStateException("Discussion submission with unknown topic center id! id=" +
                    command.getTopicCenterId());
        }

        command.setDiscussionBoardId(topicCenter.getDiscussionBoardId());

        handleDiscussionSubmission(request, response, command);
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

        CmsDiscussionBoard board = _cmsDiscussionBoardDao.get(command.getDiscussionBoardId());

        if (board == null) {
            _log.warn("Attempt to submit with unknown discussion board id (" +
                    command.getDiscussionBoardId() + ") rejected");
            throw new IllegalStateException("Discussion submission with unknown discussion board id! id=" +
                    command.getDiscussionBoardId());
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
            // TODO: more validation?

            Discussion discussion = new Discussion();
            discussion.setAuthorId(user.getId());
            discussion.setBoardId(board.getContentKey().getIdentifier());
            discussion.setBody(HtmlUtils.htmlEscape(StringUtils.abbreviate(command.getBody(), DISCUSSION_BODY_MAXIMUM_LENGTH)));
            discussion.setTitle(HtmlUtils.htmlEscape(StringUtils.abbreviate(command.getTitle(), DISCUSSION_TITLE_MAXIMUM_LENGTH)));

            if (!user.hasPermission(Permission.COMMUNITY_VIEW_REPORTED_POSTS) &&
                    (_alertWordDao.hasAlertWord(discussion.getBody()) || _alertWordDao.hasAlertWord(discussion.getTitle()))) {
                // profanity filter
                // Moderators are always allowed to post profanity
                // TODO: report post
                discussion.setActive(false);
                _log.warn("Discussion edit triggers profanity filter.");
            }

            _discussionDao.save(discussion);
            // needed for indexing
            discussion.setUser(user);
            // needed for indexing
            discussion.setDiscussionBoard(board);
            try {
                _solrService.indexDocument(discussion);
            } catch (Exception e) {
                _log.error("Could not index discussion " + discussion.getId() + " using solr", e);
            }

            OmnitureTracking ot = new CookieBasedOmnitureTracking(request, response);
            ot.addSuccessEvent(CookieBasedOmnitureTracking.SuccessEvent.CommunityDiscussionPost);

            if (StringUtils.isEmpty(command.getRedirect())) {
                // default to forwarding to the discussion detail page
                UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.COMMUNITY_DISCUSSION, board.getFullUri(),
                        Long.valueOf(discussion.getId()));
                command.setRedirect(urlBuilder.asSiteRelative(request));
            }
        }
    }

    protected void handleEditDiscussionSubmission
            (HttpServletRequest request, HttpServletResponse response, DiscussionSubmissionCommand command)
            throws IllegalStateException {
        Discussion discussion = _discussionDao.findById(command.getDiscussionId());
        if (discussion == null) {
            _log.warn("Attempt to edit discussion with unknown discussion id! id=" +
                    command.getDiscussionId());
            if (StringUtils.isEmpty(command.getRedirect())) {
                throw new IllegalStateException("Discussion edit with unknown discussion id! id=" +
                        command.getDiscussionId());
            } else {
                return;
            }
        }

        CmsDiscussionBoard board = _cmsDiscussionBoardDao.get(discussion.getBoardId());

        if (board == null) {
            _log.warn("Attempt to submit with unknown discussion board id (" +
                    discussion.getBoardId() + ") rejected");
            throw new IllegalStateException("Discussion submission with unknown discussion board id! id=" +
                    discussion.getBoardId());
        }

        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.COMMUNITY_DISCUSSION, board.getFullUri(),
                Long.valueOf(discussion.getId()));
        if (StringUtils.isEmpty(command.getRedirect())) {
            // default to forwarding to the discussion detail page
            command.setRedirect(urlBuilder.asSiteRelative(request));
        }

        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        // error checking
        if (sessionContext == null) {
            _log.warn("Attempt to submit with no SessionContext rejected");
            return;
        }
        if (!PageHelper.isMemberAuthorized(request) || sessionContext.getUser() == null) {
            _log.warn("Attempt to edit with no valid user rejected");
            return;
        }
        User user = sessionContext.getUser();

        // validation
        boolean canEdit = user.hasPermission(Permission.COMMUNITY_VIEW_REPORTED_POSTS);

        if (StringUtils.length(command.getBody()) < DISCUSSION_BODY_MINIMUM_LENGTH) {
            _log.warn("Attempt to edit with body length < " + DISCUSSION_BODY_MINIMUM_LENGTH + " ignored");
        } else if (!canEdit && !Util.dateWithinXMinutes(discussion.getDateCreated(), 150)) {
            _log.warn("Attempt to edit after too much time has passed. discussion_id=" +
                    discussion.getId() + ";created_date=" + discussion.getDateCreated());
        } else if (!canEdit && !discussion.getAuthorId().equals(user.getId())) {
            _log.warn("Attempt to edit but user != author! discussion author_id=" +
                    discussion.getAuthorId() + "; user_id=" + user.getId());
        } else {
            // TODO: more validation?

            discussion.setBody(HtmlUtils.htmlEscape(StringUtils.abbreviate(command.getBody(), DISCUSSION_BODY_MAXIMUM_LENGTH)));
            discussion.setTitle(HtmlUtils.htmlEscape(StringUtils.abbreviate(command.getTitle(), DISCUSSION_TITLE_MAXIMUM_LENGTH)));
            discussion.setDateUpdated(new Date());

            if (!user.hasPermission(Permission.COMMUNITY_VIEW_REPORTED_POSTS) &&
                    (_alertWordDao.hasAlertWord(command.getBody()) || _alertWordDao.hasAlertWord(command.getTitle()))) {
                // profanity filter
                // Moderators are always allowed to post profanity
                // TODO: report post
                discussion.setActive(false);
                _log.warn("Discussion edit triggers profanity filter.");
            }

            _discussionDao.saveKeepDates(discussion);
            // needed for indexing
            if (discussion.getAuthorId().equals(user.getId())) {
                discussion.setUser(user);
            } else {
                User author = _userDao.findUserFromId(discussion.getAuthorId());
                discussion.setUser(author);
            }
            // needed for indexing
            discussion.setDiscussionBoard(board);
            try {
                _solrService.updateDocument(discussion);
            } catch (Exception e) {
                _log.error("Could not index discussion " + discussion.getId() + " using solr", e);
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

        CmsDiscussionBoard board = _cmsDiscussionBoardDao.get(discussion.getBoardId());

        if (board == null) {
            _log.warn("Attempt to submit with unknown discussion board id (" +
                    discussion.getBoardId() + ") rejected");
            throw new IllegalStateException("Discussion submission with unknown discussion board id! id=" +
                    discussion.getBoardId());
        }

        // validation
        if (StringUtils.length(command.getBody()) < REPLY_BODY_MINIMUM_LENGTH) {
            // sample code to store reply body for re-display in form
//            SitePrefCookie sitePrefCookie = new SitePrefCookie(request, response);
//            sitePrefCookie.setProperty(COOKIE_REPLY_BODY_PROPERTY, command.getBody());
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.COMMUNITY_DISCUSSION, board.getFullUri(),
                    Long.valueOf(discussion.getId()));
            command.setRedirect(urlBuilder.asSiteRelative(request));
            _log.warn("Attempt to submit with body length < " + REPLY_BODY_MINIMUM_LENGTH + " ignored");
        } else {
            // TODO: more validation?

            boolean newReply = false;
            boolean canSave = false;
            DiscussionReply reply;
            if (command.getDiscussionReplyId() == null) {
                reply = new DiscussionReply();
                newReply = true;
                canSave = true;
            } else {
                reply = _discussionReplyDao.findById(command.getDiscussionReplyId());
                // choosing 150 minutes instead of 120 to give people time to compose their changes
                boolean canEdit = user.hasPermission(Permission.COMMUNITY_VIEW_REPORTED_POSTS);
                if (canEdit || (Util.dateWithinXMinutes(reply.getDateCreated(), 150) &&
                    reply.getAuthorId().equals(user.getId()))) {
                    canSave = true;
                }
            }

            if (canSave) {
                reply.setDiscussion(discussion);
                reply.setBody(HtmlUtils.htmlEscape(StringUtils.abbreviate(command.getBody(), REPLY_BODY_MAXIMUM_LENGTH)));
                if (!user.hasPermission(Permission.COMMUNITY_VIEW_REPORTED_POSTS) &&
                        _alertWordDao.hasAlertWord(reply.getBody())) {
                    // profanity filter
                    // Moderators are always allowed to post profanity
                    // TODO: report post
                    reply.setActive(false);
                    _log.warn("Reply triggers profanity filter.");
                }
                if (newReply) {
                    reply.setAuthorId(user.getId());
                    _discussionReplyDao.save(reply);
                } else {
                    reply.setDateUpdated(new Date());
                    _discussionReplyDao.saveKeepDates(reply);
                }
            }

            // omniture success event only if new discussion reply
            if (command.getDiscussionReplyId() == null) {
                OmnitureTracking ot = new CookieBasedOmnitureTracking(request, response);
                ot.addSuccessEvent(CookieBasedOmnitureTracking.SuccessEvent.CommunityDiscussionReplyPost);
            }

            if (StringUtils.isEmpty(command.getRedirect())) {
                // default to forwarding to the discussion detail page
                UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.COMMUNITY_DISCUSSION, board.getFullUri(),
                        Long.valueOf(discussion.getId()));
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

    public SolrService getSolrService() {
        return _solrService;
    }

    public void setSolrService(SolrService solrService) {
        _solrService = solrService;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public IAlertWordDao getAlertWordDao() {
        return _alertWordDao;
    }

    public void setAlertWordDao(IAlertWordDao alertWordDao) {
        _alertWordDao = alertWordDao;
    }
}
