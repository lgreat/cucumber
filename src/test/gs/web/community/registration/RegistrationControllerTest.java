package gs.web.community.registration;

import gs.data.community.ISubscriptionDao;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.web.BaseControllerTestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import java.security.NoSuchAlgorithmException;

/**
 * Created by IntelliJ IDEA.
 * User: UrbanaSoft
 * Date: Jun 15, 2006
 * Time: 2:05:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class RegistrationControllerTest extends BaseControllerTestCase {
    private RegistrationController _controller;

    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;

    protected void setUp() throws Exception {
        super.setUp();
        ApplicationContext appContext = getApplicationContext();
        _controller = (RegistrationController) appContext.getBean(RegistrationController.BEAN_ID);

        _userDao = (IUserDao)getApplicationContext().getBean(IUserDao.BEAN_ID);
        //_controller.setUserDao(_userDao);
        _subscriptionDao = (ISubscriptionDao)getApplicationContext().getBean(ISubscriptionDao.BEAN_ID);
        //_controller.setSubscriptionDao(_subscriptionDao);
    }

    public void testRegistration() throws Exception {
        UserCommand userCommand = new UserCommand();
        BindException errors = new BindException(userCommand, "");
        String email = "testRegistration@RegistrationControllerTest.com";
        String password = "foobar";
        userCommand.setEmail(email);
        userCommand.setPassword(password);
        userCommand.setConfirmPassword(password);

        assertNull("Fake user already exists??", _userDao.findUserFromEmailIfExists(email));

        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), userCommand, errors);

        User u = _userDao.findUserFromEmailIfExists(email);
        assertNotNull("User not inserted", u);
        try {
            assertEquals("Not getting expected success view",
                    "/community/registration/registrationSuccess", mAndV.getViewName());
            assertEquals(email, u.getEmail());
            assertTrue("Password failing compare", u.matchesPassword(password));
        } finally {
            _userDao.removeUser(u.getId());
        }
    }

    public void testExistingUser() throws NoSuchAlgorithmException {
        User user = _userDao.findUserFromId(1);
        // existing DB must have user with id==1
        assertNotNull("Cannot find existing user with id=1", user);
        String email = user.getEmail();

        UserCommand userCommand = new UserCommand();
        BindException errors = new BindException(userCommand, "");
        String password = "foobar";
        userCommand.getUser().setEmail(email);

        userCommand.setPassword(password);
        userCommand.setConfirmPassword(password);

        try {
            _controller.onSubmit(getRequest(), getResponse(), userCommand, errors);
            fail("Didn't get expected exception on onSubmit");
        } catch (Exception ex) {
            // tell session not to try to commit since a Hibernate error has occurred
            ThreadLocalTransactionManager.setRollbackOnly();
        }
    }
}
