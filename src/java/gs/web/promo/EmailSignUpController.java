package gs.web.promo;

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

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> model = new HashMap<String, Object>();

        model.put("userIsAlreadySignedUp", getUserIsAlreadySignedUp(request, response));

        return new ModelAndView(_viewName, model);
    }

    public static boolean getUserIsAlreadySignedUp(HttpServletRequest request, HttpServletResponse response) {
        boolean alreadySignedUp = true;
        SessionContext sc = SessionContextUtil.getSessionContext(request);
        User u = sc.getUser();
        if (u != null) {
            Set<Subscription> subs = u.getSubscriptions();
            if (subs != null && subs.size() > 0) {
                for (Subscription sub : subs) {
                    String prod = sub.getProduct().getName();
                    if (prod != null && prod.equals(SubscriptionProduct.PARENT_ADVISOR.getName())) {
                        alreadySignedUp = true;
                        break;
                    }
                }
            }
        }

        return alreadySignedUp;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
}
