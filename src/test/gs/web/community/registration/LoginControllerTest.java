package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author <a href="mailto:aroy@greatschools.org">Anthony Roy</a>
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
        _user.setEmail("testLoginController@greatschools.org");
        _user.setId(99);
        _user.setUserProfile(new UserProfile());

        _command = new LoginCommand();
        _command.setEmail(_user.getEmail());
        _errors = new BindException(_command, "");
    }

    public void testOnSubmitNoPassword() throws Exception {
        expect(_mockUserDao.findUserFromEmailIfExists(_user.getEmail())).andReturn(_user);
        expect(_mockUserDao.findUserFromEmail(_user.getEmail())).andReturn(_user);
        replay(_mockUserDao);

        _controller.onBindOnNewForm(getRequest(), _command, _errors);
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        assertTrue("MSL only users expect to get an error when trying to sign in", _errors.hasErrors());
    }

    public void testOnSubmitWithRedirect() throws Exception {
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

    public void testOnSubmitNoRedirect() throws Exception {
        _user.setPlaintextPassword("foobar");
        expect(_mockUserDao.findUserFromEmailIfExists(_user.getEmail())).andReturn(_user);
        expect(_mockUserDao.findUserFromEmail(_user.getEmail())).andReturn(_user);
        replay(_mockUserDao);

        _command.setPassword("foobar");

        getRequest().setServerName("dev.greatschools.org");

        _controller.onBindOnNewForm(getRequest(), _command, _errors);
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        assertFalse("Controller has errors on validate", _errors.hasErrors());


        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        verify(_mockUserDao);
        assertFalse("Controller has errors on submit", _errors.hasErrors());

        assertEquals("redirect:/account/",
                mAndV.getViewName());
    }

    public void testNonexistantUser() throws Exception {
        LoginCommand command = new LoginCommand();

        expect(_mockUserDao.findUserFromEmailIfExists("")).andReturn(null);
        replay(_mockUserDao);
        _controller.setUserDao(_mockUserDao);
        _controller.onBindOnNewForm(getRequest(), command, _errors);
        _controller.onBindAndValidate(getRequest(), command, _errors);
        verify(_mockUserDao);
        assertTrue("Controller does not have expected errors on validate", _errors.hasErrors());
    }

    public void testProvisionalUser() throws Exception {
        _user.setPlaintextPassword("foobar");
        _user.setEmailProvisional("foobar");

        expect(_mockUserDao.findUserFromEmailIfExists("testLoginController@greatschools.org")).andReturn(_user);
        replay(_mockUserDao);

        LoginCommand command = new LoginCommand();
        command.setEmail("testLoginController@greatschools.org");

        _controller.onBindOnNewForm(getRequest(), command, _errors);
        _controller.onBindAndValidate(getRequest(), command, _errors);
        verify(_mockUserDao);
        assertTrue("Controller does not have expected errors on validate", _errors.hasErrors());
    }

    public void testBadPassword() throws Exception {
        _user.setPlaintextPassword("foobar");

        expect(_mockUserDao.findUserFromEmailIfExists("testLoginController@greatschools.org")).andReturn(_user);
        replay(_mockUserDao);

        LoginCommand command = new LoginCommand();
        command.setEmail("testLoginController@greatschools.org");
        command.setPassword("wrongPassword");

        _controller.onBindOnNewForm(getRequest(), command, _errors);
        _controller.onBindAndValidate(getRequest(), command, _errors);
        verify(_mockUserDao);
        assertTrue("Controller does not have expected errors on validate", _errors.hasErrors());
    }

    public void testDisabledUser() throws Exception {
        _user.setPlaintextPassword("foobar");
        _user.setUserProfile(new UserProfile());
        _user.getUserProfile().setActive(false);

        expect(_mockUserDao.findUserFromEmailIfExists("testLoginController@greatschools.org")).andReturn(_user);
        replay(_mockUserDao);

        LoginCommand command = new LoginCommand();
        command.setEmail("testLoginController@greatschools.org");
        command.setPassword("foobar");

        _controller.onBindOnNewForm(getRequest(), command, _errors);
        _controller.onBindAndValidate(getRequest(), command, _errors);
        verify(_mockUserDao);
        assertTrue("Controller does not have expected errors on validate", _errors.hasErrors());
    }

    public void testInitializeRedirectUrl() {
        _controller.initializeRedirectUrl(getRequest());
        assertEquals("/account/", LoginController.DEFAULT_REDIRECT_URL);
    }
}
