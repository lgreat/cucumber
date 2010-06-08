package gs.web.community;

import gs.web.geo.StateSpecificFooterHelper;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.cms.IPublicationDao;
import gs.data.community.*;
import gs.data.content.cms.ICmsDiscussionBoardDao;
import gs.data.content.cms.CmsDiscussionBoard;
import gs.data.content.cms.CmsTopicCenter;
import gs.data.content.cms.CmsCategory;

import java.util.*;

import static gs.data.community.IDiscussionReplyDao.DiscussionReplySort;
import gs.data.security.Permission;
import gs.data.util.CommunityUtil;
import gs.web.util.SitePrefCookie;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.content.cms.CmsContentUtils;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class DiscussionController extends AbstractController {
    protected final Log _log = LogFactory.getLog(getClass());

    public static final String VIEW_NOT_FOUND = "/status/error404.page";
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int RAISE_YOUR_HAND_PAGE_SIZE = 10;

    public static final String MODEL_DISCUSSION_BOARD = "discussionBoard";
    public static final String MODEL_DISCUSSION = "discussion";
    public static final String MODEL_REPLIES = "replies";
    public static final String MODEL_TOTAL_REPLIES = "totalReplies";
    public static final String MODEL_TOTAL_PAGES = "totalPages";
    public static final String MODEL_TOPIC_CENTER = "topicCenter";
    public static final String MODEL_PAGE = "page";
    public static final String MODEL_PAGE_SIZE = "pageSize";
    public static final String MODEL_SORT = "sort";
    public static final String MODEL_VALID_USER = "validUser";
    public static final String MODEL_USER_REPLY_MESSAGE = "userReplyMessage";
    public static final String MODEL_USER_REPLY = "userReply";
    public static final String MODEL_CURRENT_DATE = "currentDate";
    public static final String MODEL_COMMUNITY_HOST = "communityHost";
    public static final String MODEL_URI = "uri";
    public static final String MODEL_LOGIN_REDIRECT = "loginRedirectUrl";
    public static final String MODEL_ALMOND_NET_CATEGORY = "almondNetCategory";
    public static final String MODEL_REPLY_REPORTS = "replyReports";    
    public static final String MODEL_DISCUSSION_REPORT = "discussionReport";
    public static final String MODEL_ALL_TOPIC_CENTERS = "allTopicCenters";

    public static final String PARAM_PAGE = "page";
    public static final String PARAM_PAGE_SIZE = "pageSize";
    public static final String PARAM_SORT = "sort";
    public static final String PARAM_USER_REPLY = "userReply";
    public static final String PARAM_DISCUSSION_REPLY_ID = "discussionReplyId";

    public static final String COOKIE_SORT_PROPERTY = "dReplySort";

    private String _viewName;
    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private IDiscussionDao _discussionDao;
    private IDiscussionReplyDao _discussionReplyDao;
    private IPublicationDao _publicationDao;
    private IUserDao _userDao;
    private IReportedEntityDao _reportedEntityDao;
    private StateSpecificFooterHelper _stateSpecificFooterHelper;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();
        Map<String, Object> model = new HashMap<String, Object>();

        Integer contentId;
        try {
            contentId = new Integer(request.getParameter("content"));
        } catch (Exception e) {
            _log.warn("Invalid discussion id provided: " + request.getParameter("content"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return new ModelAndView(VIEW_NOT_FOUND);
        }

        Discussion discussion = _discussionDao.findById(contentId);
        if (discussion == null) {
            _log.warn("Can't find discussion with id " + contentId);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return new ModelAndView(VIEW_NOT_FOUND);
        }
        model.put(MODEL_DISCUSSION, discussion);
        model.put(MODEL_URI, uri + "?content=" + discussion.getId());

        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user = null;
        if(PageHelper.isMemberAuthorized(request)){
            user = sessionContext.getUser();
            if (user != null) {
                model.put(MODEL_VALID_USER, user);
                if (user.hasPermission(Permission.COMMUNITY_MANAGE_RAISE_YOUR_HAND)) {
                    List<CmsTopicCenter> allTopicCenters = _publicationDao
                            .populateAllByContentType("TopicCenter", new CmsTopicCenter());
                    Collections.sort(allTopicCenters);
                    model.put(MODEL_ALL_TOPIC_CENTERS, allTopicCenters);
                }
            }
        }

        if (!discussion.isActive()
                && (user == null
                || user.getUserProfile() == null
                || !user.hasPermission(Permission.COMMUNITY_VIEW_REPORTED_POSTS))) {
            _log.warn("Attempted accesss of deactivated discussion " + discussion.getId());
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return new ModelAndView(VIEW_NOT_FOUND);
        }
        CmsDiscussionBoard board = _cmsDiscussionBoardDao.get(discussion.getBoardId());
        if (board != null) {
            model.put(MODEL_DISCUSSION_BOARD, board);
            CmsTopicCenter topicCenter = _publicationDao
                    .populateByContentId(board.getTopicCenterId(), new CmsTopicCenter());

            model.put(MODEL_TOPIC_CENTER, topicCenter);

            boolean includeInactive = false;
            if (user != null && user.hasPermission(Permission.COMMUNITY_VIEW_REPORTED_POSTS)) {
                includeInactive = true;
            }

            int pageSize = getPageSize(request, discussion.isRaiseYourHand());
            DiscussionReplySort sort = getReplySort(request, response, discussion.isRaiseYourHand());
            model.put(MODEL_PAGE_SIZE, pageSize);
            model.put(MODEL_SORT, sort);
            int page = getPageNumber(request, discussion, pageSize, sort, includeInactive);
            model.put(MODEL_PAGE, page);

            List<DiscussionReply> replies = getRepliesForPage(discussion, page, pageSize, sort, includeInactive);
            Map<Integer, Boolean> reports = getReportsForReplies(user, replies);
            if (user != null) {
                model.put(MODEL_DISCUSSION_REPORT, _reportedEntityDao.hasUserReportedEntity
                        (user, ReportedEntity.ReportedEntityType.discussion, discussion.getId()));
            }
            List<UserContent> userContents = new ArrayList<UserContent>(replies);
            userContents.add(discussion);
            _userDao.populateWithUsers(userContents);
            model.put(MODEL_REPLIES, replies);
            model.put(MODEL_REPLY_REPORTS, reports);
            int totalReplies = getTotalReplies(discussion, includeInactive);
            model.put(MODEL_TOTAL_REPLIES, totalReplies);
            model.put(MODEL_TOTAL_PAGES, getTotalPages(pageSize, totalReplies));
            model.put(MODEL_CURRENT_DATE, new Date());

            model.put(MODEL_COMMUNITY_HOST, sessionContext.getSessionContextUtil().getCommunityHost(request));

            if (user == null) {
                UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, null, model.get(MODEL_URI).toString());
                model.put(MODEL_LOGIN_REDIRECT, urlBuilder.asSiteRelative(request));
            } else {
                model.put(MODEL_LOGIN_REDIRECT, "#");                
            }

            model.put(MODEL_ALMOND_NET_CATEGORY, CmsContentUtils.getAlmondNetCategory(board));

            // Google Ad Manager ad keywords
            PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
            pageHelper.addAdKeyword(CommunityUtil.COMMUNITY_GAM_AD_ATTRIBUTE_KEY, String.valueOf(true));
            if (board.getCity() != null) {
                pageHelper.addAdKeyword(CommunityUtil.CITY_GAM_AD_ATTRIBUTE_KEY, board.getCity().getName());
                pageHelper.addAdKeyword(CommunityUtil.STATE_GAM_AD_ATTRIBUTE_KEY, board.getCity().getState().getAbbreviation());
            } else {
                for (CmsCategory category : board.getUniqueKategoryBreadcrumbs()) {
                    pageHelper.addAdKeywordMulti(CommunityUtil.EDITORIAL_GAM_AD_ATTRIBUTE_KEY, category.getName());
                }
            }

            // Sample code to pull out rejected reply body and restore it in field
//            SitePrefCookie cookie = new SitePrefCookie(request, response);
//            if (StringUtils.isNotEmpty(cookie.getProperty(DiscussionSubmissionController.COOKIE_REPLY_BODY_PROPERTY))) {
//                model.put(MODEL_USER_REPLY, cookie.getProperty(DiscussionSubmissionController.COOKIE_REPLY_BODY_PROPERTY));
//                model.put(MODEL_USER_REPLY_MESSAGE, "Reply must be at least " +
//                        DiscussionSubmissionController.REPLY_BODY_MINIMUM_LENGTH + " characters.");
//                cookie.removeProperty(DiscussionSubmissionController.COOKIE_REPLY_BODY_PROPERTY);
//            }
        }

        if (model.get(MODEL_DISCUSSION_BOARD) == null) {
            _log.warn("Can't find discussion board for discussion with id " + contentId);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return new ModelAndView(VIEW_NOT_FOUND);
        }

        if (board != null && board.getCity() != null && board.getCity().getState() != null) {
            _stateSpecificFooterHelper.placePopularCitiesInModel(board.getCity().getState(), model);
        }


        return new ModelAndView(_viewName, model);
    }

    private Map<Integer, Boolean> getReportsForReplies(User user, List<DiscussionReply> replies) {
        if (replies == null || user == null) {
            return null;
        }
        Map<Integer, Boolean> reports = new HashMap<Integer, Boolean>(replies.size());
        for (DiscussionReply reply: replies) {
            reports.put(reply.getId(),
                        _reportedEntityDao.hasUserReportedEntity
                                (user, ReportedEntity.ReportedEntityType.reply, reply.getId()));
        }
        return reports;
    }

    /**
     * Extract the page number from the request. Defaults to 1. If a discussion reply id is specified,
     * then uses that reply to determine what page to show.
     */
    protected int getPageNumber(HttpServletRequest request, Discussion discussion, int pageSize,
                                DiscussionReplySort sort, boolean includeInactive) {
        int page = -1;
        if (request.getParameter(PARAM_DISCUSSION_REPLY_ID) != null) {
            try {
                int discussionReplyId = Integer.valueOf(request.getParameter(PARAM_DISCUSSION_REPLY_ID));
                DiscussionReply reply = _discussionReplyDao.findById(discussionReplyId);
                if (reply != null && reply.getDiscussion().getId().equals(discussion.getId())) {
                    page = _discussionReplyDao.getPageForReply(discussion, reply, pageSize, sort, includeInactive);
                    _log.info("Page with discussion is " + page);
                }
            } catch (NumberFormatException nfe) {
                // nothing
            } catch (Exception e) {
                _log.warn("Error attempting to determine what page discussion " +
                        request.getParameter(PARAM_DISCUSSION_REPLY_ID) + " lies on", e);
            }
        }

        if (page < 1) {
            page = 1; // default back to 1
        }
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
    protected int getPageSize(HttpServletRequest request, boolean isRaiseYourHand) {
        if (isRaiseYourHand) {
            return RAISE_YOUR_HAND_PAGE_SIZE;
        }
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
            // otherwise check for cookied value ?? Or is this a cookied preference?
        }

        return pageSize;
    }

    protected DiscussionReplySort getReplySort(HttpServletRequest request, HttpServletResponse response, boolean isRaiseYourHand) {
        if (isRaiseYourHand) {
            return DiscussionReplySort.NEWEST_FIRST;
        }
        DiscussionReplySort sort = DiscussionReplySort.OLDEST_FIRST;

        String sortParam = request.getParameter(PARAM_SORT);
        SitePrefCookie cookie = new SitePrefCookie(request, response);
        if (sortParam != null) {
            // if there is a request parameter, use it and write it into the site pref cookie
            sort = getReplySortFromString(sortParam);
            // write cookie
            cookie.setProperty(COOKIE_SORT_PROPERTY, sortParam);
        } else {
            // otherwise check for cookied value
            String sortCookieVal = cookie.getProperty(COOKIE_SORT_PROPERTY);
            if (sortCookieVal != null) {
                sort = getReplySortFromString(sortCookieVal);
            }
        }

        return sort;
    }

    /**
     * Extract the sort enum from a string. Defaults to NEWEST_FIRST
     */
    protected DiscussionReplySort getReplySortFromString(String sort) {
        if (StringUtils.equals("oldest_first", sort)) {
            return DiscussionReplySort.OLDEST_FIRST;
        } else  {
            return DiscussionReplySort.NEWEST_FIRST;
        }
    }

    /**
     * Get the list of replies for this particular page.
     * @param discussion Discussion  containing the replies
     * @param page What page are we on?
     * @param pageSize How many replies are there per page?
     * @param sort What's the sort order?
     * @return non-null list
     */
    protected List<DiscussionReply> getRepliesForPage
            (Discussion discussion, int page, int pageSize, DiscussionReplySort sort, boolean includeInactive) {
        List<DiscussionReply> replies;

        replies = _discussionReplyDao.getRepliesForPage(discussion, page, pageSize, sort, includeInactive);

        return replies;
    }

    /**
     * Get the total number of replies in the provided discussion.
     */
    protected int getTotalReplies(Discussion discussion, boolean includeInactive) {
        int totalReplies;

        totalReplies = _discussionReplyDao.getTotalReplies(discussion, includeInactive);

        return totalReplies;
    }

    /**
     * Calculate how many pages are needed to display totalDiscussions given pageSize
     */
    protected int getTotalPages(int pageSize, int totalReplies) {
        int totalPages = 1;

        if (pageSize > 0 && totalReplies > 0 && totalReplies > pageSize) {
            totalPages = ((totalReplies + pageSize - 1) / pageSize);
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

    public IReportedEntityDao getReportedEntityDao() {
        return _reportedEntityDao;
    }

    public void setReportedEntityDao(IReportedEntityDao reportedEntityDao) {
        _reportedEntityDao = reportedEntityDao;
    }

    public StateSpecificFooterHelper getStateSpecificFooterHelper() {
        return _stateSpecificFooterHelper;
    }

    public void setStateSpecificFooterHelper(StateSpecificFooterHelper stateSpecificFooterHelper) {
        _stateSpecificFooterHelper = stateSpecificFooterHelper;
    }
}