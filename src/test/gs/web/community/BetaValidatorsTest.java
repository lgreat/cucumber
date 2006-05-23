package gs.web.community;

import gs.web.BaseTestCase;
import gs.data.community.*;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class BetaValidatorsTest extends BaseTestCase {

    private BetaSubExistsValidator _validator;
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;

    public void setUp() throws Exception {
        _validator = new BetaSubExistsValidator();
        _userDao = (IUserDao)getApplicationContext().getBean(IUserDao.BEAN_ID);
        _validator.setUserDao(_userDao);
        _subscriptionDao = (ISubscriptionDao)getApplicationContext().getBean(ISubscriptionDao.BEAN_ID);
        _validator.setSubscriptionDao(_subscriptionDao);
    }

    public void testBetaSubExistsValidator() {
        assertTrue(_validator.supports(BetaSignupCommand.class));
        assertFalse(_validator.supports(String.class));

        /*
        BetaSignupCommand command = new BetaSignupCommand();
        Errors errors = new BindException(command, "Test Errors");
        command.setEmail("foozxczxczc@baasdfasdfr.com");
        _validator.validate(command, errors);
        assertTrue(errors.hasErrors());
        assertEquals("notmember", errors.getFieldError("email").getCode());


        User user = new User();
        user.setEmail("jimbo_jehosephat@greatschools.net");
        _userDao.saveUser(user);
        Subscription sub = new Subscription();
        sub.setProduct(SubscriptionProduct.BETA_GROUP);
        sub.setUser(user);
        _subscriptionDao.saveSubscription(sub);

        command.setEmail("jimbo_jehosephat@greatschools.net");
        errors = new BindException(command, "Test Errors");
        _validator.validate(command, errors);
        */
        /*
        List l = errors.getAllErrors();
        for (int i= 0; i < l.size(); i++) {
            System.out.println ("error: " + l.get(i).toString());
        }
        */
        //_subscriptionDao.removeSubscription(sub.getId());
    }
}
