package gs.web.community.registration.popup;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.util.email.MockJavaMailSender;
import gs.web.BaseControllerTestCase;
import gs.web.community.registration.ForgotPasswordEmail;
import gs.web.community.registration.ForgotPasswordCommand;

import static org.easymock.EasyMock.*;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ForgotPasswordHoverControllerTest extends BaseControllerTestCase {
    private ForgotPasswordHoverController _controller;

    private IUserDao _userDao;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new ForgotPasswordHoverController();
        MockJavaMailSender _mailSender = new MockJavaMailSender();
        // have to set host else the mock mail sender will throw an exception
        // actual value is irrelevant
        _mailSender.setHost("greatschools.net");
        ForgotPasswordEmail email = (ForgotPasswordEmail)
                getApplicationContext().getBean(ForgotPasswordEmail.BEAN_ID);
        email.getEmailHelperFactory().setMailSender(_mailSender);
        _controller.setForgotPasswordEmail(email);

        _userDao = createStrictMock(IUserDao.class);
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

        ForgotPasswordCommand command = new ForgotPasswordCommand();
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

    public void testMslUser() throws Exception {
        String email = "forgotPasswordTest@greatschools.net";
        User user = new User();
        user.setEmail(email);
        user.setId(123);

        expect(_userDao.findUserFromEmailIfExists(email)).andReturn(user);
        replay(_userDao);

        ForgotPasswordCommand command = new ForgotPasswordCommand();
        command.setEmail(email);
        BindException errors = new BindException(command, "");

        _controller.onBindAndValidate(getRequest(), command, errors);
        verify(_userDao);
        assertFalse(errors.hasErrors());

        reset(_userDao);
        replay(_userDao);

        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), command, errors);
        verify(_userDao);
        assertFalse(errors.hasErrors());
        assertTrue(StringUtils.contains(mAndV.getViewName(), "registration"));
    }

    public void testEmailProvisionalUser() throws Exception {
        User user = new User();
        user.setEmail("forgotPasswordTest@greatschools.net");
        user.setId(125);
        user.setPlaintextPassword("foobar");
        user.setEmailProvisional("foobar");

        expect(_userDao.findUserFromEmailIfExists("forgotPasswordTest@greatschools.net")).andReturn(user);
        replay(_userDao);

        ForgotPasswordCommand command = new ForgotPasswordCommand();
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

        ForgotPasswordCommand command = new ForgotPasswordCommand();
        command.setEmail(user.getEmail());
        BindException errors = new BindException(command, "");

        _controller.onBindAndValidate(getRequest(), command, errors);
        verify(_userDao);
        assertTrue("Controller missing expected errors on validation", errors.hasErrors());
    }

    public void testSuppressValidation() throws Exception {
        ForgotPasswordCommand command = new ForgotPasswordCommand();
        getRequest().setParameter("cancel", "cancel");

        BindException errors = new BindException(command, "");
        replay(_userDao);

        _controller.onBindAndValidate(getRequest(), command, errors);
        verify(_userDao);
        assertFalse(errors.hasErrors());

        _controller.onSubmit(getRequest(), getResponse(), command, errors);
        verify(_userDao);
        assertFalse(errors.hasErrors());
    }

}
