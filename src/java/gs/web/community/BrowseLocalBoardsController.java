package gs.web.community;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.*;

import gs.data.content.cms.ICmsDiscussionBoardDao;
import gs.data.content.cms.CmsDiscussionBoard;
import gs.data.community.*;
import gs.data.community.local.ILocalBoardDao;
import gs.data.community.local.LocalBoard;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.UrlBuilder;

/**
 * @author Dave Roy <mailto:droy@greatschools.org>
 */

public class BrowseLocalBoardsController extends AbstractController {
    protected final Log _log = LogFactory.getLog(getClass());

    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private ILocalBoardDao _localBoardDao;
    private String _viewName;

    public static final String MODEL_LOCAL_BOARDS = "localBoards";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();

        List<LocalBoard> localBoards = _localBoardDao.getLocalBoards();
        Set<Long> ids = new HashSet<Long>();
        for (LocalBoard localBoard : localBoards) {
            ids.add(localBoard.getBoardId());
        }

        // Get a mapping of all the board ids to their discussion board objects
        Map<Long, CmsDiscussionBoard> boardMap = _cmsDiscussionBoardDao.get(ids);

        List<LocalBoardView> localBoardViews = new ArrayList<LocalBoardView>();
        for (LocalBoard localBoard: localBoards) {
            CmsDiscussionBoard discussionBoard = boardMap.get(localBoard.getBoardId());
            if (discussionBoard != null) {
                localBoard.setDiscussionBoard(discussionBoard);
                UrlBuilder urlBuilder = new UrlBuilder(discussionBoard.getContentKey(), localBoard.getDiscussionBoard().getFullUri());

                LocalBoardView view = new LocalBoardView(localBoard, urlBuilder.asSiteRelative(request));
                localBoardViews.add(view);
            }
        }

        model.put(MODEL_LOCAL_BOARDS, localBoardViews);
        
        return new ModelAndView(_viewName, model);
    }

    public ICmsDiscussionBoardDao getCmsDiscussionBoardDao() {
        return _cmsDiscussionBoardDao;
    }

    public void setCmsDiscussionBoardDao(ICmsDiscussionBoardDao cmsDiscussionBoardDao) {
        _cmsDiscussionBoardDao = cmsDiscussionBoardDao;
    }

    public ILocalBoardDao getLocalBoardDao() {
        return _localBoardDao;
    }

    public void setLocalBoardDao(ILocalBoardDao localBoardDao) {
        _localBoardDao = localBoardDao;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
}