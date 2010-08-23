package gs.web.community;

import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.web.jsp.link.DiscussionTagHandler;
import gs.web.util.UrlUtil;
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
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class DiscussionSubmissionController extends SimpleFormController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());

    public final static int REPLY_BODY_MINIMUM_LENGTH = 5;
    public final static int REPLY_BODY_MAXIMUM_LENGTH = 128000;
    public final static int DISCUSSION_BODY_MINIMUM_LENGTH = 5;
    public final static int DISCUSSION_BODY_MAXIMUM_LENGTH = 128000;
    public final static int DISCUSSION_TITLE_MINIMUM_LENGTH = 5;
    public final static int DISCUSSION_TITLE_MAXIMUM_LENGTH = 128;
    public final static Long GENERAL_PARENTING_DISCUSSION_BOARD_ID = 2420L;
    public final static String COOKIE_REPLY_BODY_PROPERTY = "replyBody";

    private IDiscussionReplyDao _discussionReplyDao;
    private IDiscussionDao _discussionDao;
    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private IPublicationDao _publicationDao;
    private SolrService _solrService;
    private IUserDao _userDao;
    private IAlertWordDao _alertWordDao;
    private IReportContentService _reportContentService;
    private ExactTargetAPI _exactTargetAPI;
    private ISubscriptionDao _subscriptionDao;

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

        if (StringUtils.equals("cbiAdviceReply", command.getType())
                || StringUtils.equals("cbiTipReply", command.getType())) {
            handleCBIReplySubmission(request, response, command);
        } else if (StringUtils.equals("cbiAdviceDiscussion", command.getType())
                || StringUtils.equals("cbiTipDiscussion", command.getType())) {
            handleDiscussionSubmission(request, response, command, false);
        } else if (StringUtils.equals("editDiscussion", command.getType())) {
            handleEditDiscussionSubmission(request, response, command);
        } else if (command.getDiscussionId() != null) {
            handleDiscussionReplySubmission(request, response, command);
        } else if (command.getDiscussionBoardId() != null) {
            handleDiscussionSubmission(request, response, command, true);
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

        Long discussionBoardId;
        if (GENERAL_PARENTING_DISCUSSION_BOARD_ID.equals(command.getTopicCenterId())) {
            discussionBoardId = command.getTopicCenterId();
        } else {
            CmsTopicCenter topicCenter = _publicationDao.populateByContentId
                    (command.getTopicCenterId(), new CmsTopicCenter());

            if (topicCenter == null) {
                _log.warn("Attempt to submit with unknown topic center id (" +
                        command.getTopicCenterId() + ") rejected");
                throw new IllegalStateException("Discussion submission with unknown topic center id! id=" +
                        command.getTopicCenterId());
            }
            discussionBoardId = topicCenter.getDiscussionBoardId();
        }

        command.setDiscussionBoardId(discussionBoardId);

        handleDiscussionSubmission(request, response, command, true);
    }

    public static String cleanUpText(String text, int maxLength) {
        // first truncate to max length in case the submission is really long
        text = StringUtils.abbreviate(text, maxLength);
        // then html escape the body
        text = HtmlUtils.htmlEscape(text);
        // then put in line breaks
        text = StringUtils.replace(text, "\r\n", "<br/>");
        text = StringUtils.replace(text, "\r", "<br/>");
        text = StringUtils.replace(text, "\n", "<br/>");
        // then clean up high ascii characters
        text = StringUtils.replace(text, "\u0082", ",");
        text = StringUtils.replace(text, "\u0085", "...");
        text = StringUtils.replace(text, "\u0091", "`");
        text = StringUtils.replace(text, "\u0092", "'");
        text = StringUtils.replace(text, "\u0093", "\"");
        text = StringUtils.replace(text, "\u0094", "\"");
        text = StringUtils.replace(text, "\u0095", "*");
        text = StringUtils.replace(text, "\u0096", "-");
        text = StringUtils.replace(text, "\u0097", "--");

        return text;
    }
    
    protected void handleDiscussionSubmission
            (HttpServletRequest request, HttpServletResponse response, DiscussionSubmissionCommand command, boolean doWordFilter)
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
            Discussion discussion = new Discussion();
            discussion.setAuthorId(user.getId());
            discussion.setBoardId(board.getContentKey().getIdentifier());
            discussion.setBody(cleanUpText(command.getBody(), DISCUSSION_BODY_MAXIMUM_LENGTH));
            discussion.setTitle(cleanUpText(command.getTitle(), DISCUSSION_TITLE_MAXIMUM_LENGTH));

            _discussionDao.save(discussion);
            // needed for indexing
            discussion.setUser(user);
            // needed for indexing
            discussion.setDiscussionBoard(board);
            if (!StringUtils.equals("cbiAdviceDiscussion", command.getType()) && !StringUtils.equals("cbiTipDiscussion", command.getType())) {
                try {
                    _solrService.indexDocument(discussion);
                } catch (Exception e) {
                    _log.error("Could not index discussion " + discussion.getId() + " using solr", e);
                }
            }

            if (doWordFilter) {
                String bodyWord = _alertWordDao.hasAlertWord(discussion.getBody());
                String titleWord = _alertWordDao.hasAlertWord(discussion.getTitle());
                if (!user.hasPermission(Permission.COMMUNITY_VIEW_REPORTED_POSTS) &&
                        (bodyWord != null || titleWord != null)) {
                    // profanity filter
                    // Moderators are always allowed to post profanity

                    String reason = "Contains the alert word \"";
                    if (bodyWord != null) {
                        reason += bodyWord;
                    } else {
                        reason += titleWord;
                    }
                    reason += "\"";
                    _reportContentService.reportContent(getAlertWordFilterUser(), user, request, discussion.getId(),
                                                        ReportedEntity.ReportedEntityType.discussion, reason);
                    //discussion.setActive(false);
                    _log.warn("Discussion submission triggers profanity filter.");
                }
            }

            OmnitureTracking ot = new CookieBasedOmnitureTracking(request, response);
            if (StringUtils.equals("cbiTipDiscussion", command.getType())) {
                ot.addSuccessEvent(CookieBasedOmnitureTracking.SuccessEvent.CBTipDiscussionPost);
            } else if (StringUtils.equals("cbiAdviceDiscussion", command.getType())) {
                ot.addSuccessEvent(CookieBasedOmnitureTracking.SuccessEvent.CBAdviceDiscussionPost);
            } else {
                ot.addSuccessEvent(CookieBasedOmnitureTracking.SuccessEvent.CommunityDiscussionPost);
            }

            if (StringUtils.isEmpty(command.getRedirect())) {
                // default to forwarding to the discussion detail page
                String urlToContent = getDiscussionUrl(request, board.getFullUri(), Long.valueOf(discussion.getId()));
                command.setRedirect(urlToContent);
            } else if (StringUtils.contains(command.getRedirect(), "*ID*")) {
                command.setRedirect(StringUtils.replace(command.getRedirect(), "*ID*", String.valueOf(discussion.getId())));
            }
        }
    }

    protected User getAlertWordFilterUser() {
        User reporter = new User();
        reporter.setId(-1);
        reporter.setEmail(_reportContentService.getModerationEmail());
        reporter.setUserProfile(new UserProfile());
        reporter.getUserProfile().setScreenName("gs_alert_word_filter");

        return reporter;
    }

    protected String getDiscussionUrl(HttpServletRequest request, String fullUri, Long contentId) {
        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.COMMUNITY_DISCUSSION, fullUri, contentId);
        return urlBuilder.asFullUrl(request);
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
            discussion.setBody(cleanUpText(command.getBody(), DISCUSSION_BODY_MAXIMUM_LENGTH));
            discussion.setTitle(cleanUpText(command.getTitle(), DISCUSSION_TITLE_MAXIMUM_LENGTH));
            discussion.setDateUpdated(new Date());

            String bodyWord = _alertWordDao.hasAlertWord(command.getBody());
            String titleWord = _alertWordDao.hasAlertWord(command.getTitle());
            if (!user.hasPermission(Permission.COMMUNITY_VIEW_REPORTED_POSTS) &&
                    (bodyWord != null || titleWord != null)) {
                // profanity filter
                // Moderators are always allowed to post profanity

                String reason = "Contains the alert word \"";
                if (bodyWord != null) {
                    reason += bodyWord;
                } else {
                    reason += titleWord;
                }
                reason += "\"";
                _reportContentService.reportContent(getAlertWordFilterUser(), user, request, discussion.getId(),
                                                    ReportedEntity.ReportedEntityType.discussion,
                                                    reason);
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

    protected void handleCBIReplySubmission
            (HttpServletRequest request, HttpServletResponse response, DiscussionSubmissionCommand command)
            throws IllegalStateException {
        handleDiscussionReplySubmissionHelper(request, response, command, false, false,true);
    }

    protected void handleDiscussionReplySubmission
            (HttpServletRequest request, HttpServletResponse response, DiscussionSubmissionCommand command)
            throws IllegalStateException {
        handleDiscussionReplySubmissionHelper(request, response, command, true, true,false);
    }

    /**
     * @throws IllegalStateException if this method is called with invalid parameters in the request
     */
    protected DiscussionReply handleDiscussionReplySubmissionHelper
            (HttpServletRequest request, HttpServletResponse response, DiscussionSubmissionCommand command,
             boolean doWordFilter, boolean sendGSNotification,boolean sendCBNotification)
            throws IllegalStateException {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        validateUser(sessionContext,request);
        User user = sessionContext.getUser();
        Discussion discussion = validateDiscussion(command);
        CmsDiscussionBoard board = validateBoard(discussion);

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
                replyHelper(command,discussion,reply,newReply,user);
            }

            // omniture success event only if new discussion reply
            if (command.getDiscussionReplyId() == null) {
                OmnitureTracking ot = new CookieBasedOmnitureTracking(request, response);
                if (StringUtils.equals("cbiTipReply", command.getType())) {
                    ot.addSuccessEvent(CookieBasedOmnitureTracking.SuccessEvent.CBTipReplyPost);
                } else if (StringUtils.equals("cbiAdviceReply", command.getType())) {
                    ot.addSuccessEvent(CookieBasedOmnitureTracking.SuccessEvent.CBAdviceReplyPost);
                } else {
                    ot.addSuccessEvent(CookieBasedOmnitureTracking.SuccessEvent.CommunityDiscussionReplyPost);
                }
            }

            if (doWordFilter) {
                filterWords(reply,user,request);
            }
            doRedirect(command,board,discussion,reply,request);

            if (newReply && sendGSNotification) {
                notifyAboutGSReply(request, sessionContext, board, discussion, reply, user);
            }
            if (newReply && sendCBNotification) {
                notifyAboutCBReply(request, sessionContext, board, discussion, reply, user);
            }

            return reply;
        }
        return null;
    }

    public void validateUser(SessionContext sessionContext, HttpServletRequest request){
        if (sessionContext == null) {
            _log.warn("Attempt to submit with no SessionContext rejected");
            throw new IllegalStateException("No SessionContext found in request");
        }
        if (!PageHelper.isMemberAuthorized(request) || sessionContext.getUser() == null) {
            _log.warn("Attempt to submit with no valid user rejected");
            throw new IllegalStateException("Discussion reply submission occurred but no valid user is cookied!");
        }
    }

    public Discussion validateDiscussion(DiscussionSubmissionCommand command){
        Discussion discussion = _discussionDao.findById(command.getDiscussionId());
        if (discussion == null) {
            _log.warn("Attempt to submit with unknown discussion id (" + command.getDiscussionId() + ") rejected");
            throw new IllegalStateException("Discussion reply submission with unknown discussion id! id=" +
                    command.getDiscussionId());
        }    
        return discussion;
    }

    public CmsDiscussionBoard validateBoard(Discussion discussion){
        CmsDiscussionBoard board = _cmsDiscussionBoardDao.get(discussion.getBoardId());
        if (board == null) {
            _log.warn("Attempt to submit with unknown discussion board id (" +
                    discussion.getBoardId() + ") rejected");
            throw new IllegalStateException("Discussion submission with unknown discussion board id! id=" +
                    discussion.getBoardId());
        }
        return board;
    }
    
    public void replyHelper(DiscussionSubmissionCommand command, Discussion discussion, DiscussionReply reply,
                            boolean isNewReply, User user){
        reply.setDiscussion(discussion);
        reply.setBody(cleanUpText(command.getBody(), REPLY_BODY_MAXIMUM_LENGTH));
        if (isNewReply) {
            reply.setAuthorId(user.getId());
            _discussionReplyDao.save(reply);
        } else {
            reply.setDateUpdated(new Date());
            _discussionReplyDao.saveKeepDates(reply);
        }

        // update number of replies for discussion
        int numReplies = _discussionReplyDao.getTotalReplies(discussion);
        discussion.setNumReplies(numReplies);
        _discussionDao.saveKeepDates(discussion);

    }

    public void filterWords(DiscussionReply reply, User user, HttpServletRequest request){
        String bodyWord = _alertWordDao.hasAlertWord(reply.getBody());
        if (!user.hasPermission(Permission.COMMUNITY_VIEW_REPORTED_POSTS) && bodyWord != null) {
            // profanity filter
            // Moderators are always allowed to post profanity
            String reason = "Contains the alert word \"";
            reason += bodyWord;
            reason += "\"";
            _reportContentService.reportContent(getAlertWordFilterUser(), user, request, reply.getId(),
                    ReportedEntity.ReportedEntityType.reply, reason);
            _log.warn("Reply triggers profanity filter.");
        }
    }

    public void doRedirect(DiscussionSubmissionCommand command, CmsDiscussionBoard board, Discussion discussion,
                           DiscussionReply reply,HttpServletRequest request){
        if (StringUtils.isEmpty(command.getRedirect())) {
            // default to forwarding to the discussion detail page
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.COMMUNITY_DISCUSSION, board.getFullUri(),
            Long.valueOf(discussion.getId()));
            urlBuilder.setParameter("discussionReplyId", String.valueOf(reply.getId()));
            command.setRedirect(urlBuilder.asSiteRelative(request) + "#reply_" + reply.getId());
            _log.info("Setting redirect to " + command.getRedirect());
        }
    }

    public void notifyAboutCBReply(HttpServletRequest request, SessionContext sessionContext, CmsDiscussionBoard board,
                                   Discussion discussion, DiscussionReply reply, User replyAuthor) {
        User author = _userDao.findUserFromId(discussion.getAuthorId());
        boolean sendEmail = checkEmailSend(author,sessionContext);
        String language = request.getParameter("language");
        Subscription sub = author.findSubscription(SubscriptionProduct.CB_COMMUNITY_NOTIFICATIONS);
        if(sub != null && discussion.isNotifyAuthorAboutReplies() && author.getId() != replyAuthor.getId() && // don't receive notification if commenting on own discussion
        (sendEmail)){
            _log.info("Community email notification sent to " + author.getEmail() + " (" + author.getId() +
            ") for reply " + reply.getId() + " from replyAuthor " + replyAuthor.getId());

            Map<String,String> emailAttributes = new HashMap<String,String>();
            if (replyAuthor.getUserProfile() != null && replyAuthor.getUserProfile().getScreenName() != null) {
                emailAttributes.put("CB_commenter_username", replyAuthor.getUserProfile().getScreenName());
            }
            if (author.getUserProfile() != null && author.getUserProfile().getScreenName() != null) {
                emailAttributes.put("CB_author_username", author.getUserProfile().getScreenName());
            }
            emailAttributes.put("CB_post_snippet", Util.abbreviate(reply.getBody(),20));
            String postUrl = request.getParameter("ETPostUrl");
            System.out.println("ETPostUrl-------------"+postUrl);
            emailAttributes.put("CB_post_url ", postUrl);
            emailAttributes.put("CB_entity_id", String.valueOf(discussion.getId()));
            if("es".equalsIgnoreCase(language)){
                _exactTargetAPI.sendTriggeredEmail("CB_Community_Notification_es", author, emailAttributes);
            }else{
                _exactTargetAPI.sendTriggeredEmail("CB_Community_Notification", author, emailAttributes);
            }
        }
    }

    // GS-10375
    // TODO-10375 write a unit test in DiscussionSubmissionControllerTest.java to test logic for whether or not to send email
    public void notifyAboutGSReply(HttpServletRequest request, SessionContext sessionContext, CmsDiscussionBoard board, Discussion discussion, DiscussionReply reply, User replyAuthor) {
        User author = _userDao.findUserFromId(discussion.getAuthorId());
        boolean sendEmail = checkEmailSend(author,sessionContext);

        if (author.getNotifyAboutReplies() && discussion.isNotifyAuthorAboutReplies() &&
            author.getId() != replyAuthor.getId() && // don't receive notification if commenting on own discussion
            (sendEmail)) {
            _log.info("Community email notification sent to " + author.getEmail() + " (" + author.getId() +
                    ") for reply " + reply.getId() + " from replyAuthor " + replyAuthor.getId());

            Map<String,String> emailAttributes = new HashMap<String,String>();
            if (replyAuthor.getUserProfile() != null && replyAuthor.getUserProfile().getScreenName() != null) {
                emailAttributes.put("commenter_username", replyAuthor.getUserProfile().getScreenName());
            }
            if (author.getUserProfile() != null && author.getUserProfile().getScreenName() != null) {
                emailAttributes.put("author_username", author.getUserProfile().getScreenName());
            }
            emailAttributes.put("post_snippet", Util.abbreviate(reply.getBody(),20));
            DiscussionTagHandler discussionTagHandler = new DiscussionTagHandler();
            discussionTagHandler.setDiscussion(discussion);
            discussionTagHandler.setFullUri(board.getFullUri());
            discussionTagHandler.setDiscussionReplyId(reply.getId());
            UrlBuilder postUrlBuilder = discussionTagHandler.createUrlBuilder();
            emailAttributes.put("post_url", postUrlBuilder.asSiteRelative(request));
            emailAttributes.put("entity_id", String.valueOf(discussion.getId()));

            _exactTargetAPI.sendTriggeredEmail("gs_community_notification", author, emailAttributes);
        }
    }

    public boolean checkEmailSend(User author,SessionContext sessionContext){
        boolean isInternalServer =
            UrlUtil.isDevEnvironment(sessionContext.getHostName()) ||
            UrlUtil.isQAServer(sessionContext.getHostName()) ||
            UrlUtil.isPreReleaseServer(sessionContext.getHostName());

        boolean authorHasGsEmailAddress =
            author.getEmail() != null &&
            (author.getEmail().toLowerCase().endsWith("@greatschools.org") ||
            author.getEmail().toLowerCase().endsWith("@greatschools.net"));
        return (!isInternalServer || authorHasGsEmailAddress);
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

    public IReportContentService getReportContentService() {
        return _reportContentService;
    }

    public void setReportContentService(IReportContentService reportContentService) {
        _reportContentService = reportContentService;
    }

    public ExactTargetAPI getExactTargetAPI() {
        return _exactTargetAPI;
    }

    public void setExactTargetAPI(ExactTargetAPI exactTargetAPI) {
        _exactTargetAPI = exactTargetAPI;
    }

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }
}