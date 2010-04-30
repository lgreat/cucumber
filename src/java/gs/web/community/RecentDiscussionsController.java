package gs.web.community;

import gs.data.community.local.ILocalBoardDao;
import gs.data.content.cms.CmsConstants;
import org.apache.commons.lang.StringUtils;
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
 * @author Dave Roy <mailto:droy@greatschools.org>
 */

public class RecentDiscussionsController extends AbstractController {
    protected final Log _log = LogFactory.getLog(getClass());

    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private IDiscussionDao _discussionDao;
    private IDiscussionReplyDao _discussionReplyDao;
    private ILocalBoardDao _localBoardDao;
    private IUserDao _userDao;
    private String _viewName;

    public static final String DEFAULT_MORE_TEXT = "More conversations";

    public static final String VIEW_NOT_FOUND = "/status/error404.page";
    public static final String PARAM_BOARD_ID = "board_id";
    public static final String PARAM_LIMIT = "limit";
    public static final String PARAM_CALLER_URI = "uri";
    public static final String PARAM_STYLE = "style";
    public static final String PARAM_CITY_NAME = "cityName";
    public static final String PARAM_TITLE = "title";
    public static final String PARAM_MORE_TEXT = "moreText";
    public static final String PARAM_SHOW_LARGE_FIRST_AVATAR = "showLargeFirstAvatar";

    public static final String MODEL_DISCUSSION_LIST = "discussions";
    public static final String MODEL_DISCUSSION_BOARD = "discussionBoard";
    public static final String MODEL_COMMUNITY_HOST = "communityHost";
    public static final String MODEL_CURRENT_DATE = "currentDate";
    public static final String MODEL_LOGIN_REDIRECT = "loginRedirectUrl";
    public static final String MODEL_STYLE = "style";
    public static final String MODEL_CITY_NAME = "cityName";
    public static final String MODEL_TITLE = "title";
    public static final String MODEL_MORE_TEXT = "moreText";
    public static final String MODEL_MORE_URL = "moreUrl";
    public static final String MODEL_SHOW_LARGE_FIRST_AVATAR = "showLargeFirstAvatar";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        try {
            Integer limit = new Integer(request.getParameter(PARAM_LIMIT));
            List<Discussion> discussions;
            boolean showBoardForEachDiscussion = false;

            if (request.getParameter(PARAM_BOARD_ID) != null) {
                Long contentId = new Long(request.getParameter(PARAM_BOARD_ID));
                CmsDiscussionBoard board = _cmsDiscussionBoardDao.get(contentId);
                model.put(MODEL_DISCUSSION_BOARD, board);
                discussions = _discussionDao.getDiscussionsForPage(board, 1, limit, IDiscussionDao.DiscussionSort.RECENT_ACTIVITY, false);
            } else {
                List<Integer> excludeBoardIds = _localBoardDao.getLocalBoardIds();
                discussions = _discussionDao.getDiscussionsForPage(1, limit, IDiscussionDao.DiscussionSort.RECENT_ACTIVITY, false, excludeBoardIds);
                CmsDiscussionBoard board = _cmsDiscussionBoardDao.get(CmsConstants.GENERAL_PARENTING_DISCUSSION_BOARD_ID);
                model.put(MODEL_DISCUSSION_BOARD, board);
                showBoardForEachDiscussion = true;

                UrlBuilder builder = new UrlBuilder(UrlBuilder.RECENT_CONVERSATIONS);
                model.put(MODEL_MORE_URL, builder.asSiteRelative(request));
            }

            List<UserContent> userContents = new ArrayList<UserContent>(discussions);
            _userDao.populateWithUsers(userContents);

            List<DiscussionFacade> facades = new ArrayList<DiscussionFacade>(discussions.size());
            for (Discussion discussion : discussions) {
                DiscussionFacade facade = new DiscussionFacade(discussion, null);
                if (showBoardForEachDiscussion) {
                    facade.setDiscussionBoard(_cmsDiscussionBoardDao.get(discussion.getBoardId()));
                }
                facade.setTotalReplies(_discussionReplyDao.getTotalReplies(discussion));
                if (facade.getTotalReplies() > 0) {
                    DiscussionReply mostRecentReply = _discussionReplyDao.getMostRecentReply(discussion);
                    facade.setMostRecentReplyDateCreated(mostRecentReply.getDateCreated());
                }
                facades.add(facade);
            }
            model.put(MODEL_DISCUSSION_LIST, facades);
            model.put(MODEL_CURRENT_DATE, new Date());

            SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
            model.put(MODEL_COMMUNITY_HOST, sessionContext.getSessionContextUtil().getCommunityHost(request));

            String url = request.getParameter(PARAM_CALLER_URI);
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, null, url);
            model.put(MODEL_LOGIN_REDIRECT, urlBuilder.asSiteRelative(request));

            model.put(MODEL_STYLE, request.getParameter(PARAM_STYLE));
            model.put(MODEL_CITY_NAME, request.getParameter(PARAM_CITY_NAME));

            if (StringUtils.isNotBlank(request.getParameter(PARAM_TITLE))) {
                model.put(MODEL_TITLE, request.getParameter(PARAM_TITLE));
            }

            String moreText = DEFAULT_MORE_TEXT;
            if (StringUtils.isNotBlank(request.getParameter(PARAM_MORE_TEXT))) {
                moreText = request.getParameter(PARAM_MORE_TEXT);

            }
            model.put(MODEL_MORE_TEXT, moreText);

            boolean showLargeFirstAvatar = (request.getParameter(MODEL_SHOW_LARGE_FIRST_AVATAR) == null ||
                    "true".equals(request.getParameter(MODEL_SHOW_LARGE_FIRST_AVATAR)));
            model.put(MODEL_SHOW_LARGE_FIRST_AVATAR, showLargeFirstAvatar);
        } catch (Exception e) {
            // do nothing, module will render blank
            _log.warn("Invalid invocation of RecentDiscussionController", e);
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

    public ILocalBoardDao getLocalBoardDao() {
        return _localBoardDao;
    }

    public void setLocalBoardDao(ILocalBoardDao localBoardDao) {
        _localBoardDao = localBoardDao;
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