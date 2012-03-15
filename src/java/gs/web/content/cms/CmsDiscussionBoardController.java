package gs.web.content.cms;

import gs.data.community.local.ILocalBoardDao;
import gs.web.geo.StateSpecificFooterHelper;
import gs.web.util.RedirectView301;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import gs.data.content.cms.*;
import gs.data.cms.IPublicationDao;
import gs.data.community.*;

import static gs.data.community.IDiscussionDao.DiscussionSort;
import gs.data.security.Permission;
import gs.data.util.CommunityUtil;
import gs.web.util.SitePrefCookie;
import gs.web.util.UrlBuilder;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.community.DiscussionFacade;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class CmsDiscussionBoardController extends AbstractController {
    protected final Log _log = LogFactory.getLog(getClass());

    public static final String VIEW_NOT_FOUND = "/status/error404.page";
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int DEFAULT_REPLIES_PER_DISCSSION = 2;
    public static final String DEFAULT_SORT = "recent_activity";
    public static final ContentKey HOME_PAGE_CONTENT_KEY = new ContentKey("TopicCenter#2077");

    public static final String MODEL_DISCUSSION_BOARD = "discussionBoard";
    public static final String MODEL_DISCUSSION_LIST = "discussionList";
    public static final String MODEL_TOTAL_DISCUSSIONS = "totalDiscussions";
    public static final String MODEL_TOTAL_PAGES = "totalPages";
    public static final String MODEL_TOPIC_CENTER = "topicCenter";
    public static final String MODEL_PAGE = "page";
    public static final String MODEL_PAGE_SIZE = "pageSize";
    public static final String MODEL_SORT = "sort";
    public static final String MODEL_CURRENT_DATE = "currentDate";
    public static final String MODEL_VALID_USER = "validUser";
    public static final String MODEL_COMMUNITY_HOST = "communityHost";
    public static final String MODEL_URI = "uri";
    public static final String MODEL_LOGIN_REDIRECT = "loginRedirectUrl";
    public static final String MODEL_REPLIES_PER_DISCUSSION = "repliesPerDiscussion";
    public static final String MODEL_RAISE_YOUR_HAND = "raiseYourHand";
    public static final String MODEL_RAISE_YOUR_HAND_FEATURED_QUESTIONS = "raiseYourHandFeaturedQuestions";
    public static final String MODEL_PAGE_TITLE = "pageTitle";
    public static final String MODEL_TITLE = "title";
    public static final String MODEL_RECENT_CONVERSATIONS = "recentConversations";
    public static final String MODEL_SHOW_ONLY_COMMUNITY_LANDING_PAGE_BREADCRUMB = "showOnlyCommunityLandingPageBreadcrumb";

    public static final String PARAM_PAGE = "page";
    public static final String PARAM_PAGE_SIZE = "pageSize";
    public static final String PARAM_SORT = "sort";

    public static final String COOKIE_SORT_PROPERTY = "dBoardSort";

    private String _viewName;
    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private IDiscussionDao _discussionDao;
    private IDiscussionReplyDao _discussionReplyDao;
    private IPublicationDao _publicationDao;
    private IUserDao _userDao;
    private IRaiseYourHandDao _raiseYourHandDao;
    private ILocalBoardDao _localBoardDao;
    private StateSpecificFooterHelper _stateSpecificFooterHelper;
    private Boolean _raiseYourHand;
    private Boolean _raiseYourHandFeaturedQuestions;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();
        Map<String, Object> model = new HashMap<String, Object>();

        CmsDiscussionBoard board = null;
        boolean recentConversations = false;

        Long contentId;
        if (isRaiseYourHand() && isRaiseYourHandFeaturedQuestions()) {
            model.put(MODEL_URI, uri);
        } else {
            if (request.getParameter("content") != null) {
                try {
                    contentId = new Long(request.getParameter("content"));

                    if (contentId == CmsConstants.SPECIAL_EDUCATION_DISCUSSION_BOARD_ID && uri.startsWith("/LD/")) {
                        UrlBuilder builder = new UrlBuilder(new ContentKey("DiscussionBoard", CmsConstants.SPECIAL_EDUCATION_DISCUSSION_BOARD_ID), "/special-education", isRaiseYourHand());
                        return new ModelAndView(new RedirectView301(builder.asSiteRelative(request)));
                    }
                } catch (Exception e) {
                    _log.warn("Invalid content identifier: " + request.getParameter("content"));
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return new ModelAndView(VIEW_NOT_FOUND);
                }
            } else {
                contentId = CmsConstants.GENERAL_PARENTING_DISCUSSION_BOARD_ID;
                recentConversations = true;
            }
            model.put(MODEL_RECENT_CONVERSATIONS, recentConversations);

            board = _cmsDiscussionBoardDao.get(contentId);

            if (board != null) {
                if (board.getContentKey().getIdentifier() == CmsConstants.GENERAL_PARENTING_DISCUSSION_BOARD_ID) {
                    model.put(MODEL_SHOW_ONLY_COMMUNITY_LANDING_PAGE_BREADCRUMB, true);
                }
                model.put(MODEL_DISCUSSION_BOARD, board);
                if (recentConversations) {
                    model.put(MODEL_URI, uri);
                } else {
                    model.put(MODEL_URI, uri + "?content=" + board.getContentKey().getIdentifier());
                }

                if (board.getTopicCenterId() != null) {
                    CmsTopicCenter topicCenter = _publicationDao.populateByContentId
                            (board.getTopicCenterId(), new CmsTopicCenter());
                    model.put(MODEL_TOPIC_CENTER, topicCenter);
                }
            } else {
                _log.warn("Can't find board with id " + contentId);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return new ModelAndView(VIEW_NOT_FOUND);            
            }
        }

        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = null;
        if(PageHelper.isMemberAuthorized(request)){
            user = sessionContext.getUser();
            if (user != null) {
                model.put(MODEL_VALID_USER, user);
            }
        }

        int page = getPageNumber(request);
        int pageSize = getPageSize(request);
        DiscussionSort sort = getDiscussionSort(request, response);
        if (isRaiseYourHand()) {
            page = 1;
            pageSize = -1;
            sort = DiscussionSort.NEWEST_RAISE_YOUR_HAND_DATE;
        } else if (recentConversations) {
            sort = DiscussionSort.NEWEST_FIRST;
        }
        model.put(MODEL_PAGE, page);
        model.put(MODEL_PAGE_SIZE, pageSize);
        model.put(MODEL_SORT, sort);

        boolean includeInactive = false;
        if (user != null && user.hasPermission(Permission.COMMUNITY_VIEW_REPORTED_POSTS)) {
            includeInactive = true;
        }

        List<Discussion> discussions;
        List<Long> excludeBoardIds = null;
        if (recentConversations) {
            excludeBoardIds = _localBoardDao.getLocalBoardIds();
            discussions = _discussionDao.getDiscussionsForPage(page, pageSize, sort, includeInactive, isRaiseYourHand(), excludeBoardIds);
        } else {
            discussions = getDiscussionsForPage(board, page, pageSize, sort, includeInactive, isRaiseYourHand());
        }

        List<DiscussionFacade> facades = populateFacades(board, discussions, recentConversations);
        populateWithUsers(discussions, facades);
        model.put(MODEL_DISCUSSION_LIST, facades);

        model.put(MODEL_CURRENT_DATE, new Date());

        // Google Ad Manager ad keywords
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        pageHelper.addAdKeyword(CommunityUtil.COMMUNITY_GAM_AD_ATTRIBUTE_KEY, String.valueOf(true));

        long totalDiscussions = 0;
        String pageTitle = null;
        String title = null;

        if (board != null) {
            totalDiscussions = getTotalDiscussions((recentConversations ? null : board), includeInactive, isRaiseYourHand(), excludeBoardIds);
            if (!recentConversations) {
                if (board.getCity() != null) {
                    pageHelper.addAdKeyword(CommunityUtil.CITY_GAM_AD_ATTRIBUTE_KEY, board.getCity().getName());
                    pageHelper.addAdKeyword(CommunityUtil.STATE_GAM_AD_ATTRIBUTE_KEY, board.getCity().getState().getAbbreviation());
                } else {
                    for (CmsCategory category : board.getUniqueKategoryBreadcrumbs()) {
                        pageHelper.addAdKeywordMulti(CommunityUtil.EDITORIAL_GAM_AD_ATTRIBUTE_KEY, category.getName());
                    }
                }
            }

            if (StringUtils.isNotBlank(board.getPageTitle())) {
                pageTitle = board.getPageTitle();
            } else {
                pageTitle = board.getTitle();
            }
            if (isRaiseYourHand()) {
                title = pageTitle;
                pageTitle = pageTitle.replaceAll(" Community$", "") + " Featured Discussions, Parent Community";
            } else if (recentConversations) {
                title = pageTitle = "Recent Conversations";
            }
        } else if (isRaiseYourHand() && isRaiseYourHandFeaturedQuestions()){
            totalDiscussions = discussions.size();
            title = "Featured Discussions";
            pageTitle = "Featured Discussions, Parent Community";
        }

        model.put(MODEL_TOTAL_DISCUSSIONS, totalDiscussions);
        model.put(MODEL_TOTAL_PAGES, getTotalPages(pageSize, totalDiscussions));
        model.put(MODEL_COMMUNITY_HOST, sessionContext.getSessionContextUtil().getCommunityHost(request));
        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, null,
                model.get(MODEL_URI).toString());
        model.put(MODEL_LOGIN_REDIRECT, urlBuilder.asSiteRelative(request));
        model.put(MODEL_REPLIES_PER_DISCUSSION, DEFAULT_REPLIES_PER_DISCSSION);
        model.put(MODEL_RAISE_YOUR_HAND, isRaiseYourHand());
        model.put(MODEL_RAISE_YOUR_HAND_FEATURED_QUESTIONS, isRaiseYourHandFeaturedQuestions());
        model.put(MODEL_PAGE_TITLE, pageTitle);
        model.put(MODEL_TITLE, title);
        
        if (board != null && board.getCity() != null && board.getCity().getState() != null) {
            _stateSpecificFooterHelper.displayPopularCitiesForState(board.getCity().getState(), model);
        }

        return new ModelAndView(_viewName, model);
    }

    protected List<DiscussionFacade> populateFacades(CmsDiscussionBoard board, List<Discussion> discussions, boolean showBoardForEachDiscussion) {
        List<DiscussionFacade> facades = new ArrayList<DiscussionFacade>(discussions.size());

        for (Discussion discussion: discussions) {
            if (board == null) {
                board = _cmsDiscussionBoardDao.get(discussion.getBoardId());
            }
            discussion.setDiscussionBoard(board);
            List<DiscussionReply> replies = _discussionReplyDao.getRepliesForPage
                    (discussion, 1, DEFAULT_REPLIES_PER_DISCSSION, IDiscussionReplyDao.DiscussionReplySort.NEWEST_FIRST);
            int totalReplies = _discussionReplyDao.getTotalReplies(discussion);
            DiscussionFacade facade = new DiscussionFacade(discussion, replies);
            if (showBoardForEachDiscussion) {
                    facade.setDiscussionBoard(_cmsDiscussionBoardDao.get(discussion.getBoardId()));
                }
            facade.setTotalReplies(totalReplies);
            facades.add(facade);
        }
        return facades;
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
     * Extract the page size from either the request or the cookie.
     */
    protected int getPageSize(HttpServletRequest request) {
        int pageSize = DEFAULT_PAGE_SIZE;

        // use request parameter value if present
        String pageSizeParam = request.getParameter(PARAM_PAGE_SIZE);
        if (pageSizeParam != null) {
            try {
                pageSize = Integer.valueOf(pageSizeParam);
            } catch (NumberFormatException nfe) {
                // nothing
            }
        } else {
            // otherwise check for cookied value
            // TODO: read cookie
        }

        return pageSize;
    }

    protected DiscussionSort getDiscussionSort(HttpServletRequest request, HttpServletResponse response) {
        DiscussionSort sort = DiscussionSort.RECENT_ACTIVITY;
        String sortParam = request.getParameter(PARAM_SORT);
        SitePrefCookie cookie = new SitePrefCookie(request, response);
        if (sortParam != null) {
            // if there is a request parameter, use it and write it into the site pref cookie
            sort = getDiscussionSortFromString(sortParam);
            // write cookie
            cookie.setProperty(COOKIE_SORT_PROPERTY, sortParam);
        } else {
            // otherwise check for cookied value
            String sortCookieVal = cookie.getProperty(COOKIE_SORT_PROPERTY);
            if (sortCookieVal != null) {
                sort = getDiscussionSortFromString(sortCookieVal);
            }
        }

        return sort;
    }

    /**
     * Extract the sort enum from a string. Defaults to NEWEST_FIRST
     */
    protected DiscussionSort getDiscussionSortFromString(String sort) {
        if (StringUtils.equals("oldest_first", sort)) {
            return DiscussionSort.OLDEST_FIRST;
        } else if (StringUtils.equals("newest_first", sort)) {
            return DiscussionSort.NEWEST_FIRST;
        }
        return DiscussionSort.RECENT_ACTIVITY;
    }

    /**
     * Get the list of discussions for this particular page.
     * @param board Discussion board containing the discussions
     * @param page What page are we on?
     * @param pageSize How many discussions are there per page?
     * @param sort What's the sort order?
     * @return non-null list
     */
    protected List<Discussion> getDiscussionsForPage
            (CmsDiscussionBoard board, int page, int pageSize, DiscussionSort sort, boolean includeInactive, boolean raiseYourHand) {
        List<Discussion> discussions;

        if (isRaiseYourHand() && isRaiseYourHandFeaturedQuestions()) {
            List<RaiseYourHandFeature> features = _raiseYourHandDao.getFeatures(HOME_PAGE_CONTENT_KEY, -1);
            discussions = new ArrayList<Discussion>();
            for (RaiseYourHandFeature feature : features) {
                discussions.add(feature.getDiscussion());
            }
        } else {
            discussions = _discussionDao.getDiscussionsForPage(board, page, pageSize, sort, includeInactive, raiseYourHand);
        }

        return discussions;
    }

    /**
     * Get the total number of discussions in the provided board.
     */
    protected long getTotalDiscussions(CmsDiscussionBoard board, boolean includeInactive, boolean raiseYourHand, Collection<Long> excludeBoardIds) {
        long totalDiscussions;

        totalDiscussions = _discussionDao.getTotalDiscussions(board, includeInactive, raiseYourHand, excludeBoardIds);

        return totalDiscussions;
    }

    /**
     * Calculate how many pages are needed to display totalDiscussions given pageSize
     */
    protected long getTotalPages(int pageSize, long totalDiscussions) {
        long totalPages = 1;

        if (pageSize > 0 && totalDiscussions > 0 && totalDiscussions > pageSize) {
            totalPages = ((totalDiscussions + pageSize - 1) / pageSize);
        }

        return totalPages;
    }

    protected void populateWithUsers(List<Discussion> discussions, List<DiscussionFacade> facades) {
        List<UserContent> userContents = new ArrayList<UserContent>();
        for (DiscussionFacade facade: facades) {
            userContents.addAll(facade.getReplies());
        }
        userContents.addAll(discussions);
        _userDao.populateWithUsers(userContents);
    }

    /*
     * Bean accessors/mutators below here
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

    public Boolean isRaiseYourHand() {
        return _raiseYourHand;
    }

    public void setRaiseYourHand(Boolean raiseYourHand) {
        _raiseYourHand = raiseYourHand;
    }

    public Boolean isRaiseYourHandFeaturedQuestions() {
        return _raiseYourHandFeaturedQuestions;
    }

    public void setRaiseYourHandFeaturedQuestions(Boolean raiseYourHandFeaturedQuestions) {
        _raiseYourHandFeaturedQuestions = raiseYourHandFeaturedQuestions;
    }

    public IRaiseYourHandDao getRaiseYourHandDao() {
        return _raiseYourHandDao;
    }

    public void setRaiseYourHandDao(IRaiseYourHandDao raiseYourHandDao) {
        _raiseYourHandDao = raiseYourHandDao;
    }

    public ILocalBoardDao getLocalBoardDao() {
        return _localBoardDao;
    }

    public void setLocalBoardDao(ILocalBoardDao localBoardDao) {
        _localBoardDao = localBoardDao;
    }

    public StateSpecificFooterHelper getStateSpecificFooterHelper() {
        return _stateSpecificFooterHelper;
    }

    public void setStateSpecificFooterHelper(StateSpecificFooterHelper stateSpecificFooterHelper) {
        _stateSpecificFooterHelper = stateSpecificFooterHelper;
    }
}