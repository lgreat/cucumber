package gs.web.community.registration;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.util.DigestUtil;
import gs.web.util.UrlBuilder;
import gs.web.util.ReadWriteController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: UrbanaSoft
 * Date: Jun 27, 2006
 * Time: 9:57:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class RegistrationConfirmController extends AbstractController implements ReadWriteController {
    public static final String BEAN_ID = "/community/registrationConfirm.page";
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

    protected ModelAndView redirectToRegistration(HttpServletRequest request) {
        return redirectToRegistration(request, null);
    }

    protected ModelAndView redirectToRegistration(HttpServletRequest request, String email) {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION, null);
        if (email != null) {
            builder.addParameter("email", email);
        }
        String redirectUrl = builder.asFullUrl(request);
        ModelAndView mAndV = new ModelAndView();
        mAndV.setViewName("redirect:" + redirectUrl);
        return mAndV;
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws NoSuchAlgorithmException {
        Map model = new HashMap();
        String idString = request.getParameter("id");
        if (idString == null) {
            _log.warn("Community registration request with no id, redirecting to registration");
            return redirectToRegistration(request);
        }
        if (idString.length() <= DigestUtil.MD5_HASH_LENGTH) {
            _log.warn("Community registration request with badly formed idString: " + idString + 
                    ", redirecting to registration. Expecting hash of length " +
                    DigestUtil.MD5_HASH_LENGTH + " followed by id.");
            return redirectToRegistration(request);
        }
        String hash = idString.substring(0, DigestUtil.MD5_HASH_LENGTH);
        int id = Integer.parseInt(idString.substring(DigestUtil.MD5_HASH_LENGTH));

        User user;
        try {
            user = getUserDao().findUserFromId(id);
        } catch (ObjectRetrievalFailureException orfe) {
            _log.warn("Community registration request for unknown user id: " + id + ", redirecting to registration");
            return redirectToRegistration(request);
        }
        // now confirm hash
        String actualHash = DigestUtil.hashStringInt(user.getEmail(), new Integer(id));
        if (!hash.equals(actualHash)) {
            _log.warn("Community registration request has invalid hash: " + hash + " for user " +
                    user.getEmail() + ", redirecting to registration");
            return redirectToRegistration(request, user.getEmail());
        }

        if (user.isPasswordEmpty()) {
            // request for new password has expired, redirect to registration
            _log.warn("Community registration request has expired for user " +
                    user.getEmail() + ", redirecting to registration");
            return redirectToRegistration(request, user.getEmail());
        }
        // authenticate user
        if (!user.isEmailValidated()) {
            user.setEmailValidated();
            _userDao.saveUser(user);
        }

        return new ModelAndView(_viewName, model);
    }
}
