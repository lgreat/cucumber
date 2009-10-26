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
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.UrlBuilder;

/**
 * @author Dave Roy <mailto:droy@greatschools.net>
 */

public class RecentDiscussionsController extends AbstractController {
    protected final Log _log = LogFactory.getLog(getClass());

    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private IDiscussionDao _discussionDao;
    private IDiscussionReplyDao _discussionReplyDao;
    private IUserDao _userDao;
    private String _viewName;

    public static final String VIEW_NOT_FOUND = "/status/error404.page";
    public static final String PARAM_BOARD_ID = "board_id";
    public static final String PARAM_LIMIT = "limit";
    public static final String PARAM_CALLER_URI = "uri";

    public static final String MODEL_DISCUSSION_LIST = "discussions";
    public static final String MODEL_DISCUSSION_BOARD = "discussionBoard";
    public static final String MODEL_COMMUNITY_HOST = "communityHost";
    public static final String MODEL_CURRENT_DATE = "currentDate";
    public static final String MODEL_LOGIN_REDIRECT = "loginRedirectUrl";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        try {
            Long contentId = new Long(request.getParameter(PARAM_BOARD_ID));
            CmsDiscussionBoard board = _cmsDiscussionBoardDao.get(contentId);
            Integer limit = new Integer(request.getParameter(PARAM_LIMIT));

            if (board != null) {
                model.put(MODEL_DISCUSSION_BOARD, board);
                List<Discussion> discussions = _discussionDao.getDiscussionsForPage(board, 1, limit, IDiscussionDao.DiscussionSort.RECENT_ACTIVITY);
                List<UserContent> userContents = new ArrayList<UserContent>(discussions);
                _userDao.populateWithUsers(userContents);

                List<DiscussionFacade> facades = new ArrayList<DiscussionFacade>(discussions.size());
                for (Discussion discussion : discussions) {
                    DiscussionFacade facade = new DiscussionFacade(discussion, null);
                    facade.setTotalReplies(_discussionReplyDao.getTotalReplies(discussion));
                    facades.add(facade);
                }
                model.put(MODEL_DISCUSSION_LIST, facades);
                model.put(MODEL_CURRENT_DATE, new Date());

                SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
                model.put(MODEL_COMMUNITY_HOST, sessionContext.getSessionContextUtil().getCommunityHost(request));

                String url = request.getParameter(PARAM_CALLER_URI);
                UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, null, url);
                model.put(MODEL_LOGIN_REDIRECT, urlBuilder.asSiteRelative(request));
            }
        } catch (Exception e) {
            // do nothing, module will render blank
            _log.warn("Invalid invocation of RecentDiscussionController");
        }

        return new ModelAndView(_viewName, model);
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

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
}