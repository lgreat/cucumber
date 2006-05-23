package gs.web.community;

import org.springframework.validation.Validator;
import org.springframework.validation.Errors;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.ISubscriptionDao;
import gs.data.community.SubscriptionProduct;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class BetaSubNotExistsValidator implements Validator {

    public static final String BEAN_ID = "betaSubNotExistsValidator";
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;

    public boolean supports(Class aClass) {
        return aClass.equals(BetaSignupCommand.class);
    }

    public void validate(Object object, Errors errors) {
        BetaSignupCommand command = (BetaSignupCommand)object;
        User user = _userDao.getUserFromEmailIfExists(command.getEmail());
        if (user != null) {
            if (_subscriptionDao.getUserSubscriptions(user, SubscriptionProduct.BETA_GROUP) != null) {
                StringBuffer buffer = new StringBuffer();
                buffer.append(command.getEmail());
                buffer.append(" is already a member of the beta group.");
                errors.rejectValue("email", "exists", buffer.toString());
            }
        }
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao _userDao) {
        this._userDao = _userDao;
    }

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao _subscriptionDao) {
        this._subscriptionDao = _subscriptionDao;
    }
}
