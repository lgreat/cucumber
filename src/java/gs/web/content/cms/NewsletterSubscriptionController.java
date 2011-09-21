package gs.web.content.cms;

import gs.data.community.*;
import gs.web.util.ReadWriteController;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NewsletterSubscriptionController extends SimpleFormController implements ReadWriteController {
    private IUserDao _userDao;
    protected final Log _log = LogFactory.getLog(getClass());
    private ISubscriptionDao _subscriptionDao;

    public ModelAndView onSubmit(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object command,
                                 BindException errors) throws Exception {

        ModelAndView mAndV = new ModelAndView();
        NewsletterSubscriptionCommand nlSubCmd = (NewsletterSubscriptionCommand) command;

        String email = StringEscapeUtils.escapeHtml(nlSubCmd.getEmail());
        User user = getUserDao().findUserFromEmailIfExists(email);

        if (user != null) {
            boolean isSubscribedToParentAdvisor = false;
            boolean isSubscribedToSponsorOptIn = false;
            Set<Subscription> userSubs = user.getSubscriptions();

            if (userSubs != null) {
                for (Subscription s : userSubs) {
                    if (SubscriptionProduct.PARENT_ADVISOR.equals(s.getProduct())) {
                        isSubscribedToParentAdvisor = true;
                    } else if (SubscriptionProduct.SPONSOR_OPT_IN.equals(s.getProduct())) {
                        isSubscribedToSponsorOptIn = true;
                    }
                }
            }

            if (!isSubscribedToParentAdvisor || (!isSubscribedToSponsorOptIn && nlSubCmd.isPartnerNewsletter())) {
                List subscriptions = new ArrayList();

                if (!isSubscribedToParentAdvisor) {
                    Subscription sub = new Subscription();
                    sub.setUser(user);
                    sub.setProduct(SubscriptionProduct.PARENT_ADVISOR);
                    subscriptions.add(sub);
                }
                if (nlSubCmd.isPartnerNewsletter() && !isSubscribedToSponsorOptIn) {
                    Subscription sub = new Subscription();
                    sub.setUser(user);
                    sub.setProduct(SubscriptionProduct.SPONSOR_OPT_IN);
                    subscriptions.add(sub);
                }

                getSubscriptionDao().addNewsletterSubscriptions(user, subscriptions);
            }
        }
        if(nlSubCmd.isAjaxRequest()){
          return null;
        }
        return mAndV;
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