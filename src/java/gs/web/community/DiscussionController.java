package gs.web.community;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.cms.IPublicationDao;
import gs.data.community.IDiscussionDao;
import gs.data.community.Discussion;
import gs.data.community.DiscussionReply;
import gs.data.content.cms.ICmsDiscussionBoardDao;
import gs.data.content.cms.CmsDiscussionBoard;
import gs.data.content.cms.CmsTopicCenter;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class DiscussionController extends AbstractController {
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

        Discussion discussion = null; //_discussionDao.findById(contentId);
        if (discussion != null) {
            model.put("uri", uri + "?content=" + discussion.getId());
            model.put(MODEL_DISCUSSION, discussion);
            CmsDiscussionBoard board = _cmsDiscussionBoardDao.get(Long.valueOf(discussion.getBoardId()));
            if (board != null) {
                model.put(MODEL_DISCUSSION_BOARD, board);
                CmsTopicCenter topicCenter = _publicationDao
                        .populateByContentId(board.getTopicCenterId(), new CmsTopicCenter());
                model.put(MODEL_TOPIC_CENTER, topicCenter);

                int page = getPageNumber(request);
                int pageSize = getPageSize(request);
                String sort = getDiscussionSortFromString(request.getParameter(PARAM_SORT));
                model.put(MODEL_PAGE, page);
                model.put(MODEL_PAGE_SIZE, pageSize);
                model.put(MODEL_SORT, sort);

                model.put(MODEL_REPLIES, getRepliesForPage(discussion, page, pageSize, sort));
                long totalReplies = getTotalReplies(discussion);
                model.put(MODEL_TOTAL_REPLIES, totalReplies);
                model.put(MODEL_TOTAL_PAGES, getTotalPages(pageSize, totalReplies));
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
    protected String getDiscussionSortFromString(String sort) {
        // TODO: replace with enum
        if (StringUtils.equals("oldest_first", sort)) {
            return sort;
        } else if (StringUtils.equals("most_popular", sort)) {
            return sort;
        }
        return sort;
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
            (Discussion discussion, int page, int pageSize, String sort) {
        List<DiscussionReply> discussions = new ArrayList<DiscussionReply>(0);

//        discussions = _discussionDao.getRepliesForPage(discussion, page, pageSize, sort);

        return discussions;
    }

    /**
     * Get the total number of discussions in the provided board.
     */
    protected long getTotalReplies(Discussion discussion) {
        long totalDiscussions = 0;

        //totalDiscussions = _discussionDao.getTotalReplies(discussion);

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

    public IPublicationDao getPublicationDao() {
        return _publicationDao;
    }

    public void setPublicationDao(IPublicationDao publicationDao) {
        _publicationDao = publicationDao;
    }
}
