package gs.web.community;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import gs.web.util.ReadWriteController;
import gs.web.util.context.SessionContextUtil;
import gs.data.community.*;
import gs.data.security.Permission;
import gs.data.search.SolrService;
import gs.data.content.cms.ICmsDiscussionBoardDao;
import gs.data.content.cms.CmsTopicCenter;
import gs.data.cms.IPublicationDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RaiseYourHandAjaxController extends SimpleFormController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());
    private IDiscussionReplyDao _discussionReplyDao;
    private IDiscussionDao _discussionDao;
    private IUserDao _userDao;
    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private SolrService _solrService;
    private IPublicationDao _publicationDao;

    final private String ACTION_ACTIVATE = "activate";
    final private String ACTION_DEACTIVATE = "deactivate";
    final private String ACTION_FEATURE = "feature";
    final private String ACTION_UNFEATURE = "unfeature";

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
                                    Object commandObj, BindException errors) throws Exception {
        RaiseYourHandCommand command = (RaiseYourHandCommand) commandObj;
        User user = SessionContextUtil.getSessionContext(request).getUser();
        Discussion discussion = null;
        if (command.getDiscussionId() != null) {
            discussion = _discussionDao.findById(command.getDiscussionId());
        }
        CmsTopicCenter topicCenter = null;
        if (command.getTopicCenterId() != null) {
            topicCenter = _publicationDao.populateByContentId(command.getTopicCenterId(), new CmsTopicCenter());
        }

        // only active (non-deleted) discussions can be have Manage RYH actions undertaken.
        // there should not be a way to delete a RYH the way you can delete (make inactive) a discussion
        if (user != null && user.getUserProfile() != null &&
            user.hasPermission(Permission.COMMUNITY_MANAGE_RAISE_YOUR_HAND) && discussion != null && discussion.isActive()) {
            if (ACTION_ACTIVATE.equals(command.getAction())) {
                // TODO-9134 make this discussion a RYH
                // TODO-9134 move it from the original discussion board to the RYH discussion board
                // TODO-9134 store activated timestamp
                // TODO-9134 store the original discussion board ID with the RYH
                System.out.println("TODO-9134 ACTION_ACTIVATE " + discussion.getId());
            } else if (ACTION_DEACTIVATE.equals(command.getAction())) {
                // TODO-9134 move discussion from the RYH discussion board to the original discussion board
                // TODO-9134 if tied to home page TC, keep activated timestamp
                System.out.println("TODO-9134 ACTION_DEACTIVATE " + discussion.getId());
            } else if (ACTION_FEATURE.equals(command.getAction())) {
                if (topicCenter != null) {
                    // TODO-9134 link discussion to TC
                    System.out.println("TODO-9134 ACTION_FEATURE " + discussion.getId() + ", TC: " + topicCenter.getTitle() + " (" + topicCenter.getContentKey().getIdentifier() + ")");
                }
            } else if (ACTION_UNFEATURE.equals(command.getAction())) {
                if (topicCenter != null) {
                    // TODO-9134 unlink discussion from TC
                    System.out.println("TODO-9134 ACTION_UNFEATURE " + discussion.getId() + ", TC: " + topicCenter.getTitle() + " (" + topicCenter.getContentKey().getIdentifier() + ")");
                }
            }
        }
        /*

        User user = SessionContextUtil.getSessionContext(request).getUser();
        if (user != null && user.getUserProfile() != null
                && user.hasPermission(Permission.COMMUNITY_VIEW_REPORTED_POSTS)) {
            if (command.getContentType() == DeactivateContentCommand.ContentType.reply) {
                DiscussionReply reply = _discussionReplyDao.findById((int)command.getContentId());
                if (reply != null && reply.isActive() != command.isReactivate()) {
                    _log.info("Setting reply with id=" + reply.getId() + " to active=" + command.isReactivate());
                    reply.setActive(command.isReactivate());
                    reply.setDateUpdated(new Date());
                    _discussionReplyDao.saveKeepDates(reply);
                    _discussionDao.recalculateDateThreadUpdated(reply.getDiscussion());
                    ThreadLocalTransactionManager.commitOrRollback();
                }
            } else if (command.getContentType() == DeactivateContentCommand.ContentType.discussion) {
                Discussion discussion = _discussionDao.findById((int)command.getContentId());
                if (discussion != null && discussion.isActive() != command.isReactivate()) {
                    _log.info("Setting discussion with id=" + discussion.getId() +
                            " to active=" + command.isReactivate());
                    discussion.setActive(command.isReactivate());
                    _discussionDao.saveKeepDates(discussion);
                    Set<DiscussionReply> replies = discussion.getReplies();
                    for (DiscussionReply reply : replies) {
                        reply.setActive(command.isReactivate());
                        _discussionReplyDao.saveKeepDates(reply);
                    }
                    ThreadLocalTransactionManager.commitOrRollback();
                    try {
                        if (command.isReactivate()) {
                            _userDao.populateWithUser(discussion);
                            discussion.setDiscussionBoard(_cmsDiscussionBoardDao.get(discussion.getBoardId()));
                            _solrService.indexDocument(discussion);
                        } else {
                            _solrService.deleteDocument(discussion);
                        }
                    } catch (Exception e) {
                        String action = command.isReactivate() ? "index" : "de-index";
                        _log.error("Could not " + action + " discussion " + discussion.getId() + " using solr", e);
                    }
                }
            }
        }
        */
        return null;
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

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public ICmsDiscussionBoardDao getCmsDiscussionBoardDao() {
        return _cmsDiscussionBoardDao;
    }

    public void setCmsDiscussionBoardDao(ICmsDiscussionBoardDao cmsDiscussionBoardDao) {
        _cmsDiscussionBoardDao = cmsDiscussionBoardDao;
    }

    public SolrService getSolrService() {
        return _solrService;
    }

    public void setSolrService(SolrService solrService) {
        _solrService = solrService;
    }

    public IPublicationDao getPublicationDao() {
        return _publicationDao;
    }

    public void setPublicationDao(IPublicationDao publicationDao) {
        _publicationDao = publicationDao;
    }
}
