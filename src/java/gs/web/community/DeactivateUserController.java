package gs.web.community;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.ISubscriptionDao;
import gs.data.community.Subscription;
import gs.web.util.ReadWriteController;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class DeactivateUserController implements ReadWriteController {

    /** Spring BEAN id */
    public static final String BEAN_ID = "/community/deactivateUser.page";
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    protected static final Logger _log = Logger.getLogger(gs.web.community.DeactivateUserController.class);
    public static final String SECRET_NUMBER = "5582";

    public ModelAndView handleRequest(HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        response.setContentType("text/plain");
        String rval = "false";
        try {
            // request must have secret number for basic security
            if (!StringUtils.equals(request.getParameter("secret"), SECRET_NUMBER)) {
                _log.warn("Error deactivating user: incorrect secret number provided");
            // request must have user id
            } else if (request.getParameter("id") == null) {
                _log.warn("Error deactivating user: no parameter \"id\" provided");
            } else {
                int id = Integer.valueOf(request.getParameter("id"));
                User user = _userDao.findUserFromId(id);
                // user must be a community user
                if (user.getUserProfile() == null) {
                    _log.warn("Error deactivating user " + id + ": Null user profile");
                } else {
                    user.getUserProfile().setActive(false);
                    _userDao.saveUser(user);
                    List<Subscription> subs = _subscriptionDao.getUserSubscriptions(user);
                    if (subs != null) {
                        for (Subscription sub: subs) {
                            _subscriptionDao.removeSubscription(sub.getId());
                        }
                    }
                    rval = "true";
                }
            }
        } catch (ObjectRetrievalFailureException orfe) {
            // user doesn't exist
            _log.warn("Error deactivating user " + request.getParameter("id") + ": no user found");
        } catch (Exception e) {
            _log.warn("Error deactivating user " + request.getParameter("id"), e);
            // nothing
        }
        PrintWriter out = response.getWriter();
        out.print(rval);
        out.flush();
        return null;
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
}
