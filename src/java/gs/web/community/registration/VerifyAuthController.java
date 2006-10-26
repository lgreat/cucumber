package gs.web.community.registration;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.ISubscriptionDao;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class VerifyAuthController implements Controller {
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String BEAN_ID = "/community/verifyAuth.page";
    public static final int ERROR_CODE = HttpServletResponse.SC_FORBIDDEN;

    private IUserDao _userDao;
    private AuthenticationManager _authenticationManager;
    private ISubscriptionDao _subscriptionDao;
    private String _viewName;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws NoSuchAlgorithmException, IOException {
        Map model = new HashMap();
        GetUserProfileController.UserProfileInfo upi = null;
        String authInfo = request.getParameter(_authenticationManager.getParameterName());
        if (authInfo != null) {
            Integer id = null;
            try {
                id = _authenticationManager.getUserIdFromParameter(authInfo);
                User user = _userDao.findUserFromId(id.intValue());
                if (_authenticationManager.verifyAuthInfo(user, authInfo)) {
                    upi = GetUserProfileController.getUserProfileInfo(user, _subscriptionDao);
                }
            } catch (ObjectRetrievalFailureException orfe) {
                _log.warn("Community authentication request failed because no user with id=" + id +
                        " exists.");
            } catch (Exception e) {
                _log.warn("Community authentication request failed because of exception " +
                        e.getClass().getName() + ": " + e.getMessage());
            }
        }
        if (upi != null) {
            List list = new ArrayList();
            list.add(upi);
            model.put(GetUserProfileController.USER_LIST_PARAMETER_NAME, list);
            return new ModelAndView(getViewName(), model);
        } else {
            response.sendError(ERROR_CODE);
            return null;
        }
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }

    public AuthenticationManager getAuthenticationManager() {
        return _authenticationManager;
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        _authenticationManager = authenticationManager;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
}
