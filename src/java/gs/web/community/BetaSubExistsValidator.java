package gs.web.community;

import org.springframework.validation.Validator;
import org.springframework.validation.Errors;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.ISubscriptionDao;
import gs.data.community.SubscriptionProduct;

/**
 * This validator checks to make sure that the command class -
 * BetaSignupCommand contains an email that is currently
 * subscribed to SubscriptionProduct.BETA_GROUP.
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class BetaSubExistsValidator implements Validator {

    public static final String BEAN_ID = "betaSubExistsValidator";
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;

    public boolean supports(Class aClass) {
        return aClass.equals(BetaSignupCommand.class);
    }

    public void validate(Object object, Errors errors) {
        BetaSignupCommand command = (BetaSignupCommand)object;
        User user = _userDao.findUserFromEmailIfExists(command.getEmail());
        if (user != null) {
            if (_subscriptionDao.getUserSubscriptions(user, SubscriptionProduct.BETA_GROUP) == null) {
                errors.rejectValue("email", "notsub", "You are not subscribed to the beta group");
            }
        } else {
            errors.rejectValue("email", "notmember", "We have no records with that email address");
        }
    }

    public void setUserDao(IUserDao _userDao) {
        this._userDao = _userDao;
    }

    public void setSubscriptionDao(ISubscriptionDao _subscriptionDao) {
        this._subscriptionDao = _subscriptionDao;
    }
}
