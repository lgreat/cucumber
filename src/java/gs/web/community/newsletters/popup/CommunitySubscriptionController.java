package gs.web.community.newsletters.popup;

import gs.data.community.*;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.web.util.ReadWriteController;
import gs.web.util.NewSubscriberDetector;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.io.PrintWriter;

public class CommunitySubscriptionController extends AbstractController implements ReadWriteController {
    private Log _log = LogFactory.getLog(CommunitySubscriptionController.class);

    public static final String EMAIL_PARAM = "email";
    public static final String ERROR = "error";

    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private String _viewName;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        ModelAndView modelAndView = new ModelAndView(_viewName);

        String email = request.getParameter(EMAIL_PARAM);
        User user = _userDao.findUserFromEmailIfExists(email);

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        String result = "success";

        try {
            if (user == null) {
                // create user
                user = new User();
                user.setEmail(email);
                _userDao.saveUser(user);
                // Because of hibernate caching, it's possible for a list_active record
                // (with list_member id) to be commited before the list_member record is
                // committed. Adding this commitOrRollback prevents this.
                ThreadLocalTransactionManager.commitOrRollback();
            }

            Subscription subscription = new Subscription(user, SubscriptionProduct.COMMUNITY, sessionContext.getStateOrDefault());
            if (!NewSubscriberDetector.userHasNewsLetterSubscriptions(user.getSubscriptions()))  {
                result += ",newSubscriber";
            }
            _subscriptionDao.addNewsletterSubscriptions(user, Arrays.asList(new Subscription[]{subscription}));
            out.print(result);
        } catch (Exception e) {
            _log.error("Error trying to save user or subscription", e);
            out.print("failure");
        }

        out.flush();
        return null;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
}
