package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.web.util.MockJavaMailSender;
import gs.data.community.IUserDao;
import gs.data.community.User;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.BindException;
import org.springframework.mail.SimpleMailMessage;

import java.security.NoSuchAlgorithmException;

/**
 * Created by IntelliJ IDEA.
 * User: UrbanaSoft
 * Date: Jul 18, 2006
 * Time: 10:07:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class ForgotPasswordControllerTest extends BaseControllerTestCase {
    private ForgotPasswordController _controller;

    private IUserDao _userDao;

    protected void setUp() throws Exception {
        super.setUp();
        ApplicationContext appContext = getApplicationContext();
        _controller = (ForgotPasswordController) appContext.getBean(ForgotPasswordController.BEAN_ID);
        MockJavaMailSender mailSender = new MockJavaMailSender();
        // have to set host else the mock mail sender will throw an exception
        // actual value is irrelevant
        mailSender.setHost("greatschools.net");
        _controller.setMailSender(mailSender);
        _userDao = (IUserDao)appContext.getBean(IUserDao.BEAN_ID);
    }

    public void testOnSubmit() throws Exception {
        User user = new User();
        user.setEmail("forgotPasswordTest@greatschools.net");
        _userDao.saveUser(user);
        try {
            user.setPlaintextPassword("foobar");
            _userDao.updateUser(user);

            UserCommand command = new UserCommand();
            command.setEmail("forgotPasswordTest@greatschools.net");
            BindException errors = new BindException(command, "");

            _controller.onBindAndValidate(getRequest(), command, errors);
            assertFalse(errors.hasErrors());

            _controller.onSubmit(getRequest(), getResponse(), command, errors);
            assertFalse(errors.hasErrors());
        } finally {
            _userDao.removeUser(user.getId());
        }
    }

    // deprecated since page no longer checks for missing user
    public void xtestNonexistantUser() throws Exception {
        UserCommand command = new UserCommand();
        command.setEmail("forgotPasswordTest@greatschools.net");
        BindException errors = new BindException(command, "");

        _controller.onBindAndValidate(getRequest(), command, errors);
        assertTrue("Controller missing expected errors on validation", errors.hasErrors());
    }

    public void testNoPasswordUser() throws Exception {
        User user = new User();
        user.setEmail("forgotPasswordTest@greatschools.net");
        _userDao.saveUser(user);
        try {
            UserCommand command = new UserCommand();
            command.setEmail("forgotPasswordTest@greatschools.net");
            BindException errors = new BindException(command, "");

            _controller.onBindAndValidate(getRequest(), command, errors);
            assertTrue("Controller missing expected errors on validation", errors.hasErrors());
        } finally {
            _userDao.removeUser(user.getId());
        }
    }

    public void testEmailProvisionalUser() throws Exception {
        User user = new User();
        user.setEmail("forgotPasswordTest@greatschools.net");
        _userDao.saveUser(user);
        try {
            user.setPlaintextPassword("foobar");
            user.setEmailProvisional();
            _userDao.updateUser(user);

            UserCommand command = new UserCommand();
            command.setEmail(user.getEmail());
            BindException errors = new BindException(command, "");

            _controller.onBindAndValidate(getRequest(), command, errors);
            assertTrue("Controller missing expected errors on validation", errors.hasErrors());
        } finally {
            _userDao.removeUser(user.getId());
        }
    }

    public void testBuildEmailMessage() throws NoSuchAlgorithmException {
        User user = new User();
        user.setEmail("forgotPasswordTest@greatschools.net");
        _userDao.saveUser(user);
        try {
            UserCommand command = new UserCommand();
            command.setUser(user);
            SimpleMailMessage message;
            message = _controller.buildEmailMessageForExistingUser(getRequest(), command);
            assertEquals(message.getTo()[0], command.getEmail());
            assertNotNull(message.getFrom());
            assertNotNull(message.getSubject());
            assertNotNull(message.getText());

            message = _controller.buildEmailMessageForNewUser(getRequest(), command);
            assertEquals(message.getTo()[0], command.getEmail());
            assertNotNull(message.getFrom());
            assertNotNull(message.getSubject());
            assertNotNull(message.getText());
        } finally {
            _userDao.removeUser(user.getId());
        }
    }
}
