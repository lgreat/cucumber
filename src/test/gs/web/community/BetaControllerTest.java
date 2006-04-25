package gs.web.community;

import gs.web.BaseControllerTestCase;
import gs.web.util.MockJavaMailSender;
import gs.data.community.IUserDao;
import gs.data.community.ISubscriptionDao;
import gs.data.community.User;
import gs.data.community.SubscriptionProduct;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.Validator;

import javax.servlet.http.HttpServletRequest;

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
        _controller.setCommandClass(BetaEmailCommand.class);
        _controller.setCommandName("betaEmailCommand");
        _controller.setValidators(new Validator[] {
                (EmailValidator)appContext.getBean("emailValidator"),
                (BetaSubNotExistsValidator)appContext.getBean("betaSubNotExistsValidator")});

        _userDao = (IUserDao)getApplicationContext().getBean(IUserDao.BEAN_ID);
        _controller.setUserDao(_userDao);
        _subscriptionDao = (ISubscriptionDao)getApplicationContext().getBean(ISubscriptionDao.BEAN_ID);
        _controller.setSubscriptionDao(_subscriptionDao);

    }

    public void testSubscribe() throws Exception {
        BetaEmailCommand command = new BetaEmailCommand();
        String testEmail = "foo@bar.com";
        command.setEmail(testEmail);

        assertNull(_userDao.getUserFromEmailIfExists(testEmail));

        getRequest().setMethod("POST");
        getRequest().setParameter("email", testEmail);

        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());
        //ModelAndView mAndV = _controller.onSubmit(command);
        assertEquals("/community/betaThankyou", mAndV.getViewName());

        User u = _userDao.getUserFromEmailIfExists(testEmail);
        assertNotNull(u);

        System.out.println ("fasd: " +
                _subscriptionDao.getUserSubscriptions(u, SubscriptionProduct.BETA_GROUP));

        mAndV = _controller.onSubmit(command);
        //assertEquals("/community/beta", mAndV.getViewName());

        System.out.println ("removing: " + u.getId());
        _userDao.removeUser(u.getId());
    }
}
