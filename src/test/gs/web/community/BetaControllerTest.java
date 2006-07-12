package gs.web.community;

import gs.data.community.*;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.util.MockJavaMailSender;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.mail.internet.MimeMessage;
import java.util.List;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class BetaControllerTest extends BaseControllerTestCase {

    private BetaController _controller;
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = (BetaController)getApplicationContext().getBean(BetaController.BEAN_ID);
        MockJavaMailSender _sender = new MockJavaMailSender();
        _sender.setHost("mail.greatschools.net");
        _controller.setMailSender(_sender);
        _userDao = (IUserDao)getApplicationContext().getBean(IUserDao.BEAN_ID);
        _subscriptionDao = (ISubscriptionDao)getApplicationContext().getBean(ISubscriptionDao.BEAN_ID);
    }

    public void testFormBackingObject() throws Exception {
        String email = "blah@blahdeeblah.com";
        getRequest().setParameter("email", email);
        getRequest().setParameter("state", "ca");
        Object o = _controller.formBackingObject(getRequest());
        assertTrue(o instanceof BetaSignupCommand);
        BetaSignupCommand command = (BetaSignupCommand)o;
        assertEquals(email, command.getEmail());
        assertEquals(State.CA, command.getState());
    }

    public void testSubscribe() throws Exception {

        BetaSignupCommand command = new BetaSignupCommand();
        String testEmail = "foo@barfolblahhhhhhh.com";
        command.setEmail(testEmail);
        command.setState(State.CA);

        assertNull(_userDao.findUserFromEmailIfExists(testEmail));

        ModelAndView mAndV = _controller.onSubmit(command);
        assertEquals("community/betaThankyou", mAndV.getViewName());

        User u = _userDao.findUserFromEmailIfExists(testEmail);
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
        assertNull(_userDao.findUserFromEmailIfExists(testEmail));
        _controller.onSubmit(command);
        User u = _userDao.findUserFromEmailIfExists(testEmail);
        assertEquals(testEmail, u.getEmail());
        List subs = _subscriptionDao.getUserSubscriptions(u, SubscriptionProduct.BETA_GROUP);
        Subscription sub = (Subscription)subs.get(0);

        assertEquals(SubscriptionProduct.BETA_GROUP, sub.getProduct());
        assertEquals(State.AK, sub.getState());
        _subscriptionDao.removeSubscription(sub.getId());
        _userDao.removeUser(u.getId());
    }

    public void testCreateMessage() throws Exception {
        JavaMailSender sender = new JavaMailSenderImpl();
        BetaSignupCommand bsc = (BetaSignupCommand)_controller.formBackingObject(getRequest());
        bsc.setEmail("foo@bar.com");
        bsc.setState(State.NY);
        MimeMessage mm = _controller.createMessage(sender.createMimeMessage(), bsc);
        assertNotNull(mm.getContent());
    }
}
