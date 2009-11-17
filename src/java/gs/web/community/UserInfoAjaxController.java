package gs.web.community;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.security.Permission;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.ReadWriteController;
import gs.web.util.PageHelper;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class UserInfoAjaxController extends AbstractController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String PARAM_ABOUT_ME = "aboutMe";
    public static final String PARAM_MEMBER_ID = "memberId";

    private IUserDao _userDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        if (sessionContext == null) {
            _log.warn("No SessionContext found in request.");
            return null;
        }
        User viewer = sessionContext.getUser();
        if (viewer == null) {
            _log.warn("No User in request.");
            return null;
        }
        if (viewer.getUserProfile() == null) {
            _log.warn("User in request has no user profile (email=" + viewer.getEmail() + ").");
            return null;
        }
        if (!PageHelper.isMemberAuthorized(request)) {
            _log.warn("User in request is not authorized (email=" + viewer.getEmail() + ").");
            return null;
        }

        String aboutMe = request.getParameter(PARAM_ABOUT_ME);
        if (aboutMe == null) {
            _log.warn("No aboutMe in request.");
            return null;
        }

        User pageUser;
        try {
            int pageUserId = Integer.parseInt(request.getParameter(PARAM_MEMBER_ID));
            if (pageUserId != viewer.getId()) {
                pageUser = _userDao.findUserFromId(pageUserId);
            } else {
                pageUser = viewer;
            }
        } catch (Exception e) {
            pageUser = null;
        }

        boolean canEdit = viewer.hasPermission(Permission.USER_EDIT_MEMBER_DETAILS);
        if (pageUser != null && (viewer.getId() == pageUser.getId() || canEdit)) {
            pageUser.getUserProfile().setAboutMe(aboutMe);
        }

        _userDao.saveUser(pageUser);
        return null;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }
}
