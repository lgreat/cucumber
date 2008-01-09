package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.util.email.MockJavaMailSender;
import gs.web.BaseControllerTestCase;
import static org.easymock.EasyMock.*;
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

        _userDao = createMock(IUserDao.class);
        _controller.setUserDao(_userDao);
    }

    public void testOnSubmit() throws Exception {
        String email = "forgotPasswordTest@greatschools.net";
        User user = new User();
        user.setEmail(email);
        user.setId(123);
        user.setPlaintextPassword("foobar");

        expect(_userDao.findUserFromEmailIfExists(email)).andReturn(user);
        replay(_userDao);

        UserCommand command = new UserCommand();
        command.setEmail(email);
        BindException errors = new BindException(command, "");

        _controller.onBindAndValidate(getRequest(), command, errors);
        verify(_userDao);
        assertFalse(errors.hasErrors());

        reset(_userDao);
        expect(_userDao.findUserFromEmailIfExists(email)).andReturn(user);
        replay(_userDao);

        _controller.onSubmit(getRequest(), getResponse(), command, errors);
        verify(_userDao);
        assertFalse(errors.hasErrors());
    }

    public void testOnSubmitUserNotExist() throws Exception {
        String email = "forgotPasswordTest@greatschools.net";

        expect(_userDao.findUserFromEmailIfExists(email)).andReturn(null);
        replay(_userDao);

        UserCommand command = new UserCommand();
        command.setEmail(email);
        BindException errors = new BindException(command, "");

        _controller.onBindAndValidate(getRequest(), command, errors);
        verify(_userDao);
        assertTrue(errors.hasErrors());
    }

    public void testNoPasswordUser() throws Exception {
        User user = new User();
        user.setEmail("forgotPasswordTest@greatschools.net");
        user.setId(124);

        expect(_userDao.findUserFromEmailIfExists("forgotPasswordTest@greatschools.net")).andReturn(user);
        replay(_userDao);

        UserCommand command = new UserCommand();
        command.setEmail("forgotPasswordTest@greatschools.net");
        BindException errors = new BindException(command, "");

        _controller.onBindAndValidate(getRequest(), command, errors);
        verify(_userDao);
        assertTrue("Controller missing expected errors on validation", errors.hasErrors());
    }

    public void testEmailProvisionalUser() throws Exception {
        User user = new User();
        user.setEmail("forgotPasswordTest@greatschools.net");
        user.setId(125);
        user.setPlaintextPassword("foobar");
        user.setEmailProvisional("foobar");

        expect(_userDao.findUserFromEmailIfExists("forgotPasswordTest@greatschools.net")).andReturn(user);
        replay(_userDao);

        UserCommand command = new UserCommand();
        command.setEmail(user.getEmail());
        BindException errors = new BindException(command, "");

        _controller.onBindAndValidate(getRequest(), command, errors);
        verify(_userDao);
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
        replay(_userDao);

        _controller.onBindAndValidate(getRequest(), command, errors);
        verify(_userDao);
        assertFalse(errors.hasErrors());

        _controller.onSubmit(getRequest(), getResponse(), command, errors);
        verify(_userDao);
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

    public void testCancelWithReferrer() throws Exception {
        getRequest().setParameter("cancel", "cancel");
        getRequest().setServerName("dev.greatschools.net");
        UserCommand command = new UserCommand();
        command.setReferrer("http://hello.kit.ty/");
        BindException errors = new BindException(command, "");

        assertTrue(_controller.suppressValidation(getRequest()));

        replay(_userDao);
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), command, errors);
        verify(_userDao);

        assertEquals("redirect:http://hello.kit.ty/", mAndV.getViewName());

        assertFalse(errors.hasErrors());
    }

    public void testCancelWithReferrerIframe() throws Exception {
        getRequest().setParameter("cancel", "cancel");
        getRequest().setServerName("dev.greatschools.net");
        UserCommand command = new UserCommand();
        command.setReferrer("http://community.dev.greatschools.net/login_iframe?redirect=blahblah");
        BindException errors = new BindException(command, "");

        assertTrue(_controller.suppressValidation(getRequest()));

        replay(_userDao);
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), command, errors);
        verify(_userDao);

        assertEquals("redirect:http://community.dev.greatschools.net/", mAndV.getViewName());

        assertFalse(errors.hasErrors());
    }

    public void testBindReferrer() {
        UserCommand command = new UserCommand();
        BindException errors = new BindException(command, "");

        getRequest().addHeader("REFERER", "http://good.b.ye/");

        assertNull(command.getReferrer());
        _controller.onBindOnNewForm(getRequest(), command, errors);
        assertEquals("http://good.b.ye/", command.getReferrer());
    }

    public void testBindNoReferrer() {
        UserCommand command = new UserCommand();
        BindException errors = new BindException(command, "");

        assertNull(command.getReferrer());
        _controller.onBindOnNewForm(getRequest(), command, errors);
        assertNull(command.getReferrer());
    }
}
