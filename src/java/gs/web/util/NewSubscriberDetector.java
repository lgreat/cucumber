package gs.web.util;

import gs.data.community.User;
import gs.data.community.Subscription;
import gs.web.util.context.SubCookie;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
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
        boolean found = false;

        if (subs != null){
            for (Subscription subscription: subs){
                if (subscription.getProduct().isNewsletter()){
                    found = true;
                    break;
                }
            }
        }

        if (!found) {
            SubCookie subCookie = new SubCookie(request, response);
            subCookie.setProperty("events","event11;");
        }

    }
}
