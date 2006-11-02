package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.web.util.UrlBuilder;
import gs.data.community.IUserDao;
import gs.data.community.User;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.easymock.MockControl;

import java.security.NoSuchAlgorithmException;

/**
 * @author <a href="mailto:aroy@greatschools.net">Anthony Roy</a>
 */
public class LoginControllerTest extends BaseControllerTestCase {
    private LoginController _controller;

    private MockControl _userControl;
    private IUserDao _mockUserDao;

    protected void setUp() throws Exception {
        super.setUp();
        ApplicationContext appContext = getApplicationContext();
        _controller = (LoginController) appContext.getBean(LoginController.BEAN_ID);
        _userControl = MockControl.createControl(IUserDao.class);
        _mockUserDao = (IUserDao)_userControl.getMock();
        _controller.setUserDao(_mockUserDao);
    }

    public void testOnSubmitNoPassword() throws NoSuchAlgorithmException {
        User user = new User();
        user.setEmail("testLoginController@greatschools.net");
        user.setId(new Integer(99));
        _mockUserDao.findUserFromEmailIfExists(user.getEmail());
        _userControl.setReturnValue(user);
        _mockUserDao.findUserFromEmail(user.getEmail());
        _userControl.setReturnValue(user);
        _userControl.replay();

        LoginCommand command = new LoginCommand();
        command.setEmail(user.getEmail());
        BindException errors = new BindException(command, "");

        _controller.onBindOnNewForm(getRequest(), command, errors);
        _controller.onBindAndValidate(getRequest(), command, errors);
        assertFalse(errors.hasErrors());

        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), command, errors);
        _userControl.verify();
        assertFalse(errors.hasErrors());
        UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION, null);
        builder.addParameter("email", user.getEmail());
        builder.addParameter("redirect", LoginController.DEFAULT_REDIRECT_URL);
        String redirectUrl = "redirect:" + builder.asFullUrl(getRequest());

        assertEquals(redirectUrl, mAndV.getViewName());
    }

    public void testOnSubmit() throws NoSuchAlgorithmException {
        User user = new User();
        user.setEmail("testLoginController@greatschools.net");
        user.setId(new Integer(99));
        user.setPlaintextPassword("foobar");
        _mockUserDao.findUserFromEmailIfExists(user.getEmail());
        _userControl.setReturnValue(user);
        _mockUserDao.findUserFromEmail(user.getEmail());
        _userControl.setReturnValue(user);
        _userControl.replay();

        LoginCommand command = new LoginCommand();
        command.setEmail(user.getEmail());
        command.setPassword("foobar");
        command.setRedirect("/?14@@.598dae0f");
        BindException errors = new BindException(command, "");

        _controller.onBindOnNewForm(getRequest(), command, errors);
        _controller.onBindAndValidate(getRequest(), command, errors);
        assertFalse("Controller has errors on validate", errors.hasErrors());

        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), command, errors);
        _userControl.verify();
        assertFalse("Controller has errors on submit", errors.hasErrors());

        assertTrue(mAndV.getViewName().startsWith("redirect:"));
        assertTrue(mAndV.getViewName().indexOf(_controller.getAuthenticationManager().getParameterName()) > -1);
    }

    public void testNonexistantUser() throws NoSuchAlgorithmException {
        LoginCommand command = new LoginCommand();
        BindException errors = new BindException(command, "");

        _mockUserDao.findUserFromEmailIfExists("");
        _userControl.setReturnValue(null);
        _userControl.replay();
        _controller.setUserDao(_mockUserDao);
        _controller.onBindOnNewForm(getRequest(), command, errors);
        _controller.onBindAndValidate(getRequest(), command, errors);
        _userControl.verify();
        assertTrue("Controller does not have expected errors on validate", errors.hasErrors());
    }

    public void testProvisionalUser() throws NoSuchAlgorithmException {
        User user = new User();
        user.setEmail("testLoginController@greatschools.net");
        user.setId(new Integer(99));
        user.setPlaintextPassword("foobar");
        user.setEmailProvisional();

        _mockUserDao.findUserFromEmailIfExists("testLoginController@greatschools.net");
        _userControl.setReturnValue(user);
        _userControl.replay();

        LoginCommand command = new LoginCommand();
        command.setEmail("testLoginController@greatschools.net");
        BindException errors = new BindException(command, "");

        _controller.onBindOnNewForm(getRequest(), command, errors);
        _controller.onBindAndValidate(getRequest(), command, errors);
        _userControl.verify();
        assertTrue("Controller does not have expected errors on validate", errors.hasErrors());
    }

    public void testBadPassword() throws NoSuchAlgorithmException {
        User user = new User();
        user.setEmail("testLoginController@greatschools.net");
        user.setId(new Integer(99));
        user.setPlaintextPassword("foobar");

        _mockUserDao.findUserFromEmailIfExists("testLoginController@greatschools.net");
        _userControl.setReturnValue(user);
        _userControl.replay();

        LoginCommand command = new LoginCommand();
        command.setEmail("testLoginController@greatschools.net");
        command.setPassword("wrongPassword");
        BindException errors = new BindException(command, "");

        _controller.onBindOnNewForm(getRequest(), command, errors);
        _controller.onBindAndValidate(getRequest(), command, errors);
        _userControl.verify();
        assertTrue("Controller does not have expected errors on validate", errors.hasErrors());
    }
}
