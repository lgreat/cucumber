package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.util.MockJavaMailSender;
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
    private MockJavaMailSender _mailSender;

    protected void setUp() throws Exception {
        super.setUp();
        ApplicationContext appContext = getApplicationContext();
        _controller = (RegistrationController) appContext.getBean(RegistrationController.BEAN_ID);
        _mailSender = new MockJavaMailSender();
        // have to set host else the mock mail sender will throw an exception
        // actual value is irrelevant
        _mailSender.setHost("greatschools.net");
        _controller.setMailSender(_mailSender);
        _userDao = (IUserDao)appContext.getBean(IUserDao.BEAN_ID);
    }

    /**
     * Test successful registration with a new user
     * @throws Exception
     */
    public void testRegistration() throws Exception {
        UserCommand userCommand = new UserCommand();
        BindException errors = new BindException(userCommand, "");
        String email = "testRegistration@RegistrationControllerTest.com";
        String password = "foobar";
        userCommand.setEmail(email);
        userCommand.setPassword(password);
        userCommand.setConfirmPassword(password);
        userCommand.setState(State.CA);

        assertNull("Fake user already exists??", _userDao.findUserFromEmailIfExists(email));

        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), userCommand, errors);

        User u = _userDao.findUserFromEmailIfExists(email);
        assertNotNull("User not inserted", u);
        try {
            assertEquals("Not getting expected success view",
                    "/community/registration/registrationSuccess", mAndV.getViewName());
            assertEquals(email, u.getEmail());
            assertTrue("Password failing compare", u.matchesPassword(password));
            assertNotNull("User wasn't given a user profile", u.getUserProfile());
            assertEquals(State.CA, u.getUserProfile().getState());
        } finally {
            _userDao.removeUser(u.getId());
        }
    }

    /**
     * Test successful registration with an existing user
     * @throws NoSuchAlgorithmException
     */
    public void testExistingUser() throws NoSuchAlgorithmException {
        User user = new User();
        user.setEmail("testExistingUser@greatschools.net");
        _userDao.saveUser(user);
        Integer userId = user.getId();
        try {
            user = _userDao.findUserFromId(user.getId().intValue());
            assertTrue(user.isPasswordEmpty());
            String email = user.getEmail();

            UserCommand userCommand = new UserCommand();
            BindException errors = new BindException(userCommand, "");
            String password = "foobar";
            userCommand.getUser().setEmail(email);

            userCommand.setPassword(password);
            userCommand.setConfirmPassword(password);

            try {
                _controller.onSubmit(getRequest(), getResponse(), userCommand, errors);
                user = _userDao.findUserFromId(user.getId().intValue());
                assertTrue(user.isEmailProvisional());
            } catch (Exception e) {
                fail(e.getMessage());
            }
        } finally {
            _userDao.removeUser(userId);
        }
    }

    /**
     * Test that on serious error during the registration process, no partially completed records
     * are left in the database.
     */
    public void testRegistrationFailureOnNewUser() {
        UserCommand userCommand = new UserCommand();
        BindException errors = new BindException(userCommand, "");
        String email = "testRegistrationFailureOnNewUser@RegistrationControllerTest.com";
        String password = "foobar";
        userCommand.setEmail(email);
        userCommand.setPassword(password);
        userCommand.setConfirmPassword(password);

        assertNull("Fake user already exists??", _userDao.findUserFromEmailIfExists(email));

        // set the mock mail sender to throw an exception
        _mailSender.setThrowOnSendMessage(true);

        try {
            _controller.onSubmit(getRequest(), getResponse(), userCommand, errors);
            fail("Expected mail exception not thrown");
        } catch (Exception ex) {
            assertNull("Record for new user was not deleted on failed registration",
                    _userDao.findUserFromEmailIfExists(email));
        } finally {
            _mailSender.setThrowOnSendMessage(false);
        }
    }

    /**
     * Test that on serious error during the registration process, no partially completed records
     * are left in the database.
     */
    public void testRegistrationFailureOnExistingUser() {
        User user = new User();
        user.setEmail("testRegistrationFailureOnExistingUser@greatschools.net");
        _userDao.saveUser(user);
        Integer userId = user.getId();

        try {
            user = _userDao.findUserFromId(user.getId().intValue());
            assertTrue(user.isPasswordEmpty());
            String email = user.getEmail();

            UserCommand userCommand = new UserCommand();
            BindException errors = new BindException(userCommand, "");
            String password = "foobar";
            userCommand.getUser().setEmail(email);

            userCommand.setPassword(password);
            userCommand.setConfirmPassword(password);

            // set the mock mail sender to throw an exception
            _mailSender.setThrowOnSendMessage(true);
            try {
                _controller.onSubmit(getRequest(), getResponse(), userCommand, errors);
                fail("Expected mail exception not thrown");
            } catch (Exception e) {
                user = _userDao.findUserFromId(user.getId().intValue());
                assertTrue("Record for existing user remains modified after failed registration",
                        user.isPasswordEmpty());
            }
        } finally {
            _userDao.removeUser(userId);
            _mailSender.setThrowOnSendMessage(false);
        }
    }
}
