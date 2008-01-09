package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import static org.easymock.EasyMock.*;

import java.security.NoSuchAlgorithmException;

/**
 * @author <a href="mailto:aroy@greatschools.net">Anthony Roy</a>
 */
public class LoginControllerTest extends BaseControllerTestCase {
    private LoginController _controller;
    private IUserDao _mockUserDao;
    private User _user;
    private LoginCommand _command;
    private BindException _errors;

    protected void setUp() throws Exception {
        super.setUp();
        ApplicationContext appContext = getApplicationContext();
        _controller = (LoginController) appContext.getBean(LoginController.BEAN_ID);

        _mockUserDao = createMock(IUserDao.class);
        _controller.setUserDao(_mockUserDao);

        _user = new User();
        _user.setEmail("testLoginController@greatschools.net");
        _user.setId(99);

        _command = new LoginCommand();
        _command.setEmail(_user.getEmail());
        _errors = new BindException(_command, "");
    }

    public void testOnSubmitNoPassword() throws NoSuchAlgorithmException {
        expect(_mockUserDao.findUserFromEmailIfExists(_user.getEmail())).andReturn(_user);
        expect(_mockUserDao.findUserFromEmail(_user.getEmail())).andReturn(_user);
        replay(_mockUserDao);

        _controller.onBindOnNewForm(getRequest(), _command, _errors);
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        assertTrue("MSL only users expect to get an error when trying to sign in", _errors.hasErrors());
    }

    public void testOnSubmitWithRedirect() throws NoSuchAlgorithmException {
        _user.setPlaintextPassword("foobar");
        expect(_mockUserDao.findUserFromEmailIfExists(_user.getEmail())).andReturn(_user);
        expect(_mockUserDao.findUserFromEmail(_user.getEmail())).andReturn(_user);
        replay(_mockUserDao);

        _command.setPassword("foobar");
        _command.setRedirect("/?14@@.598dae0f");

        _controller.onBindOnNewForm(getRequest(), _command, _errors);
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        assertFalse("Controller has errors on validate", _errors.hasErrors());

        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verify(_mockUserDao);
        assertFalse("Controller has errors on submit", _errors.hasErrors());

        assertTrue(mAndV.getViewName().startsWith("redirect:"));
    }

    public void testOnSubmitNoRedirect() throws NoSuchAlgorithmException {
        _user.setPlaintextPassword("foobar");
        expect(_mockUserDao.findUserFromEmailIfExists(_user.getEmail())).andReturn(_user);
        expect(_mockUserDao.findUserFromEmail(_user.getEmail())).andReturn(_user);
        replay(_mockUserDao);

        _command.setPassword("foobar");

        getRequest().setServerName("dev.greatschools.net");

        _controller.onBindOnNewForm(getRequest(), _command, _errors);
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        assertFalse("Controller has errors on validate", _errors.hasErrors());

        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verify(_mockUserDao);
        assertFalse("Controller has errors on submit", _errors.hasErrors());

        assertEquals("redirect:http://community.dev.greatschools.net/",
                mAndV.getViewName());
    }

    public void testNonexistantUser() throws NoSuchAlgorithmException {
        LoginCommand command = new LoginCommand();

        expect(_mockUserDao.findUserFromEmailIfExists("")).andReturn(null);
        replay(_mockUserDao);
        _controller.setUserDao(_mockUserDao);
        _controller.onBindOnNewForm(getRequest(), command, _errors);
        _controller.onBindAndValidate(getRequest(), command, _errors);
        verify(_mockUserDao);
        assertTrue("Controller does not have expected errors on validate", _errors.hasErrors());
    }

    public void testProvisionalUser() throws NoSuchAlgorithmException {
        _user.setPlaintextPassword("foobar");
        _user.setEmailProvisional("foobar");

        expect(_mockUserDao.findUserFromEmailIfExists("testLoginController@greatschools.net")).andReturn(_user);
        replay(_mockUserDao);

        LoginCommand command = new LoginCommand();
        command.setEmail("testLoginController@greatschools.net");

        _controller.onBindOnNewForm(getRequest(), command, _errors);
        _controller.onBindAndValidate(getRequest(), command, _errors);
        verify(_mockUserDao);
        assertTrue("Controller does not have expected errors on validate", _errors.hasErrors());
    }

    public void testBadPassword() throws NoSuchAlgorithmException {
        _user.setPlaintextPassword("foobar");

        expect(_mockUserDao.findUserFromEmailIfExists("testLoginController@greatschools.net")).andReturn(_user);
        replay(_mockUserDao);

        LoginCommand command = new LoginCommand();
        command.setEmail("testLoginController@greatschools.net");
        command.setPassword("wrongPassword");

        _controller.onBindOnNewForm(getRequest(), command, _errors);
        _controller.onBindAndValidate(getRequest(), command, _errors);
        verify(_mockUserDao);
        assertTrue("Controller does not have expected errors on validate", _errors.hasErrors());
    }

    public void testDisabledUser() throws NoSuchAlgorithmException {
        _user.setPlaintextPassword("foobar");
        _user.setUserProfile(new UserProfile());
        _user.getUserProfile().setActive(false);

        expect(_mockUserDao.findUserFromEmailIfExists("testLoginController@greatschools.net")).andReturn(_user);
        replay(_mockUserDao);

        LoginCommand command = new LoginCommand();
        command.setEmail("testLoginController@greatschools.net");
        command.setPassword("foobar");

        _controller.onBindOnNewForm(getRequest(), command, _errors);
        _controller.onBindAndValidate(getRequest(), command, _errors);
        verify(_mockUserDao);
        assertTrue("Controller does not have expected errors on validate", _errors.hasErrors());
    }

    public void testInitializeRedirectUrl() {
        getRequest().setServerName("dev.greatschools.net");
        _controller.initializeRedirectUrl(getRequest());
        assertEquals("http://community.dev.greatschools.net/", LoginController.DEFAULT_REDIRECT_URL);

        getRequest().setServerName("staging.greatschools.net");
        _controller.initializeRedirectUrl(getRequest());
        assertEquals("http://community.staging.greatschools.net/", LoginController.DEFAULT_REDIRECT_URL);

        getRequest().setServerName("www.greatschools.net");
        _controller.initializeRedirectUrl(getRequest());
        assertEquals("http://community.greatschools.net/", LoginController.DEFAULT_REDIRECT_URL);

        getRequest().setServerName("yahooed.greatschools.net");
        _controller.initializeRedirectUrl(getRequest());
        assertEquals("http://community.greatschools.net/", LoginController.DEFAULT_REDIRECT_URL);
    }
}
