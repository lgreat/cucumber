package gs.web.community;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.log4j.Logger;
import gs.data.community.*;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class BetaUnsubscribeController extends SimpleFormController {

    public static final String BEAN_ID = "/community/betaUnsubscribe.page";
    private static final Logger _log = Logger.getLogger(BetaUnsubscribeController.class);
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;

    public ModelAndView onSubmit(Object command) {
        BetaEmailCommand bsc = (BetaEmailCommand)command;
        removeFromBetaGroup(bsc.getEmail());
        return new ModelAndView(getSuccessView());
    }

    /**
     * This method assumes that beta subscription exists for the provided
     * email.  All validation of the email and the subscription are
     * in the validator(s) see pages-servlet for config.
     * @param email
     */
    private void removeFromBetaGroup(String email) {
        System.out.println ("removing: " + email);
        User user = _userDao.getUserFromEmailIfExists(email);
        // Despite what I said in the comment, check for nulls just
        // to be safe.
        if (user != null) {
            System.out.println ("remove 1");
            Subscription sub = user.findSubscription(SubscriptionProduct.BETA_GROUP);
            if (sub != null) {
                System.out.println ("remove 2");
                _subscriptionDao.removeSubscription(sub.getId());
            }
            //_subscriptionDao.getUserSubscriptions(user, SubscriptionProduct.BETA_GROUP);
            //_subscriptionDao.
        }
    }

    /**
     * Spring setter.
     * @param _userDao
     */
    public void setUserDao(IUserDao _userDao) {
        this._userDao = _userDao;
    }

    /**
     * Spring setter
     * @param _subscriptionDao
     */
    public void setSubscriptionDao(ISubscriptionDao _subscriptionDao) {
        this._subscriptionDao = _subscriptionDao;
    }
}