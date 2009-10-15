package gs.web.community;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.cms.IPublicationDao;
import gs.data.community.*;
import gs.data.content.cms.ICmsDiscussionBoardDao;
import gs.data.content.cms.CmsDiscussionBoard;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.PageHelper;

import java.util.*;
import java.text.SimpleDateFormat;

/**
 * @author Dave Roy <mailto:droy@greatschools.net>
 */
public class UserInfoController extends AbstractController {
    protected final Log _log = LogFactory.getLog(getClass());

    public static final String VIEW_NOT_FOUND = "/status/error404.page";

    public static final String MODEL_PAGE_TYPE = "pageType";
    public static final String MODEL_PAGE_USER = "pageUser";
    public static final String MODEL_VIEWER = "viewer";
    public static final String MODEL_VIEWING_OWN_PROFILE = "viewingOwnProfile";
    public static final String MODEL_RECENT_POSTS = "recentPosts";
    public static final String MODEL_OMNITURE_PAGENAME = "omniturePagename";
    public static final String MODEL_OMNITURE_HIERARCHY = "omnitureHierarchy";
    public static final String MODEL_AD_SLOT_PREFIX = "adSlotPrefix";
    public static final String MODEL_PAGE_HEADING = "pageHeading";

    public static final String USER_ACCOUNT_PAGE_TYPE = "userAccount";
    public static final String USER_PROFILE_PAGE_TYPE = "userProfile";

    private String _viewName;
    private boolean _defaultToCurrentUser;
    private String _pageType;
    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private IDiscussionDao _discussionDao;
    private IDiscussionReplyDao _discussionReplyDao;
    private IUserContentDao _userContentDao;
    private IPublicationDao _publicationDao;
    private IUserDao _userDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> model = new HashMap<String, Object>();

        User pageUser = null;

        String username = request.getRequestURI().replaceAll("^/members/", "").replaceAll("^/account/","");
        if (username.endsWith("/")) {
            username = username.substring(0, username.length() - 1);
        }
        if (!_defaultToCurrentUser && StringUtils.isNotBlank(username)) {
            // first try to get a user from username in uri

            try {
                pageUser = _userDao.findUserFromScreenNameIfExists(username);
            } catch (ObjectRetrievalFailureException orfe) {
                _log.warn("Unknown member username: " + username);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return new ModelAndView(VIEW_NOT_FOUND);
            }

            if (pageUser == null) {
                _log.warn("Unknown member username: " + username);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return new ModelAndView(VIEW_NOT_FOUND);
            }
        }

        boolean viewingOwnProfile = false;
        if (PageHelper.isMemberAuthorized(request)) {
            SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
            User viewer = sessionContext.getUser();
            if (viewer != null) {
                model.put(MODEL_VIEWER, viewer);
            }

            if (pageUser == null && _defaultToCurrentUser) {
                pageUser = viewer;
                viewingOwnProfile = true;
            } else if (pageUser.getId() == viewer.getId()) {
                viewingOwnProfile = true;
            }
        }

        if (USER_ACCOUNT_PAGE_TYPE.equals(_pageType)) {
            model.put(MODEL_PAGE_HEADING, "My account");
            model.put(MODEL_OMNITURE_PAGENAME, "My Account");
            model.put(MODEL_OMNITURE_HIERARCHY, "Account,My Account");
            model.put(MODEL_AD_SLOT_PREFIX, "MyAccount");
        } else if (USER_PROFILE_PAGE_TYPE.equals(_pageType)) {
            model.put(MODEL_PAGE_HEADING, username + "'s profile");
            model.put(MODEL_OMNITURE_PAGENAME, "Member Profile");
            model.put(MODEL_OMNITURE_HIERARCHY, "Community,Members,User Profile," + username);
            model.put(MODEL_AD_SLOT_PREFIX, "MemberProfile");
        }

        List<UserContent> recentContent = _userContentDao.findAllContentByAuthor(pageUser, 5);
        for (UserContent content: recentContent) {
            Discussion discussion = null;
            if (content.getType().equals("Discussion")) {
                discussion = (Discussion)content;
            } else if (content.getType().equals("DiscussionReply")) {
                discussion = ((DiscussionReply)content).getDiscussion();
            }

            if (discussion != null) {
                CmsDiscussionBoard board = _cmsDiscussionBoardDao.get(discussion.getBoardId());
                discussion.setDiscussionBoard(board);
            }
        }
        model.put(MODEL_RECENT_POSTS, recentContent);

        model.put(MODEL_PAGE_USER, pageUser);
        model.put(MODEL_VIEWING_OWN_PROFILE, viewingOwnProfile);

        model.put(MODEL_PAGE_TYPE, _pageType);

        return new ModelAndView(_viewName, model);
    }

    /*
     * Bean accessor/mutators below
     */
    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
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

    public IPublicationDao getPublicationDao() {
        return _publicationDao;
    }

    public void setPublicationDao(IPublicationDao publicationDao) {
        _publicationDao = publicationDao;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public boolean isDefaultToCurrentUser() {
        return _defaultToCurrentUser;
    }

    public void setDefaultToCurrentUser(boolean defaultToCurrentUser) {
        _defaultToCurrentUser = defaultToCurrentUser;
    }

    public String getPageType() {
        return _pageType;
    }

    public void setPageType(String pageType) {
        _pageType = pageType;
    }

    public IUserContentDao getUserContentDao() {
        return _userContentDao;
    }

    public void setUserContentDao(IUserContentDao userContentDao) {
        _userContentDao = userContentDao;
    }
}