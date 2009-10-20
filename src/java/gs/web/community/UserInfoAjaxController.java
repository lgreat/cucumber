package gs.web.community;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.ReadWriteController;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class UserInfoAjaxController extends AbstractController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String PARAM_ABOUT_ME = "aboutMe";

    private IUserDao _userDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        if (sessionContext == null) {
            _log.warn("No SessionContext found in request.");
            return null;
        }
        User user = sessionContext.getUser();
        if (user == null) {
            _log.warn("No User in request.");
            return null;
        }
        if (user.getUserProfile() == null) {
            _log.warn("User in request has no user profile (email=" + user.getEmail() + ").");
            return null;
        }

        String aboutMe = request.getParameter(PARAM_ABOUT_ME);
        if (aboutMe == null) {
            _log.warn("No aboutMe in request.");
            return null;
        }
        user.getUserProfile().setAboutMe(aboutMe);
        _userDao.saveUser(user);
        return null;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }
}
