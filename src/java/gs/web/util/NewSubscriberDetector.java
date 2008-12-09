package gs.web.util;

import gs.data.community.User;
import gs.data.community.Subscription;
import gs.data.util.SubscriptionUtil;
import gs.web.tracking.OmnitureTracking;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * Adds an omniture success event if the user had no previous newsletter subscriptions.
 */
public class NewSubscriberDetector {

    public static void notifyOmnitureWhenNewNewsLetterSubscriber(User user,HttpServletRequest request, HttpServletResponse response){
        Set<Subscription> subs = user.getSubscriptions();

        if (!SubscriptionUtil.userHasNewsLetterSubscriptions(subs)) {
            OmnitureTracking ot = new OmnitureTracking(request, response);
            ot.addSuccessEvent(OmnitureTracking.SuccessEvent.NewNewsLetterSubscriber);
        }
    }

    public static void notifyOmnitureWhenNewNewsLetterSubscriber(User user, OmnitureTracking ot){
        Set<Subscription> subs = user.getSubscriptions();

        if (!SubscriptionUtil.userHasNewsLetterSubscriptions(subs)) {
            ot.addSuccessEvent(OmnitureTracking.SuccessEvent.NewNewsLetterSubscriber);
        }
    }
}
