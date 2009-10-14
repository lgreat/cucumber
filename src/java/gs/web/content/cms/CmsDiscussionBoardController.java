package gs.web.content.cms;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import gs.data.content.cms.CmsDiscussionBoard;
import gs.data.content.cms.ICmsDiscussionBoardDao;
import gs.data.content.cms.CmsTopicCenter;
import gs.data.cms.IPublicationDao;
import gs.data.community.*;

import static gs.data.community.IDiscussionDao.DiscussionSort;
import gs.web.util.SitePrefCookie;
import gs.web.util.UrlBuilder;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.community.DiscussionFacade;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CmsDiscussionBoardController extends AbstractController {
    protected final Log _log = LogFactory.getLog(getClass());

    public static final String VIEW_NOT_FOUND = "/status/error404.page";
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final String DEFAULT_SORT = "newest_first";

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
    public static final String MODEL_DISCUSSION_TOPICS = "discussionTopics";
    public static final String MODEL_URI = "uri";
    public static final String MODEL_LOGIN_REDIRECT = "loginRedirectUrl";

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

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();
        Map<String, Object> model = new HashMap<String, Object>();

        Long contentId;
        try {
            contentId = new Long(request.getParameter("content"));
        } catch (Exception e) {
            _log.warn("Invalid content identifier: " + request.getParameter("content"));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return new ModelAndView(VIEW_NOT_FOUND);
        }

        CmsDiscussionBoard board = _cmsDiscussionBoardDao.get(contentId);

        if (board != null) {
            model.put(MODEL_URI, uri + "?content=" + board.getContentKey().getIdentifier());
            model.put(MODEL_DISCUSSION_BOARD, board);
            CmsTopicCenter topicCenter = _publicationDao.populateByContentId
                    (board.getTopicCenterId(), new CmsTopicCenter());

            SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
            User user;
            if(PageHelper.isMemberAuthorized(request)){
                user = sessionContext.getUser();
                if (user != null) {
                    model.put(MODEL_VALID_USER, user);
                }
            }

            model.put(MODEL_TOPIC_CENTER, topicCenter);
            int page = getPageNumber(request);
            int pageSize = getPageSize(request);
            DiscussionSort sort = getDiscussionSort(request, response);
            model.put(MODEL_PAGE, page);
            model.put(MODEL_PAGE_SIZE, pageSize);
            model.put(MODEL_SORT, sort);

            List<Discussion> discussions = getDiscussionsForPage(board, page, pageSize, sort);
            List<DiscussionFacade> facades = populateFacades(board, discussions);
            populateWithUsers(discussions, facades);
            model.put(MODEL_DISCUSSION_LIST, facades);

            long totalDiscussions = getTotalDiscussions(board);
            model.put(MODEL_TOTAL_DISCUSSIONS, totalDiscussions);
            model.put(MODEL_TOTAL_PAGES, getTotalPages(pageSize, totalDiscussions));
            model.put(MODEL_CURRENT_DATE, new Date());

            model.put(MODEL_COMMUNITY_HOST, sessionContext.getSessionContextUtil().getCommunityHost(request));
            populateModelWithListOfValidDiscussionTopics(model);
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, null,
                    model.get(MODEL_URI).toString());
            model.put(MODEL_LOGIN_REDIRECT, urlBuilder.asSiteRelative(request));
        } else {
            _log.warn("Can't find board with id " + contentId);
        }
        if (model.get(MODEL_DISCUSSION_BOARD) == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return new ModelAndView(VIEW_NOT_FOUND);            
        }

        return new ModelAndView(_viewName, model);
    }

    protected List<DiscussionFacade> populateFacades(CmsDiscussionBoard board, List<Discussion> discussions) {
        List<DiscussionFacade> facades = new ArrayList<DiscussionFacade>(discussions.size());

        for (Discussion discussion: discussions) {
            discussion.setDiscussionBoard(board);
            List<DiscussionReply> replies = _discussionReplyDao.getRepliesForPage
                    (discussion, 1, 2, IDiscussionReplyDao.DiscussionReplySort.NEWEST_FIRST);
            int totalReplies = _discussionReplyDao.getTotalReplies(discussion);
            DiscussionFacade facade = new DiscussionFacade(discussion, replies);
            facade.setTotalReplies(totalReplies);
            facades.add(facade);
        }
        return facades;
    }

    protected void populateModelWithListOfValidDiscussionTopics(Map<String, Object> model) {
        Collection<CmsTopicCenter> topicCenters =
                _publicationDao.populateAllByContentType("TopicCenter", new CmsTopicCenter());
        SortedSet<CmsTopicCenter> sortedTopics = new TreeSet<CmsTopicCenter>(new Comparator<CmsTopicCenter>() {
            public int compare(CmsTopicCenter o1, CmsTopicCenter o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });
        for (CmsTopicCenter topicCenter: topicCenters) {
            if (topicCenter.getDiscussionBoardId() != null && topicCenter.getDiscussionBoardId() > 0) {
                sortedTopics.add(topicCenter);
            }
        }
        model.put(MODEL_DISCUSSION_TOPICS, sortedTopics);
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
        DiscussionSort sort = DiscussionSort.NEWEST_FIRST;
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
        }
        return DiscussionSort.NEWEST_FIRST;
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
            (CmsDiscussionBoard board, int page, int pageSize, DiscussionSort sort) {
        List<Discussion> discussions;

        discussions = _discussionDao.getDiscussionsForPage(board, page, pageSize, sort);

        return discussions;
    }

    /**
     * Get the total number of discussions in the provided board.
     */
    protected long getTotalDiscussions(CmsDiscussionBoard board) {
        long totalDiscussions;

        totalDiscussions = _discussionDao.getTotalDiscussions(board);

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
}
