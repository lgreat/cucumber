package gs.web.community;

import gs.data.community.*;
import gs.data.state.State;
import gs.data.admin.IPropertyDao;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.web.BaseControllerTestCase;
import gs.web.util.MockJavaMailSender;
import gs.web.util.validator.EmailValidator;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.Validator;
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
    private MockJavaMailSender _sender;
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private IPropertyDao _propertyDao;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = (BetaController)getApplicationContext().getBean(BetaController.BEAN_ID);
        ApplicationContext appContext = getApplicationContext();
        _sender = new MockJavaMailSender();
        _controller.setMailSender(_sender);
        _userDao = (IUserDao)getApplicationContext().getBean(IUserDao.BEAN_ID);
        _subscriptionDao = (ISubscriptionDao)getApplicationContext().getBean(ISubscriptionDao.BEAN_ID);
        _propertyDao = (IPropertyDao)getApplicationContext().getBean(IPropertyDao.BEAN_ID);
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
        //ThreadLocalTransactionManager.commitOrRollback();
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

    public void testGetNextShutterflyCode() {
        if (_propertyDao.getProperty(IPropertyDao.SHUTTERFLY_CODE_INDEX) != null) {
            _propertyDao.removeProperty(IPropertyDao.SHUTTERFLY_CODE_INDEX);
        }

        assertEquals("GRS1-F7S2-YUA3-4508TZ", _controller.getShutterflyCode());
        assertEquals("GRS1-2CGF-847A-W5YFVK", _controller.getShutterflyCode());
        assertEquals("GRS1-6EWA-NMKH-D01571", _controller.getShutterflyCode());

        // set the index to the last available code
        _propertyDao.setProperty(IPropertyDao.SHUTTERFLY_CODE_INDEX, String.valueOf(20000));
        assertEquals("GRS1-F1TB-8GA7-V4FAXJ", _controller.getShutterflyCode());

        assertNull(_controller.getShutterflyCode());

        // clean up
        _propertyDao.removeProperty(IPropertyDao.SHUTTERFLY_CODE_INDEX);
    }

    public void testCreateMessage() throws Exception {
        System.out.println("debug 1");
        JavaMailSender sender = new JavaMailSenderImpl();
        System.out.println("debug 2");
        MimeMessage mm = _controller.createMessage(sender.createMimeMessage(), "foo@bar.com");
        System.out.println ("content: " + mm.getContent());
    }
}
