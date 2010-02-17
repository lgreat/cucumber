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
import gs.data.dao.hibernate.ThreadLocalTransactionManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

public class RaiseYourHandAjaxController extends SimpleFormController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());
    private IDiscussionReplyDao _discussionReplyDao;
    private IDiscussionDao _discussionDao;
    private IUserDao _userDao;
    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private SolrService _solrService;
    private IPublicationDao _publicationDao;
    private IRaiseYourHandDao _raiseYourHandDao;

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
                // make this discussion a RYH
                discussion.setDateRaiseYourHand(new Date());
                _discussionDao.save(discussion);
                // re-index discussion in solr
                _userDao.populateWithUser(discussion);
                discussion.setDiscussionBoard(_cmsDiscussionBoardDao.get(discussion.getBoardId()));
                _solrService.updateDocument(discussion);
                ThreadLocalTransactionManager.commitOrRollback();
            } else if (ACTION_DEACTIVATE.equals(command.getAction())) {
                // make this discussion not a RYH by clearning the RYH date
                discussion.setDateRaiseYourHand(null);
                _discussionDao.save(discussion);
                // re-index discussion in solr
                _userDao.populateWithUser(discussion);
                discussion.setDiscussionBoard(_cmsDiscussionBoardDao.get(discussion.getBoardId()));
                _solrService.updateDocument(discussion);
                // remove RYH feature entries
                _raiseYourHandDao.deleteAllFeatures(discussion);
                ThreadLocalTransactionManager.commitOrRollback();
            } else if (ACTION_FEATURE.equals(command.getAction())) {
                if (topicCenter != null) {
                    // link discussion to TC
                    RaiseYourHandFeature ryhFeature = new RaiseYourHandFeature();
                    ryhFeature.setDiscussion(discussion);
                    ryhFeature.setContentKey(topicCenter.getContentKey());
                    _raiseYourHandDao.save(ryhFeature);
                    ThreadLocalTransactionManager.commitOrRollback();
                }
            } else if (ACTION_UNFEATURE.equals(command.getAction())) {
                if (topicCenter != null) {
                    // unlink discussion from TC
                    RaiseYourHandFeature ryhFeature = _raiseYourHandDao.getRaiseYourHandFeature(discussion, topicCenter.getContentKey());
                    if (ryhFeature != null) {
                        _raiseYourHandDao.delete(ryhFeature);
                        ThreadLocalTransactionManager.commitOrRollback();
                    }
                }
            }
        }

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

    public IRaiseYourHandDao getRaiseYourHandDao() {
        return _raiseYourHandDao;
    }

    public void setRaiseYourHandDao(IRaiseYourHandDao raiseYourHandDao) {
        _raiseYourHandDao = raiseYourHandDao;
    }
}
