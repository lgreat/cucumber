package gs.web.content.cms;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import gs.data.content.cms.CmsDiscussionBoard;
import gs.data.content.cms.ICmsDiscussionBoardDao;
import gs.data.content.cms.CmsTopicCenter;
import gs.data.cms.IPublicationDao;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CmsDiscussionBoardController extends AbstractController {
    public static final String VIEW_NOT_FOUND = "/status/error404.page";

    public static final String MODEL_DISCUSSION_BOARD = "discussionBoard";
    public static final String MODEL_TOPIC_CENTER = "topicCenter";
    public static final String PARAM_PAGE = "page";
    private String _viewName;
    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private IPublicationDao _publicationDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();

        Long contentId;
        try {
            contentId = new Long(request.getParameter("content"));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return new ModelAndView(VIEW_NOT_FOUND);
        }

        CmsDiscussionBoard board = _cmsDiscussionBoardDao.get(contentId);

        if (board != null) {
//            model.board = board
            model.put(MODEL_DISCUSSION_BOARD, board);
            CmsTopicCenter topicCenter = _publicationDao.populateByContentId
                    (board.getTopicCenterId(), new CmsTopicCenter());
            if (topicCenter != null) {
//                model.topicCenter = board.topicCenter OR _publicationDao.findById(tcId)
                model.put(MODEL_TOPIC_CENTER, topicCenter);
            }
//            model.discussions = getDiscussionsForPage(board, request.getParameter("page"));
//            model.totalPages = getTotalPages(board, cookie.board_page_size);
//            model.page = request.getParameter("page");
//            model.pageSize = cookie.board_page_size
        }

        return new ModelAndView(_viewName, model);
    }

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

    public IPublicationDao getPublicationDao() {
        return _publicationDao;
    }

    public void setPublicationDao(IPublicationDao publicationDao) {
        _publicationDao = publicationDao;
    }
}
