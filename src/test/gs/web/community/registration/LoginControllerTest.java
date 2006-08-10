package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.web.util.UrlBuilder;
import gs.web.util.UrlUtil;
import gs.data.community.IUserDao;
import gs.data.community.User;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import java.security.NoSuchAlgorithmException;

/**
 * Created by IntelliJ IDEA.
 * User: UrbanaSoft
 * Date: Jul 18, 2006
 * Time: 12:03:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoginControllerTest extends BaseControllerTestCase {
    private LoginController _controller;

    private IUserDao _userDao;

    protected void setUp() throws Exception {
        super.setUp();
        ApplicationContext appContext = getApplicationContext();
        _controller = (LoginController) appContext.getBean(LoginController.BEAN_ID);

        _userDao = (IUserDao)appContext.getBean(IUserDao.BEAN_ID);
    }

    public void testOnSubmitNoPassword() throws NoSuchAlgorithmException {
        User user = new User();
        user.setEmail("testLoginController@greatschools.net");
        _userDao.saveUser(user);

        try {
            LoginCommand command = new LoginCommand();
            command.setEmail(user.getEmail());
            BindException errors = new BindException(command, "");

            _controller.onBindOnNewForm(getRequest(), command, errors);
            _controller.onBindAndValidate(getRequest(), command, errors);
            assertFalse(errors.hasErrors());

            ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), command, errors);
            assertFalse(errors.hasErrors());
            UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION, null);
            builder.addParameter("email", user.getEmail());
            builder.addParameter("redirect", LoginController.DEFAULT_REDIRECT_URL);
            String redirectUrl = "redirect:" + builder.asFullUrl(getRequest());

            assertEquals(redirectUrl, mAndV.getViewName());
        } finally {
            _userDao.removeUser(user.getId());
        }
    }

    public void testOnSubmit() throws NoSuchAlgorithmException {
        User user = new User();
        user.setEmail("testLoginController@greatschools.net");
        _userDao.saveUser(user);

        try {
            user.setPlaintextPassword("foobar");
            _userDao.updateUser(user);
            LoginCommand command = new LoginCommand();
            command.setEmail(user.getEmail());
            command.setPassword("foobar");
            BindException errors = new BindException(command, "");

            _controller.onBindOnNewForm(getRequest(), command, errors);
            _controller.onBindAndValidate(getRequest(), command, errors);
            assertFalse("Controller has errors on validate", errors.hasErrors());

            ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), command, errors);
            assertFalse("Controller has errors on submit", errors.hasErrors());
            UrlUtil urlUtil = new UrlUtil();

            String redirectUrl = "redirect:" + urlUtil.buildUrl(LoginController.DEFAULT_REDIRECT_URL, getRequest());

            assertEquals(redirectUrl, mAndV.getViewName());
        } finally {
            _userDao.removeUser(user.getId());
        }
    }

    public void testNonexistantUser() throws NoSuchAlgorithmException {
        LoginCommand command = new LoginCommand();
        BindException errors = new BindException(command, "");

        _controller.onBindOnNewForm(getRequest(), command, errors);
        _controller.onBindAndValidate(getRequest(), command, errors);
        assertTrue("Controller does not have expected errors on validate", errors.hasErrors());
    }

    public void testProvisionalUser() throws NoSuchAlgorithmException {
        User user = new User();
        user.setEmail("testLoginController@greatschools.net");
        _userDao.saveUser(user);

        try {
            user.setPlaintextPassword("foobar");
            user.setEmailProvisional();
            _userDao.updateUser(user);
            LoginCommand command = new LoginCommand();
            command.setEmail("testLoginController@greatschools.net");
            BindException errors = new BindException(command, "");

            _controller.onBindOnNewForm(getRequest(), command, errors);
            _controller.onBindAndValidate(getRequest(), command, errors);
            assertTrue("Controller does not have expected errors on validate", errors.hasErrors());
        } finally {
            _userDao.removeUser(user.getId());
        }
    }

    public void testBadPassword() throws NoSuchAlgorithmException {
        User user = new User();
        user.setEmail("testLoginController@greatschools.net");
        _userDao.saveUser(user);

        try {
            user.setPlaintextPassword("foobar");
            _userDao.updateUser(user);
            LoginCommand command = new LoginCommand();
            command.setEmail("testLoginController@greatschools.net");
            command.setPassword("wrongPassword");
            BindException errors = new BindException(command, "");

            _controller.onBindOnNewForm(getRequest(), command, errors);
            _controller.onBindAndValidate(getRequest(), command, errors);
            assertTrue("Controller does not have expected errors on validate", errors.hasErrors());
        } finally {
            _userDao.removeUser(user.getId());
        }
    }
}
