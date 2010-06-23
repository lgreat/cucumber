package gs.web.community;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import gs.data.community.*;
import gs.data.content.cms.CmsDiscussionBoard;
import gs.data.content.cms.ICmsDiscussionBoardDao;
import gs.data.content.cms.ContentKey;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;

/**
 * Controller for raise your hand module
 *
 * @author Young Fan
 */
public class RaiseYourHandController extends AbstractController {
    protected final Log _log = LogFactory.getLog(getClass());

    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private IDiscussionDao _discussionDao;
    private IDiscussionReplyDao _discussionReplyDao;
    private IUserDao _userDao;
    private IRaiseYourHandDao _raiseYourHandDao;
    private String _viewName;

    public static final int RAISE_YOUR_HAND_MAX_NUM_REPLIES = 5;

    public static final String MODEL_DISCUSSION = "discussion";
    public static final String MODEL_REPLIES = "replies";
    public static final String MODEL_CURRENT_DATE = "currentDate";
    public static final String MODEL_USER = "user";
    public static final String MODEL_SIGNED_IN = "signedIn";
    public static final String MODEL_REDIRECT_URL = "redirectUrl";
    public static final String MODEL_RAISE_YOUR_HAND_MAX_NUM_REPLIES = "ryhMaxNumReplies";
    public static final String MODEL_STYLE = "style";
    public static final String MODEL_SHOW_VIEW_ALL = "showViewAll";
    public static final String MODEL_SHOW_SPONSOR = "showSponsor";

    public static final String PARAM_FEATURE_CONTENT_KEY = "featureContentKey";
    public static final String PARAM_REDIRECT_URL = "redirectUrl";
    public static final String PARAM_LIMIT = "limit";
    public static final String PARAM_STYLE = "style";
    public static final String PARAM_SHOW_VIEW_ALL = "showViewAll";
    public static final String PARAM_SHOW_SPONSOR = "showSponsor";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();

        String contentKey = request.getParameter(PARAM_FEATURE_CONTENT_KEY);
        String limitStr = request.getParameter(PARAM_LIMIT);
        int limit = 5;
        if (limitStr != null) {
            limit = Integer.parseInt(limitStr);
        }
        String style = request.getParameter(PARAM_STYLE);        
        boolean showViewAll = Boolean.valueOf(request.getParameter(PARAM_SHOW_VIEW_ALL));

        RaiseYourHandFeature ryhFeature = _raiseYourHandDao.getRandomFeature(new ContentKey(contentKey));
        if (ryhFeature != null) {
            Discussion discussion = ryhFeature.getDiscussion();
            List<UserContent> userContents = new ArrayList<UserContent>();
            userContents.add(discussion);
            CmsDiscussionBoard discussionBoard = _cmsDiscussionBoardDao.get(discussion.getBoardId());
            List<DiscussionReply> replies = _discussionReplyDao.getRepliesForPage(
                    discussion, 1, limit,
                    IDiscussionReplyDao.DiscussionReplySort.NEWEST_FIRST, false);
            userContents.addAll(replies);

            discussion.setDiscussionBoard(discussionBoard);
            _userDao.populateWithUsers(userContents);

            model.put(MODEL_DISCUSSION, discussion);
            model.put(MODEL_REPLIES, replies);
            model.put(MODEL_CURRENT_DATE, new Date());
            model.put(MODEL_RAISE_YOUR_HAND_MAX_NUM_REPLIES, limit);
            model.put(MODEL_STYLE, style);
            model.put(MODEL_SHOW_VIEW_ALL, showViewAll);
            model.put(MODEL_SHOW_SPONSOR, "true".equals(request.getParameter(PARAM_SHOW_SPONSOR)));

            SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
            User user = null;
            if (PageHelper.isMemberAuthorized(request)) {
                user = sessionContext.getUser();
                if (user != null) {
                    model.put(MODEL_USER, user);
                }
            }
            model.put(MODEL_SIGNED_IN, user != null);
            
            if (user == null) {
                String redirectUrl = request.getParameter(PARAM_REDIRECT_URL);
                UrlBuilder builder;
                if (redirectUrl == null) {
                    // if no redirect url specified, use discussion detail page url as redirect url
                    builder = new UrlBuilder(UrlBuilder.COMMUNITY_DISCUSSION, discussion.getDiscussionBoard().getFullUri(), new Long(discussion.getId()));
                    redirectUrl = builder.asSiteRelative(request);
                }
//                builder = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, null, redirectUrl);
//                model.put(MODEL_REDIRECT_URL, builder.asSiteRelative(request));
                model.put(MODEL_REDIRECT_URL, redirectUrl);
            } else {
                model.put(MODEL_REDIRECT_URL, "#");
            }
        }

        return new ModelAndView(_viewName, model);
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

    public IRaiseYourHandDao getRaiseYourHandDao() {
        return _raiseYourHandDao;
    }

    public void setRaiseYourHandDao(IRaiseYourHandDao raiseYourHandDao) {
        _raiseYourHandDao = raiseYourHandDao;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
}
