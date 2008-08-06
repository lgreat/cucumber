package gs.web.util;

import gs.data.community.User;
import gs.data.community.Subscription;
import gs.web.tracking.OmnitureSuccessEvent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: jnorton
 * Date: Jul 30, 2008
 * Time: 2:00:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class NewSubscriberDetector {

    public static void notifyOmnitureWhenNewNewsLetterSubscriber(User user,HttpServletRequest request, HttpServletResponse response){
        Set<Subscription> subs = user.getSubscriptions();

        if (!userHasNewsLetterSubscriptions(subs)) {
            //OmnitureSuccessEvent.createSuccessEvent(OmnitureSuccessEvent.SuccessEvent.NewNewsLetterSubscriber, request, response);
            OmnitureSuccessEvent ose = new OmnitureSuccessEvent(request, response);
            ose.add(OmnitureSuccessEvent.SuccessEvent.NewNewsLetterSubscriber);
        }
    }
    public static void notifyOmnitureWhenNewNewsLetterSubscriber(User user, OmnitureSuccessEvent omnitureSuccessEvent){
        Set<Subscription> subs = user.getSubscriptions();

        if (!userHasNewsLetterSubscriptions(subs)) {
            omnitureSuccessEvent.add(OmnitureSuccessEvent.SuccessEvent.NewNewsLetterSubscriber);
        }
    }

    static boolean userHasNewsLetterSubscriptions(Set<Subscription> subs) {
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
