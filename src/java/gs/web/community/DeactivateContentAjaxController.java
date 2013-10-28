package gs.web.community;

import gs.data.school.ISchoolMediaDao;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.ITopicalSchoolReviewDao;
import gs.data.school.review.Review;
import gs.data.school.review.TopicalSchoolReview;
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
import gs.data.content.cms.ICmsDiscussionBoardDao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Set;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class DeactivateContentAjaxController extends SimpleFormController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());
    private IDiscussionReplyDao _discussionReplyDao;
    private IDiscussionDao _discussionDao;
    private IUserDao _userDao;
    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private SolrService _solrService;
    private IReviewDao _reviewDao;
    private ITopicalSchoolReviewDao _topicalSchoolReviewDao;
    private ISchoolMediaDao _schoolMediaDao;

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

                    // update number of replies for discussion
                    Discussion discussion = reply.getDiscussion();
                    int numReplies = _discussionReplyDao.getTotalReplies(discussion);
                    discussion.setNumReplies(numReplies);

                    // this saves the discussion
                    _discussionDao.recalculateDateThreadUpdated(reply.getDiscussion());

                    ThreadLocalTransactionManager.commitOrRollback();
                }
            } else if (command.getContentType() == DeactivateContentCommand.ContentType.discussion) {
                Discussion discussion = _discussionDao.findById((int)command.getContentId());
                if (discussion != null && discussion.isActive() != command.isReactivate()) {
                    // activate/deactivate the replies
                    Set<DiscussionReply> replies = discussion.getReplies();
                    for (DiscussionReply reply : replies) {
                        reply.setActive(command.isReactivate());
                        _discussionReplyDao.saveKeepDates(reply);
                    }

                    _log.info("Setting discussion with id=" + discussion.getId() +
                            " to active=" + command.isReactivate());
                    discussion.setActive(command.isReactivate());

                    // update number of replies for discussion
                    int numReplies = _discussionReplyDao.getTotalReplies(discussion);
                    discussion.setNumReplies(numReplies);

                    // save the discussion
                    _discussionDao.saveKeepDates(discussion);

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
            } else if (command.getContentType() == DeactivateContentCommand.ContentType.schoolReview) {
                Review review = _reviewDao.getReview((int)command.getContentId());
                if (command.isReactivate()) {
                    review.setStatus("p");
                } else {
                    review.setStatus("d");
                }
                review.setProcessDate(new Date());
                _reviewDao.saveReview(review);
            } else if (command.getContentType() == DeactivateContentCommand.ContentType.topicalSchoolReview) {
                TopicalSchoolReview review = _topicalSchoolReviewDao.get((int) command.getContentId());
                if (command.isReactivate()) {
                    review.setStatus("p");
                } else {
                    review.setStatus("d");
                }
                review.setProcessDate(new Date());
                _topicalSchoolReviewDao.save(review);
            } else if (command.getContentType() == DeactivateContentCommand.ContentType.schoolMedia) {
                _schoolMediaDao.disableById((int) command.getContentId());

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

    public IReviewDao getReviewDao() {
        return _reviewDao;
    }

    public void setReviewDao(IReviewDao reviewDao) {
        _reviewDao = reviewDao;
    }

    public ITopicalSchoolReviewDao getTopicalSchoolReviewDao() {
        return _topicalSchoolReviewDao;
    }

    public void setTopicalSchoolReviewDao(ITopicalSchoolReviewDao topicalSchoolReviewDao) {
        _topicalSchoolReviewDao = topicalSchoolReviewDao;
    }

    public ISchoolMediaDao getSchoolMediaDao() {
        return _schoolMediaDao;
    }

    public void setSchoolMediaDao(ISchoolMediaDao schoolMediaDao) {
        _schoolMediaDao = schoolMediaDao;
    }
}
