package gs.web.community;

import gs.data.community.*;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.util.MockJavaMailSender;
import gs.web.util.validator.EmailValidator;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class BetaControllerTest extends BaseControllerTestCase {

    private BetaController _controller;
    private MockJavaMailSender _sender;
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new BetaController();
        ApplicationContext appContext = getApplicationContext();
        _sender = new MockJavaMailSender();
        _controller.setMailSender(_sender);
        _controller.setSuccessView("/community/betaThankyou");
        _controller.setFormView("/community/beta");
        _controller.setCommandClass(BetaSignupCommand.class);
        _controller.setCommandName("betaSignupCommand");
        _controller.setValidators(new Validator[] {
                (EmailValidator)appContext.getBean("emailValidator"),
                (BetaSubNotExistsValidator)appContext.getBean("betaSubNotExistsValidator")});

        _userDao = (IUserDao)getApplicationContext().getBean(IUserDao.BEAN_ID);
        _controller.setUserDao(_userDao);
        _subscriptionDao = (ISubscriptionDao)getApplicationContext().getBean(ISubscriptionDao.BEAN_ID);
        _controller.setSubscriptionDao(_subscriptionDao);

    }

    public void testSubscribe() throws Exception {

        BetaSignupCommand command = new BetaSignupCommand();
        String testEmail = "foo@barfolblahhhhhhh.com";
        command.setEmail(testEmail);
        command.setState(State.CA);

        assertNull(_userDao.getUserFromEmailIfExists(testEmail));

        ModelAndView mAndV = _controller.onSubmit(command);
        assertEquals("/community/betaThankyou", mAndV.getViewName());

        User u = _userDao.getUserFromEmailIfExists(testEmail);
        assertEquals(testEmail, u.getEmail());

        List subs = _subscriptionDao.getUserSubscriptions(u, SubscriptionProduct.BETA_GROUP);
        Subscription sub = (Subscription)subs.get(0);
        assertEquals(SubscriptionProduct.BETA_GROUP, sub.getProduct());
        assertEquals(State.CA, sub.getState());

        _subscriptionDao.removeSubscription(sub.getId());
        _userDao.removeUser(u.getId());
    }

    public void testSubscribeStateAware() throws Exception {

        BetaSignupCommand command = new BetaSignupCommand();
        String testEmail = "foo@joestateaware.com";
        command.setEmail(testEmail);
        command.setState(State.AK);
        assertNull(_userDao.getUserFromEmailIfExists(testEmail));
        _controller.onSubmit(command);
        User u = _userDao.getUserFromEmailIfExists(testEmail);
        assertEquals(testEmail, u.getEmail());
        List subs = _subscriptionDao.getUserSubscriptions(u, SubscriptionProduct.BETA_GROUP);
        Subscription sub = (Subscription)subs.get(0);

        assertEquals(SubscriptionProduct.BETA_GROUP, sub.getProduct());
        assertEquals(State.AK, sub.getState());
        _subscriptionDao.removeSubscription(sub.getId());
        _userDao.removeUser(u.getId());
    }
}
