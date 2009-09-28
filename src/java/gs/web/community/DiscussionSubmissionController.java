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
import gs.web.util.SitePrefCookie;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.data.community.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class DiscussionSubmissionController extends SimpleFormController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());

    public final static int REPLY_BODY_MINIMUM_LENGTH = 5;
    public final static String COOKIE_REPLY_BODY_PROPERTY = "replyBody";

    private IDiscussionReplyDao _discussionReplyDao;
    private IDiscussionDao _discussionDao;

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object commandObj, BindException errors) throws Exception {
        DiscussionSubmissionCommand command = (DiscussionSubmissionCommand) commandObj;

        handleSubmission(request, response, command);

        return new ModelAndView(new RedirectView(command.getRedirect()));
    }

    protected void handleSubmission(HttpServletRequest request, HttpServletResponse response, DiscussionSubmissionCommand command) {
        if (command.getDiscussionId() != null) {
            handleDiscussionReplySubmission(request, response, command);
        } else {
            _log.warn("Unknown submission type -- has no discussion id");
            // TODO: Handle other forms of submits
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
            throw new IllegalStateException("Discussion submission occurred but no valid user is cookied!");
        }
        User user = sessionContext.getUser();
        Discussion discussion = _discussionDao.findById(command.getDiscussionId());
        if (discussion == null) {
            _log.warn("Attempt to submit with unknown discussion id (" + command.getDiscussionId() + ") rejected");
            throw new IllegalStateException("Discussion submission with unknown discussion id! id=" +
                    command.getDiscussionId());
        }

        // validation
        if (StringUtils.length(command.getBody()) < REPLY_BODY_MINIMUM_LENGTH) {
            SitePrefCookie sitePrefCookie = new SitePrefCookie(request, response);
            sitePrefCookie.setProperty(COOKIE_REPLY_BODY_PROPERTY, command.getBody());
            // TODO
            command.setRedirect("/community/discussion.gs?content=" + command.getDiscussionId());
            _log.warn("Attempt to submit with body length < " + REPLY_BODY_MINIMUM_LENGTH + " ignored");
        } else {

            // TODO: profanity filter
            // TODO: more validation?
            // TODO: sanitize string (strip HTML? JS? SQL?)?

            DiscussionReply reply = new DiscussionReply();
            reply.setDiscussion(discussion);
            reply.setBody(command.getBody());
            reply.setAuthorId(user.getId());
            _log.info("Saving reply " + reply);
            _discussionReplyDao.save(reply);
            if (StringUtils.isEmpty(command.getRedirect())) {
                // default to forwarding to the discussion detail page
                command.setRedirect("/community/discussion.gs?content=" + command.getDiscussionId());
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
}
