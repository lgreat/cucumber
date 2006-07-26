package gs.web.community.registration;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.util.DigestUtil;
import gs.web.util.ReadWriteController;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: UrbanaSoft
 * Date: Jul 11, 2006
 * Time: 4:11:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class RegistrationRemoveController  extends AbstractController implements ReadWriteController {
    public static final String BEAN_ID = "/community/registrationRemove.page";
    protected final Log _log = LogFactory.getLog(getClass());

    private String _viewName;

    private IUserDao _userDao;

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

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map model = new HashMap();

        String idString = request.getParameter("id");

        String hash = idString.substring(0, DigestUtil.MD5_HASH_LENGTH);
        int id = Integer.parseInt(idString.substring(DigestUtil.MD5_HASH_LENGTH));

        User user = getUserDao().findUserFromId(id);
        String email = user.getEmail();
        _log.info("Community registration cancellation request from: " + email);

        // now confirm hash
        String actualHash = DigestUtil.hashStringInt(user.getEmail(), new Integer(id));
        if (!hash.equals(actualHash)) {
            _log.warn("Community registration cancellation request has invalid hash: " + hash);
            throw new IllegalArgumentException("Community registration cancellation request has invalid hash: " + hash);
        }

        if (user.isEmailProvisional()) {
            user.setPasswordMd5(null);
            _userDao.updateUser(user);
        }
        model.put("email", email);
        return new ModelAndView(_viewName, model);
    }
}
