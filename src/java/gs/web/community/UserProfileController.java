package gs.web.community;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.cms.IPublicationDao;
import gs.data.community.*;
import gs.data.content.cms.ICmsDiscussionBoardDao;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.PageHelper;

import java.util.*;

/**
 * @author Dave Roy <mailto:droy@greatschools.net>
 */
public class UserProfileController extends AbstractController {
    protected final Log _log = LogFactory.getLog(getClass());

    public static final String VIEW_NOT_FOUND = "/status/error404.page";

    public static final String MODEL_PAGE_USER = "pageUser";
    public static final String MODEL_VIEWER = "viewer";
    public static final String MODEL_VIEWING_OWN_PROFILE = "viewingOwnProfile";
    public static final String MODEL_RECENT_POSTS = "recentPosts";

    public static final String PARAM_MEMBER_ID = "memberId";

    private String _viewName;
    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private IDiscussionDao _discussionDao;
    private IDiscussionReplyDao _discussionReplyDao;
    private IPublicationDao _publicationDao;
    private IUserDao _userDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> model = new HashMap<String, Object>();

        Integer memberId;
        try {
            memberId = new Integer(request.getParameter(PARAM_MEMBER_ID));
        } catch (Exception e) {
            _log.warn("Invalid member id: " + request.getParameter(PARAM_MEMBER_ID));
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return new ModelAndView(VIEW_NOT_FOUND);
        }
        
        User pageUser;
        try {
            pageUser = _userDao.findUserFromId(memberId);
            model.put(MODEL_PAGE_USER, pageUser);
        } catch (ObjectRetrievalFailureException orfe) {
            _log.warn("Unknown member id: " + memberId);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return new ModelAndView(VIEW_NOT_FOUND);
        }

        boolean viewingOwnProfile = false;
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        User viewer;
        if(PageHelper.isMemberAuthorized(request)){
            viewer = sessionContext.getUser();
            if (viewer != null) {
                model.put(MODEL_VIEWER, viewer);
                if (viewer.equals(pageUser)) {
                    viewingOwnProfile = true;
                }
            }
        }
        model.put(MODEL_VIEWING_OWN_PROFILE, viewingOwnProfile);

        return new ModelAndView(_viewName, model);
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