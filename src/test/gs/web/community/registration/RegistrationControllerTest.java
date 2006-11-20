package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.data.util.email.MockJavaMailSender;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.easymock.MockControl;

import java.security.NoSuchAlgorithmException;

/**
 * Provides ...
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RegistrationControllerTest extends BaseControllerTestCase {
    private RegistrationController _controller;

    private IUserDao _userDao;
    private MockControl _userControl;
    private MockJavaMailSender _mailSender;

    private static final String SUCCESS_VIEW = "/community/registration/registrationSuccess";

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new RegistrationController();
        _mailSender = new MockJavaMailSender();
        // have to set host else the mock mail sender will throw an exception
        // actual value is irrelevant
        _mailSender.setHost("greatschools.net");
        _controller.setMailSender(_mailSender);
        _userControl = MockControl.createControl(IUserDao.class);
        _userDao = (IUserDao)_userControl.getMock();
        _controller.setUserDao(_userDao);
        _controller.setSuccessView(SUCCESS_VIEW);

        RegistrationConfirmationEmail email = (RegistrationConfirmationEmail)
                getApplicationContext().getBean(RegistrationConfirmationEmail.BEAN_ID);
        email.getEmailHelperFactory().setMailSender(_mailSender);
        _controller.setRegistrationConfirmationEmail(email);
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
        userCommand.setScreenName("screeny");
        userCommand.setNumSchoolChildren(new Integer(0));

        userCommand.getUser().setId(new Integer(345)); // to fake the database save

        _userControl.expectAndReturn(_userDao.findUserFromEmailIfExists(email), null);
        _userDao.saveUser(userCommand.getUser());
        _userDao.updateUser(userCommand.getUser());
        _userDao.updateUser(userCommand.getUser());
        _userControl.replay();

        getRequest().addParameter("next", "next"); // submit button
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), userCommand, errors);
        _userControl.verify();

        assertEquals("Not getting expected success view",
                SUCCESS_VIEW, mAndV.getViewName());
    }

    /**
     * Test successful registration with an existing user
     * @throws NoSuchAlgorithmException
     */
    public void testExistingUser() throws NoSuchAlgorithmException {
        String email = "testExistingUser@greatschools.net";
        Integer userId = new Integer(346);

        UserCommand userCommand = new UserCommand();
        BindException errors = new BindException(userCommand, "");
        String password = "foobar";
        userCommand.getUser().setEmail(email);

        userCommand.setPassword(password);
        userCommand.setConfirmPassword(password);
        userCommand.setScreenName("screeny");
        userCommand.setNumSchoolChildren(new Integer(0));
        userCommand.getUser().setId(userId);

        assertTrue(userCommand.getUser().isPasswordEmpty());
        assertFalse(userCommand.getUser().isEmailProvisional());

        _userControl.expectAndReturn(_userDao.findUserFromEmailIfExists(email),
                userCommand.getUser());
        _userDao.updateUser(userCommand.getUser());
        _userDao.updateUser(userCommand.getUser());
        _userControl.replay();

        try {
            _controller.onSubmit(getRequest(), getResponse(), userCommand, errors);
            _userControl.verify();
            assertTrue(userCommand.getUser().isEmailProvisional());
            assertFalse(userCommand.getUser().isPasswordEmpty());
        } catch (Exception e) {
            fail(e.toString());
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
        Integer userId = new Integer(347);
        userCommand.setEmail(email);
        userCommand.setPassword(password);
        userCommand.setConfirmPassword(password);
        userCommand.getUser().setId(userId);

        _userControl.expectAndReturn(_userDao.findUserFromEmailIfExists(email), null);
        _userDao.saveUser(userCommand.getUser());
        _userDao.updateUser(userCommand.getUser());
        _userDao.removeUser(userId);
        _userControl.replay();

        // set the mock mail sender to throw an exception
        _mailSender.setThrowOnSendMessage(true);

        try {
            _controller.onSubmit(getRequest(), getResponse(), userCommand, errors);
            fail("Expected mail exception not thrown");
        } catch (Exception ex) {
            _userControl.verify();
        } finally {
            _mailSender.setThrowOnSendMessage(false);
        }
    }

    /**
     * Test that on serious error during the registration process, no partially completed records
     * are left in the database.
     */
    public void testRegistrationFailureOnExistingUser() {
        UserCommand userCommand = new UserCommand();
        BindException errors = new BindException(userCommand, "");
        String email = "testRegistrationFailureOnExistingUser@greatschools.net";
        String password = "foobar";
        Integer userId = new Integer(348);
        userCommand.setEmail(email);
        userCommand.setPassword(password);
        userCommand.setConfirmPassword(password);
        userCommand.getUser().setId(userId);

        User user = userCommand.getUser();
        assertTrue(user.isPasswordEmpty());

        _userControl.expectAndReturn(_userDao.findUserFromEmailIfExists(email), user);
        _userDao.updateUser(userCommand.getUser());
        _userDao.updateUser(userCommand.getUser());
        _userControl.replay();

        // set the mock mail sender to throw an exception
        _mailSender.setThrowOnSendMessage(true);
        try {
            _controller.onSubmit(getRequest(), getResponse(), userCommand, errors);
            fail("Expected mail exception not thrown");
        } catch (Exception e) {
            _userControl.verify();
            assertTrue(user.isPasswordEmpty());
        } finally {
            _mailSender.setThrowOnSendMessage(false);
        }
    }
}
