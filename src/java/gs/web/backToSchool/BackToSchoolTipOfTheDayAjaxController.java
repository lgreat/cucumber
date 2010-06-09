package gs.web.backToSchool;

import gs.data.community.ISubscriptionDao;
import gs.data.community.Subscription;
import gs.data.community.SubscriptionProduct;
import gs.data.community.User;
import gs.data.state.State;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.ReadWriteController;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;


@Controller
@RequestMapping("/backToSchool/backToSchoolTipOfTheDayAjax.page")
public class BackToSchoolTipOfTheDayAjaxController implements ReadWriteAnnotationController {
    protected final Log _log = LogFactory.getLog(getClass());

    ISubscriptionDao _subscriptionDao;

    public static final String BEAN_ID = "/backToSchool/backToSchoolTipOfTheDayAjax.page";

    @RequestMapping(method= RequestMethod.POST)
    public void addBtsTipOfTheDay(@RequestParam("btsTipOfTheDay") String tipOfTheDay, HttpServletRequest request, HttpServletResponse response) {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);

        boolean added = false;
        
        if (sessionContext == null) {
            _log.warn("No SessionContext found in request.");
            return;
        }

        User user = sessionContext.getUser();

        if (user != null) {
            added = addSubscriptionToUser(tipOfTheDay, user);
        }

        response.setContentType("application/json");
        try {
            PrintWriter writer = response.getWriter();
            writer.print(added? "1":"0");
            writer.flush();
        } catch (IOException e) {
            //lost all hope
        }
    }

    public boolean addSubscriptionToUser(String tipOfTheDay, User user, State state) {
        boolean added = false;
        
        SubscriptionProduct product = SubscriptionProduct.getSubscriptionProduct(tipOfTheDay);
        if (product != null) {
            Subscription s = new Subscription(user, product, state);
            _subscriptionDao.saveSubscription(s);
            added = true;
        }
        return added;
    }

    public boolean addSubscriptionToUser(String tipOfTheDay, User user) {
    
        State state = user.getState();
        if(user.getUserProfile() != null && user.getUserProfile().getState() != null){
            state = user.getUserProfile().getState();
        }
        if (state == null) {
            state = State.CA;
        }

        return addSubscriptionToUser(tipOfTheDay, user, state);
    }

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }
}
