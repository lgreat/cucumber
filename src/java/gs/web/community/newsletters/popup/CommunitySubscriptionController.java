package gs.web.community.newsletters.popup;

import gs.data.community.*;
import gs.web.util.ReadWriteController;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

        try {
            if (user == null) {
                // create user
                user = new User();
                user.setEmail(email);
                _userDao.saveUser(user);
            }

            Subscription subscription = new Subscription(user, SubscriptionProduct.COMMUNITY, sessionContext.getStateOrDefault());
            _subscriptionDao.saveSubscription(subscription);
        } catch (Exception e) {
            _log.error("Error trying to save user or subscription", e);
            modelAndView.getModel().put(ERROR, e);
        }

        return modelAndView;
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
