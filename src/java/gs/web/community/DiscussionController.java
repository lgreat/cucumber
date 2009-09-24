package gs.web.community;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
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

import java.util.*;

import static gs.data.community.IDiscussionReplyDao.DiscussionReplySort;
import gs.web.util.SitePrefCookie;
import gs.web.util.ReadWriteController;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class DiscussionController extends AbstractController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());

    public static final String VIEW_NOT_FOUND = "/status/error404.page";
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final String DEFAULT_SORT = "newest_first";

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

    public static final String PARAM_PAGE = "page";
    public static final String PARAM_PAGE_SIZE = "pageSize";
    public static final String PARAM_SORT = "sort";
    public static final String PARAM_USER_REPLY = "userReply";

    public static final String COOKIE_SORT_PROPERTY = "dReplySort";

    private String _viewName;
    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private IDiscussionDao _discussionDao;
    private IDiscussionReplyDao _discussionReplyDao;
    private IPublicationDao _publicationDao;
    private IUserDao _userDao;

    protected boolean isPost(HttpServletRequest request) {
        return request.getMethod().equals("POST");
    }

    protected boolean handlePost(HttpServletRequest request, Map<String, Object> model) {
        User user = (User)model.get(MODEL_VALID_USER);
        if (user != null) {
            String post = request.getParameter(PARAM_USER_REPLY);
            if (post.length() < 5) {
                model.put(MODEL_USER_REPLY, post);
                model.put(MODEL_USER_REPLY_MESSAGE, "Cannot submit post with 5 or fewer characters.");
                return false;
            }

            // TODO: profanity filter
            // TODO: more validation?
            // TODO: sanitize string (strip HTML? JS? SQL?)?
            
            DiscussionReply reply = new DiscussionReply();
            reply.setDiscussion((Discussion)model.get(MODEL_DISCUSSION));
            reply.setBody(post);
            reply.setAuthorId(user.getId());
            _discussionReplyDao.save(reply);
            return true;
        }

        return false;
    }
    
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
        model.put("uri", uri + "?content=" + discussion.getId());

        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User user;
        if(PageHelper.isMemberAuthorized(request)){
            user = sessionContext.getUser();
            if (user != null) {
                model.put(MODEL_VALID_USER, user);
                if (isPost(request)) {
                    boolean postSaved = handlePost(request, model);
                    if (postSaved) {
                        // Redirect back so the browser won't try to post again on a refresh
                        StringBuffer url = request.getRequestURL();
                        url.append("?").append(request.getQueryString());
                        return new ModelAndView(new RedirectView(url.toString()));
                    }
                }
            }
        }
        
        // TODO: switch to read only

        CmsDiscussionBoard board = _cmsDiscussionBoardDao.get(discussion.getBoardId());
        if (board != null) {
            model.put(MODEL_DISCUSSION_BOARD, board);
            CmsTopicCenter topicCenter = _publicationDao
                    .populateByContentId(board.getTopicCenterId(), new CmsTopicCenter());
            if (topicCenter == null) {
                String topicCenterParam = request.getParameter("topicCenterId");
                if (topicCenterParam != null) {
                    topicCenter = _publicationDao.populateByContentId(new Long(topicCenterParam), new CmsTopicCenter());
                }
            }

            // TODO REMOVE THIS USE OF SAMPLE DATA
            if (topicCenter == null) {
               topicCenter = _publicationDao.populateByContentId(15L, new CmsTopicCenter());
            }

            model.put(MODEL_TOPIC_CENTER, topicCenter);

            int page = getPageNumber(request);
            int pageSize = getPageSize(request);
            DiscussionReplySort sort = getReplySort(request, response);
            model.put(MODEL_PAGE, page);
            model.put(MODEL_PAGE_SIZE, pageSize);
            model.put(MODEL_SORT, sort);

            List<DiscussionReply> replies = getRepliesForPage(discussion, page, pageSize, sort);
            updateRepliesWithUsers(replies);
            model.put(MODEL_REPLIES, replies);
            int totalReplies = getTotalReplies(discussion);
            model.put(MODEL_TOTAL_REPLIES, totalReplies);
            model.put(MODEL_TOTAL_PAGES, getTotalPages(pageSize, totalReplies));
            model.put(MODEL_CURRENT_DATE, new Date());

            model.put("communityHost", sessionContext.getSessionContextUtil().getCommunityHost(request));
        }

        if (model.get(MODEL_TOPIC_CENTER) == null) {
            _log.warn("Can't find topic center for discussion with id " + contentId);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return new ModelAndView(VIEW_NOT_FOUND);
        }

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
            // otherwise check for cookied value ?? Or is this a cookied preference?
        }

        return pageSize;
    }

    protected DiscussionReplySort getReplySort(HttpServletRequest request, HttpServletResponse response) {
        DiscussionReplySort sort = DiscussionReplySort.NEWEST_FIRST;

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
            (Discussion discussion, int page, int pageSize, DiscussionReplySort sort) {
        List<DiscussionReply> replies;

        replies = _discussionReplyDao.getRepliesForPage(discussion, page, pageSize, sort);

        if (replies.size() == 0 && "Anthony's Test Discussion".equals(discussion.getTitle())) {
            DiscussionReply reply;
            for (int x=0; x < pageSize; x++) {
                int replyNum = ((page-1) * pageSize) + (x+1);
                if (replyNum > 18) {
                    break;
                }

                reply = new DiscussionReply();
                reply.setActive(true);
                reply.setAuthorId(18283);
                reply.setBody("Reply " + replyNum);
                reply.setDateCreated(new Date());
                reply.setId(replyNum);
                reply.setDiscussion(discussion);
                replies.add(reply);
            }
        }

        return replies;
    }

    protected void updateRepliesWithUsers(List<DiscussionReply> replies) {
        Map<Integer, List<DiscussionReply>> userIdToReplyMap = new HashMap<Integer, List<DiscussionReply>>(replies.size());
        // for each reply
        for (DiscussionReply reply: replies) {
            // add reply to map of author id to list of replies
            List<DiscussionReply> replyList = userIdToReplyMap.get(reply.getAuthorId());
            if (replyList == null) {
                replyList = new ArrayList<DiscussionReply>();
                userIdToReplyMap.put(reply.getAuthorId(), replyList);
            }
            replyList.add(reply);
        }
        if (userIdToReplyMap.isEmpty()) {
            return;
        }
        // get list of users from set of author ids
        List<User> users = _userDao.findUsersFromIds(new ArrayList<Integer>(userIdToReplyMap.keySet()));
        // for each user
        for (User user: users) {
            // get list of replies for that user id
            List<DiscussionReply> replyList = userIdToReplyMap.get(user.getId());
            if (replyList != null) {
                // for each reply
                for (DiscussionReply reply: replyList) {
                    // set user
                    reply.setUser(user);
                }
            }
        }
    }

    /**
     * Get the total number of discussions in the provided board.
     */
    protected int getTotalReplies(Discussion discussion) {
        int totalReplies;

        totalReplies = _discussionReplyDao.getTotalReplies(discussion);

        if (totalReplies == 0 && "Anthony's Test Discussion".equals(discussion.getTitle())) {
            totalReplies = 18;
        }

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
}