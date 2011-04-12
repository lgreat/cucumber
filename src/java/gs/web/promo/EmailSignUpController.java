package gs.web.promo;

import gs.data.community.ISubscriptionDao;
import gs.data.community.Subscription;
import gs.data.community.SubscriptionProduct;
import gs.data.community.User;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EmailSignUpController extends AbstractController {
    private static final Logger _log = Logger.getLogger(EmailSignUpController.class);

    public static final String BEAN_ID = "/promo/emailSignUp.module";

    private String _viewName;
    private ISubscriptionDao _subscriptionDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> model = new HashMap<String, Object>();

        model.put("userIsAlreadySignedUp", getUserIsAlreadySignedUp(request));

        return new ModelAndView(_viewName, model);
    }

    public boolean getUserIsAlreadySignedUp(HttpServletRequest request) {
        SessionContext sc = SessionContextUtil.getSessionContext(request);
        User user = sc.getUser();
        if (user != null) {
            return _subscriptionDao.isUserSubscribed(user, SubscriptionProduct.PARENT_ADVISOR, null);
        }

        return false;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }
}
