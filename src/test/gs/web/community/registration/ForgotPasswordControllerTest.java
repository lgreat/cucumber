package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.util.email.MockJavaMailSender;
import gs.web.BaseControllerTestCase;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import org.easymock.MockControl;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Provides testing for the controller managing the forgot password page.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ForgotPasswordControllerTest extends BaseControllerTestCase {
    private ForgotPasswordController _controller;

    private IUserDao _userDao;
    private MockControl _userControl;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new ForgotPasswordController();
        MockJavaMailSender _mailSender = new MockJavaMailSender();
        // have to set host else the mock mail sender will throw an exception
        // actual value is irrelevant
        _mailSender.setHost("greatschools.net");
        ForgotPasswordEmail email = (ForgotPasswordEmail)
                getApplicationContext().getBean(ForgotPasswordEmail.BEAN_ID);
        email.getEmailHelperFactory().setMailSender(_mailSender);
        _controller.setForgotPasswordEmail(email);

        _userControl = MockControl.createControl(IUserDao.class);
        _userDao = (IUserDao) _userControl.getMock();
        _controller.setUserDao(_userDao);
    }

    public void testOnSubmit() throws Exception {
        String email = "forgotPasswordTest@greatschools.net";
        User user = new User();
        user.setEmail(email);
        user.setId(new Integer(123));
        user.setPlaintextPassword("foobar");

        _userDao.findUserFromEmailIfExists(email);
        _userControl.setReturnValue(user);
        _userControl.replay();

        UserCommand command = new UserCommand();
        command.setEmail(email);
        BindException errors = new BindException(command, "");

        _controller.onBindAndValidate(getRequest(), command, errors);
        _userControl.verify();
        assertFalse(errors.hasErrors());

        _userControl.reset();
        _userDao.findUserFromEmailIfExists(email);
        _userControl.setReturnValue(user);
        _userControl.replay();

        _controller.onSubmit(getRequest(), getResponse(), command, errors);
        _userControl.verify();
        assertFalse(errors.hasErrors());
    }

    public void testOnSubmitUserNotExist() throws Exception {
        String email = "forgotPasswordTest@greatschools.net";

        _userDao.findUserFromEmailIfExists(email);
        _userControl.setReturnValue(null);
        _userControl.replay();

        UserCommand command = new UserCommand();
        command.setEmail(email);
        BindException errors = new BindException(command, "");

        _controller.onBindAndValidate(getRequest(), command, errors);
        _userControl.verify();
        assertTrue(errors.hasErrors());
    }

    public void testNoPasswordUser() throws Exception {
        User user = new User();
        user.setEmail("forgotPasswordTest@greatschools.net");
        user.setId(new Integer(124));

        _userDao.findUserFromEmailIfExists("forgotPasswordTest@greatschools.net");
        _userControl.setReturnValue(user);
        _userControl.replay();

        UserCommand command = new UserCommand();
        command.setEmail("forgotPasswordTest@greatschools.net");
        BindException errors = new BindException(command, "");

        _controller.onBindAndValidate(getRequest(), command, errors);
        _userControl.verify();
        assertTrue("Controller missing expected errors on validation", errors.hasErrors());
    }

    public void testEmailProvisionalUser() throws Exception {
        User user = new User();
        user.setEmail("forgotPasswordTest@greatschools.net");
        user.setId(125);
        user.setPlaintextPassword("foobar");
        user.setEmailProvisional("foobar");

        _userDao.findUserFromEmailIfExists("forgotPasswordTest@greatschools.net");
        _userControl.setReturnValue(user);
        _userControl.replay();

        UserCommand command = new UserCommand();
        command.setEmail(user.getEmail());
        BindException errors = new BindException(command, "");

        _controller.onBindAndValidate(getRequest(), command, errors);
        _userControl.verify();
        assertTrue("Controller missing expected errors on validation", errors.hasErrors());
    }

    public void testDisabledUser() throws Exception {
        User user = new User();
        user.setEmail("forgotPasswordTest@greatschools.net");
        user.setId(125);
        user.setPlaintextPassword("foobar");
        user.setUserProfile(new UserProfile());
        user.getUserProfile().setActive(false);

        expect(_userDao.findUserFromEmailIfExists("forgotPasswordTest@greatschools.net")).andReturn(user);
        replay(_userDao);

        UserCommand command = new UserCommand();
        command.setEmail(user.getEmail());
        BindException errors = new BindException(command, "");

        _controller.onBindAndValidate(getRequest(), command, errors);
        verify(_userDao);
        assertTrue("Controller missing expected errors on validation", errors.hasErrors());
    }

    public void testSuppressValidation() throws Exception {
        getRequest().setParameter("cancel", "cancel");
        assertTrue(_controller.suppressValidation(getRequest()));

        UserCommand command = new UserCommand();
        BindException errors = new BindException(command, "");
        _userControl.replay();

        _controller.onBindAndValidate(getRequest(), command, errors);
        _userControl.verify();
        assertFalse(errors.hasErrors());

        _controller.onSubmit(getRequest(), getResponse(), command, errors);
        _userControl.verify();
        assertFalse(errors.hasErrors());
    }

    public void testCancelFromDev() throws Exception {
        getRequest().setParameter("cancel", "cancel");
        getRequest().setServerName("dev.greatschools.net");
        UserCommand command = new UserCommand();
        BindException errors = new BindException(command, "");

        assertTrue(_controller.suppressValidation(getRequest()));

        replay(_userDao);
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), command, errors);
        verify(_userDao);

        assertEquals("redirect:http://community.dev.greatschools.net/", mAndV.getViewName());

        assertFalse(errors.hasErrors());
    }

    public void testCancelFromDevWorkstation() throws Exception {
        getRequest().setParameter("cancel", "cancel");
        getRequest().setServerName("localhost");
        UserCommand command = new UserCommand();
        BindException errors = new BindException(command, "");

        assertTrue(_controller.suppressValidation(getRequest()));

        replay(_userDao);
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), command, errors);
        verify(_userDao);

        assertEquals("redirect:http://community.dev.greatschools.net/", mAndV.getViewName());

        assertFalse(errors.hasErrors());
    }

    public void testCancelFromStaging() throws Exception {
        getRequest().setParameter("cancel", "cancel");
        getRequest().setServerName("staging.greatschools.net");
        UserCommand command = new UserCommand();
        BindException errors = new BindException(command, "");

        assertTrue(_controller.suppressValidation(getRequest()));

        replay(_userDao);
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), command, errors);
        verify(_userDao);

        assertEquals("redirect:http://community.staging.greatschools.net/", mAndV.getViewName());

        assertFalse(errors.hasErrors());
    }

    public void testCancelFromWww() throws Exception {
        getRequest().setParameter("cancel", "cancel");
        getRequest().setServerName("www.greatschools.net");
        UserCommand command = new UserCommand();
        BindException errors = new BindException(command, "");

        assertTrue(_controller.suppressValidation(getRequest()));

        replay(_userDao);
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), command, errors);
        verify(_userDao);

        assertEquals("redirect:http://community.greatschools.net/", mAndV.getViewName());

        assertFalse(errors.hasErrors());
    }

    public void testCancelFromWwwCobrand() throws Exception {
        getRequest().setParameter("cancel", "cancel");
        getRequest().setServerName("sfgate.greatschools.net");
        UserCommand command = new UserCommand();
        BindException errors = new BindException(command, "");

        assertTrue(_controller.suppressValidation(getRequest()));

        replay(_userDao);
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), command, errors);
        verify(_userDao);

        assertEquals("redirect:http://community.greatschools.net/", mAndV.getViewName());

        assertFalse(errors.hasErrors());
    }
}
