package gs.web.community;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
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
import gs.web.util.UrlBuilder;
import gs.web.util.RedirectView301;
import gs.web.util.SitePrefCookie;

import java.util.*;

/**
 * @author Dave Roy <mailto:droy@greatschools.net>
 */
public class UserInfoController extends AbstractController {
    protected final Log _log = LogFactory.getLog(getClass());

    public static final String VIEW_NOT_FOUND = "/status/error404.page";
    public static final String URI_USER_ACCOUNT = "/account/";
    public static final String URI_PREFIX_USER_PROFILE = "/members/";

    public static final String MODEL_PAGE_TYPE = "pageType";
    public static final String MODEL_PAGE_USER = "pageUser";
    public static final String MODEL_VIEWER = "viewer";
    public static final String MODEL_VIEWING_OWN_PROFILE = "viewingOwnProfile";
    public static final String MODEL_RECENT_POSTS = "recentPosts";
    public static final String MODEL_OMNITURE_PAGENAME = "omniturePagename";
    public static final String MODEL_OMNITURE_HIERARCHY = "omnitureHierarchy";
    public static final String MODEL_AD_SLOT_PREFIX = "adSlotPrefix";
    public static final String MODEL_PAGE_HEADING = "pageHeading";
    public static final String MODEL_URI = "uri";
    public static final String MODEL_VIEW_ALL_ACTIVITY = "viewAllActivity";
    public static final String MODEL_TOTAL_USER_CONTENT = "totalUserContent";
    public static final String MODEL_PAGE = "page";
    public static final String MODEL_TOTAL_PAGES = "totalPages";
    public static final String MODEL_COMMUNITY_HOST = "communityHost";

    public static final String USER_ACCOUNT_PAGE_TYPE = "userAccount";
    public static final String USER_PROFILE_PAGE_TYPE = "userProfile";

    public static final int RECENT_ACTIVITY_PAGE_SIZE = 5;
    public static final int VIEW_ALL_ACTIVITY_PAGE_SIZE = 10;

    public static final String PARAM_VIEW_ALL_ACTIVITY = "viewAllActivity";
    public static final String PARAM_PAGE = "page";

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

        if (request.getRequestURI().startsWith("/members/") && !request.getRequestURI().endsWith("/")) {
            String redirectUrl = request.getRequestURI() + "/" +
                    (StringUtils.isNotBlank(request.getQueryString()) ? "?" + request.getQueryString() : "");
            return new ModelAndView(new RedirectView301(redirectUrl));
        } else if (request.getRequestURI().startsWith("/account") && !request.getRequestURI().equals("/account/")) {
            return new ModelAndView(new RedirectView301("/account/"));
        }

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
        } else if (USER_ACCOUNT_PAGE_TYPE.equals(_pageType)) {
            // if viewing My Account and user is not logged in, redirect to login page
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, null, URI_USER_ACCOUNT);
            return new ModelAndView(new RedirectView(urlBuilder.asSiteRelative(request)));
        }

        List<UserContent> recentContent;

        String viewAllActivity = request.getParameter(PARAM_VIEW_ALL_ACTIVITY);
        if (StringUtils.isNotBlank(viewAllActivity) && "true".equals(viewAllActivity)) {
            int pageSize = VIEW_ALL_ACTIVITY_PAGE_SIZE;
            model.put(MODEL_VIEW_ALL_ACTIVITY, true);
            int totalUserContent = getTotalUserContent(pageUser);
            model.put(MODEL_TOTAL_USER_CONTENT, totalUserContent);
            model.put(MODEL_TOTAL_PAGES, getTotalPages(pageSize, totalUserContent));
            int page = getPageNumber(request); 
            model.put(MODEL_PAGE, page);
            recentContent = getUserContentForPage(pageUser.getId(), page, pageSize);

        } else {
            model.put(MODEL_VIEW_ALL_ACTIVITY, false);
            recentContent = _userContentDao.findAllContentByAuthor(pageUser, RECENT_ACTIVITY_PAGE_SIZE);
        }

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

        model.put(MODEL_URI, request.getRequestURI());

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

        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        model.put(MODEL_COMMUNITY_HOST, sessionContext.getSessionContextUtil().getCommunityHost(request));
        model.put(MODEL_PAGE_USER, pageUser);
        model.put(MODEL_VIEWING_OWN_PROFILE, viewingOwnProfile);

        model.put(MODEL_PAGE_TYPE, _pageType);

        SitePrefCookie sitePrefCookie = new SitePrefCookie(request, response);
        model.put("avatarAlertType", sitePrefCookie.getProperty("avatarAlertType"));
        sitePrefCookie.removeProperty("avatarAlertType");

        return new ModelAndView(_viewName, model);
    }

    /**
     * Extract the page number from the request. Defaults to 1.
     */
    protected int getPageNumber(HttpServletRequest request) {
        int page = 1;

        String pageParam = request.getParameter(PARAM_PAGE);
        if (pageParam != null) {
            try {
                page = Integer.valueOf(pageParam);
            } catch (NumberFormatException nfe) {
                // nothing
            }
        }
        return page;
    }

    /**
     * Get the list of user content for this particular page.
     * @param userId id of User authoringt he content
     * @param page What page are we on?
     * @param pageSize How many items of user content are there per page?
     * @return non-null list
     */
    protected List<UserContent> getUserContentForPage
            (int userId, int page, int pageSize) {
        List<UserContent> content;

        content = _userContentDao.getContentByAuthorForPage(userId, page, pageSize);

        return content;
    }

    /**
     * Get the total number of user content items by this user.
     */
    protected int getTotalUserContent(User user) {
        int totalUserContent;

        totalUserContent = _userContentDao.getTotalUserContent(user.getId());

        return totalUserContent;
    }

    /**
     * Calculate how many pages are needed to display totalDiscussions given pageSize
     */
    protected int getTotalPages(int pageSize, int totalItems) {
        int totalPages = 1;

        if (pageSize > 0 && totalItems > 0 && totalItems > pageSize) {
            totalPages = ((totalItems + pageSize - 1) / pageSize);
        }

        return totalPages;
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