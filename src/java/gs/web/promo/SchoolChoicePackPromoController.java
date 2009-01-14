package gs.web.promo;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import gs.data.community.*;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.web.util.ReadWriteController;
import gs.web.util.NewSubscriberDetector;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContextUtil;
import gs.web.tracking.OmnitureTracking;

/**
 * Created by chriskimm@greatschools.net
 */
public class SchoolChoicePackPromoController extends AbstractController implements ReadWriteController {

    public static final String BEAN_ID = "/promo/schoolChoicePackPromo.page";
    public static final String EMAIL_PARAM = "email";
    public static final String LEVELS_PARAM = "levels";
    public static final String PAGE_NAME = "pageName";
    public static final String SCHOOL_CHOICE_PACK_TRIGGER_KEY = "chooser_pack_trigger";

    private ISubscriptionDao _subscriptionDao;
    private IUserDao _userDao;
    private ExactTargetAPI _exactTargetAPI;
    private static final Logger _log = Logger.getLogger(SchoolChoicePackPromoController.class);

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String email = request.getParameter(EMAIL_PARAM);
        String level_val= request.getParameter(LEVELS_PARAM);
        String pageName = request.getParameter(PAGE_NAME);
        if (level_val != null) {

            String[] levels = level_val.split(",");

            User user = _userDao.findUserFromEmailIfExists(email);

            // If the user does not yet exist, add her to list_member
            if (user == null) {
                user = new User();
                user.setEmail(email);
                user.setWelcomeMessageStatus(WelcomeMessageStatus.NEVER_SEND);
                _userDao.saveUser(user);
            }

            OmnitureTracking omnitureTracking = new OmnitureTracking(request, response);

            NewSubscriberDetector.notifyOmnitureWhenNewNewsLetterSubscriber(user, omnitureTracking);
            omnitureTracking.addSuccessEvent(OmnitureTracking.SuccessEvent.ChoicePackRequest);
            omnitureTracking.addEvar(new OmnitureTracking.Evar(OmnitureTracking.EvarNumber.CrossPromotion, "Chooser_pack_" + pageName));

            // add PA subscription
            List<Subscription> subs = new ArrayList<Subscription>();
            Subscription communityNewsletterSubscription = new Subscription();
            communityNewsletterSubscription.setUser(user);
            communityNewsletterSubscription.setProduct(SubscriptionProduct.PARENT_ADVISOR);
            communityNewsletterSubscription.setState(SessionContextUtil.getSessionContext(request).getStateOrDefault());
            subs.add(communityNewsletterSubscription);

            _subscriptionDao.addNewsletterSubscriptions(user, subs);

            // add each promo level as a new subscription
            for (String level : levels) {
                SubscriptionProduct prod = SubscriptionProduct.getSubscriptionProduct("chooserpack_" + level);
                if (prod != null) {
                    if (!_subscriptionDao.isUserSubscribed(user, prod, null)) {
                        Subscription sub = new Subscription();
                        sub.setProduct(prod);
                        sub.setUser(user);
                        _subscriptionDao.saveSubscription(sub);
                    } else {
                        StringBuilder sb = new StringBuilder("User is already subscribed: ");
                        sb.append(user.toString());
                        sb.append(" prod: ");
                        sb.append(prod.getName());
                        _log.info(sb.toString());
                    }
                } else {
                    _log.warn ("Could not find subscription product for: " + level);
                }
            }
            PageHelper.setMemberCookie(request, response, user);
            triggerPromoPackEmail(user, levels);

            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.println("{\"memid\":\"" + String.valueOf(user.getId()) + "\"}");
        }


        return null;
    }

    /**
     * This method triggers an ExactTarget SOAP api call that sends out the
     * promo pack email.
     * @param user - a valid user
     * @param levels - the grade levels - {p,e,m,h} 
     */
    void triggerPromoPackEmail(User user, String[] levels) {
        Map<String, String> attributes = new HashMap<String, String>();
        for (String level : levels) {
            attributes.put(level,  "1");
        }
        _exactTargetAPI.sendTriggeredEmail(SCHOOL_CHOICE_PACK_TRIGGER_KEY, user, attributes);
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public void setExactTargetAPI(ExactTargetAPI etAPI) {
        _exactTargetAPI = etAPI;
    }
}
