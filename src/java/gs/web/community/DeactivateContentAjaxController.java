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
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.search.SolrService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class DeactivateContentAjaxController extends SimpleFormController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());
    private IDiscussionReplyDao _discussionReplyDao;
    private IDiscussionDao _discussionDao;
    private IUserDao _userDao;
    private SolrService _solrService;

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
                                    Object commandObj, BindException errors) throws Exception {
        DeactivateContentCommand command = (DeactivateContentCommand) commandObj;

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
                    ThreadLocalTransactionManager.commitOrRollback();
                }
            } else if (command.getContentType() == DeactivateContentCommand.ContentType.discussion) {
                Discussion discussion = _discussionDao.findById((int)command.getContentId());
                if (discussion != null && discussion.isActive() != command.isReactivate()) {
                    _log.info("Setting discussion with id=" + discussion.getId() +
                            " to active=" + command.isReactivate());
                    discussion.setActive(command.isReactivate());
                    _discussionDao.saveKeepDates(discussion);
                    ThreadLocalTransactionManager.commitOrRollback();
                    try {
                        if (command.isReactivate()) {
                            _userDao.populateWithUser(discussion);
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

    public SolrService getSolrService() {
        return _solrService;
    }

    public void setSolrService(SolrService solrService) {
        _solrService = solrService;
    }
}
