package gs.web.util;

import gs.data.community.User;
import gs.data.community.Subscription;
import gs.web.tracking.OmnitureTracking;
import gs.web.tracking.CookieBasedOmnitureTracking;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;
import java.util.Collection;

/**
 * Adds an omniture success event if the user had no previous newsletter subscriptions.
 */
public class NewSubscriberDetector {

    public static void notifyOmnitureWhenNewNewsLetterSubscriber(User user,HttpServletRequest request, HttpServletResponse response){
        Set<Subscription> subs = user.getSubscriptions();

        if (!userHasNewsLetterSubscriptions(subs)) {
            OmnitureTracking ot = new CookieBasedOmnitureTracking(request, response);
            ot.addSuccessEvent(OmnitureTracking.SuccessEvent.NewNewsLetterSubscriber);
        }
    }

    public static void notifyOmnitureWhenNewNewsLetterSubscriber(User user, OmnitureTracking ot){
        Set<Subscription> subs = user.getSubscriptions();

        if (!userHasNewsLetterSubscriptions(subs)) {
            ot.addSuccessEvent(OmnitureTracking.SuccessEvent.NewNewsLetterSubscriber);
        }
    }

    public static boolean userHasNewsLetterSubscriptions(Collection<Subscription> subs) {
        boolean found = false;
        if (subs != null){
            for (Subscription subscription: subs){
                if (subscription.getProduct().isNewsletter()){
                    found = true;
                    break;
                }
            }
        }
        return found;
    }
}
