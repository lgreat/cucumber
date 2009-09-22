package gs.web.content.cms;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import gs.data.content.cms.CmsDiscussionBoard;
import gs.data.content.cms.ICmsDiscussionBoardDao;
import gs.data.content.cms.CmsTopicCenter;
import gs.data.content.cms.ContentKey;
import gs.data.cms.IPublicationDao;
import gs.data.community.Discussion;
import gs.data.community.IDiscussionDao;
import gs.data.community.DiscussionReply;

import static gs.data.community.IDiscussionDao.DiscussionSort;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CmsDiscussionBoardController extends AbstractController {
    public static final String VIEW_NOT_FOUND = "/status/error404.page";
    public static final int DEFAULT_PAGE_SIZE = 5;
    public static final String DEFAULT_SORT = "newest_first";

    public static final String MODEL_DISCUSSION_BOARD = "discussionBoard";
    public static final String MODEL_DISCUSSION_LIST = "discussionList";
    public static final String MODEL_TOTAL_DISCUSSIONS = "totalDiscussions";
    public static final String MODEL_TOTAL_PAGES = "totalPages";
    public static final String MODEL_TOPIC_CENTER = "topicCenter";
    public static final String MODEL_PAGE = "page";
    public static final String MODEL_PAGE_SIZE = "pageSize";
    public static final String MODEL_SORT = "sort";
    
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_PAGE_SIZE = "pageSize";
    public static final String PARAM_SORT = "sort";

    private String _viewName;
    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private IDiscussionDao _discussionDao;
    private IPublicationDao _publicationDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();
        Map<String, Object> model = new HashMap<String, Object>();

        Long contentId;
        try {
            contentId = new Long(request.getParameter("content"));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return new ModelAndView(VIEW_NOT_FOUND);
        }

        CmsDiscussionBoard board = _cmsDiscussionBoardDao.get(contentId);

        // TODO: REMOVE DEBUG CODE!!
        if (board == null && contentId == 999999l) {
            board = new CmsDiscussionBoard();
            board.setTitle("Anthony's Test Board");
            board.setMetaDescription("Anthony's Test Board meta description");
            board.setMetaKeywords("Anthony,Test,Board,Meta,Keywords");
            board.setTopicCenterId(1541); // LD
            board.setContentKey(new ContentKey("CmsDiscussionBoard", 999999l));
        } // END DEBUG CODE

        if (board != null) {
            model.put("uri", uri + "?content=" + board.getContentKey().getIdentifier());
            model.put(MODEL_DISCUSSION_BOARD, board);
            CmsTopicCenter topicCenter = _publicationDao.populateByContentId
                    (board.getTopicCenterId(), new CmsTopicCenter());
            if (topicCenter == null && contentId == 999999l) {
                topicCenter = new CmsTopicCenter();
                topicCenter.setTitle("Anthony Topic Center");
            }
            if (topicCenter != null) {
                model.put(MODEL_TOPIC_CENTER, topicCenter);
                int page = getPageNumber(request);
                int pageSize = getPageSize(request);
                DiscussionSort sort = getDiscussionSortFromString(request.getParameter(PARAM_SORT));
                model.put(MODEL_PAGE, page);
                model.put(MODEL_PAGE_SIZE, pageSize);
                model.put(MODEL_SORT, sort);
                model.put(MODEL_DISCUSSION_LIST, getDiscussionsForPage(board, page, pageSize, sort));
                long totalDiscussions = getTotalDiscussions(board);
                model.put(MODEL_TOTAL_DISCUSSIONS, totalDiscussions);
                model.put(MODEL_TOTAL_PAGES, getTotalPages(pageSize, totalDiscussions));
            }
        }
        if (model.get(MODEL_TOPIC_CENTER) == null) {
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
            // otherwise check for cookied value
            // TODO: read cookie
        }

        return pageSize;
    }

    /**
     * Extract the sort enum from a string. Defaults to NEWEST_FIRST
     */
    protected DiscussionSort getDiscussionSortFromString(String sort) {
        if (StringUtils.equals("oldest_first", sort)) {
            return DiscussionSort.OLDEST_FIRST;
        } else if (StringUtils.equals("most_popular", sort)) {
            return DiscussionSort.MOST_POPULAR;
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

        if (board != null && board.getTitle() != null &&
                "Anthony's Test Board".equals(board.getTitle())) {
            for (int x=0; x < pageSize; x++) {
                int discussionNum = ((page-1) * pageSize) + (x+1);
                if (discussionNum > 23) {
                    break;
                }
                Discussion discussion = new Discussion();
                discussion.setTitle("Discussion " + discussionNum);
                Set<DiscussionReply> replies = new HashSet<DiscussionReply>(2);
                DiscussionReply reply = new DiscussionReply();
                reply.setDiscussion(discussion);
                reply.setActive(true);
                reply.setBody("First reply.");
                reply.setAuthorId(18283);
                reply.setDateCreated(new Date());
                reply.setId(1);
                replies.add(reply);
                reply = new DiscussionReply();
                reply.setDiscussion(discussion);
                reply.setActive(true);
                reply.setBody("Second reply. This one is a bit longer. Well, more than a bit. Maybe a little. A medium amount. An indeterminate amount that is neither too little nor too large, much like the perfect serving size of the perfect meal.");
                reply.setAuthorId(18283);
                reply.setDateCreated(new Date());
                reply.setId(2);
                replies.add(reply);
                discussion.setReplies(replies);
                discussions.add(discussion);
            }
        }

        return discussions;
    }

    /**
     * Get the total number of discussions in the provided board.
     */
    protected long getTotalDiscussions(CmsDiscussionBoard board) {
        long totalDiscussions;

        totalDiscussions = _discussionDao.getTotalDiscussions(board);

        if (board != null && board.getTitle() != null &&
                "Anthony's Test Board".equals(board.getTitle())) {
            totalDiscussions = 23;
        }

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

    public IPublicationDao getPublicationDao() {
        return _publicationDao;
    }

    public void setPublicationDao(IPublicationDao publicationDao) {
        _publicationDao = publicationDao;
    }
}
