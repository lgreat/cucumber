package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.community.WelcomeMessageStatus;
import gs.web.BaseControllerTestCase;
import gs.web.admin.EspModerationDetailsCommand;
import gs.web.util.context.SessionContext;
import org.easymock.classextension.EasyMock;
import org.hibernate.validator.HibernateValidator;
import org.junit.Before;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.easymock.EasyMock.*;

public class UserRegistrationOrLoginServiceTest extends BaseControllerTestCase {
    UserRegistrationOrLoginService _service;

    private EmailVerificationEmail _emailVerificationEmail;

    @Resource
    private LocalValidatorFactoryBean _validatorFactory;

    private UserLoginCommand _userLoginCommand;

    private UserRegistrationCommand _userRegistrationCommand;

    private IUserDao _userDao;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        _service = new UserRegistrationOrLoginService();
        _userLoginCommand = new UserLoginCommand();
        _userRegistrationCommand = new UserRegistrationCommand();
        _userDao = EasyMock.createStrictMock(IUserDao.class);
        _emailVerificationEmail = EasyMock.createMock(EmailVerificationEmail.class);

        _validatorFactory = new LocalValidatorFactoryBean();
        _validatorFactory.setProviderClass(HibernateValidator.class);
        _validatorFactory.afterPropertiesSet();
        _service.setValidatorFactory(_validatorFactory);
        _service.setUserDao(_userDao);
        _service.setEmailVerificationEmail(_emailVerificationEmail);
    }

    private void replayAllMocks() {
        replayMocks(_userDao, _emailVerificationEmail);
    }

    private void verifyAllMocks() {
        verifyMocks(_userDao, _emailVerificationEmail);
    }

    private void resetAllMocks() {
        resetMocks(_userDao, _emailVerificationEmail);
    }

    public void testGetUserFromSession() throws Exception {
        RegistrationBehavior registrationBehavior = new RegistrationBehavior();
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();
        User user = _service.getUserFromSession(registrationBehavior, request, response);

        assertNull("No user in the session.", user);

        //put user in session.
        SessionContext sessionContext = getSessionContext();
        User newUser = new User();
        sessionContext.setUser(newUser);
        user = _service.getUserFromSession(registrationBehavior, request, response);

        assertEquals("Get the user from the session.", newUser, user);
    }

    public void testLoginUser() throws Exception {
        RegistrationBehavior registrationBehavior = new UspRegistrationBehavior();
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();

        //Empty command
        User user = _service.loginUser(_userLoginCommand, registrationBehavior, request, response);
        assertNull("There is no email and password in the command.Hence cannot log in user.", user);

        //Invalid email.
        _userLoginCommand.setEmail("asd");
        user = _service.loginUser(_userLoginCommand, registrationBehavior, request, response);
        assertNull("Invalid email in the command.Hence cannot log in user.", user);
        resetAllMocks();

        //Valid email and password in the command. User is not email validated.Hence send the verification email.
        _userLoginCommand.setEmail("someuser@somedomain.com");
        _userLoginCommand.setPassword("password");

        registrationBehavior.setRedirectUrl("index.page");

        User user1 = new User();
        user1.setId(1);
        user1.setEmail("someuser@somedomain.com");
        user1.setPlaintextPassword("password");
        user1.setEmailVerified(false);
        user1.setEmailProvisional("password");

        expect(_userDao.findUserFromEmailIfExists("someuser@somedomain.com")).andReturn(user1);
        _emailVerificationEmail.sendVerificationEmail(request, user1, registrationBehavior.getRedirectUrl(), null);

        replayAllMocks();
        user = _service.loginUser(_userLoginCommand, registrationBehavior, request, response);
        verifyAllMocks();
        assertNotNull("Valid user.", user);
        assertFalse("User is not email validated.", user.isEmailValidated());
        resetAllMocks();

        //Valid email and password in the command. User is validated.
        user1 = new User();
        user1.setId(1);
        user1.setEmail("someuser@somedomain.com");
        user1.setPlaintextPassword("password");

        expect(_userDao.findUserFromEmailIfExists("someuser@somedomain.com")).andReturn(user1);

        replayAllMocks();
        user = _service.loginUser(_userLoginCommand, registrationBehavior, request, response);
        verifyAllMocks();

        assertNotNull("Valid user.", user);
        assertTrue("User is not email validated.", user.isEmailValidated());
    }

    public void testRegisterUser() throws Exception {
        RegistrationBehavior registrationBehavior = new UspRegistrationBehavior();
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();

        //Empty command
        BindingResult bindingResult = new BeanPropertyBindingResult(_userRegistrationCommand, "userRegistrationCommand");
        User user = _service.registerUser(_userRegistrationCommand, registrationBehavior, bindingResult, request, response);

        assertNull("User email, password ,first name etc are not set in the command.Hence cannot register in user.", user);
        assertTrue("There is no email, password, first name etc in the command.Hence errors.", bindingResult.hasErrors());
        resetAllMocks();

        //Invalid email,password etc.
        bindingResult = new BeanPropertyBindingResult(_userRegistrationCommand, "userRegistrationCommand");
        _userRegistrationCommand.setEmail("asd");
        _userRegistrationCommand.setPassword("a");
        _userRegistrationCommand.setConfirmPassword("as");
        _userRegistrationCommand.setFirstName("a");

        user = _service.registerUser(_userRegistrationCommand, registrationBehavior, bindingResult, request, response);
        assertNull("Invalid email,password and first name in the command.Hence cannot log in user.", user);
        assertTrue("Invalid email, password and first name in the command.Hence errors.", bindingResult.hasErrors());
        resetAllMocks();

         //User already exists
        _userRegistrationCommand.setEmail("someuser@somedomain.com");
        _userRegistrationCommand.setPassword("password");
        _userRegistrationCommand.setConfirmPassword(_userRegistrationCommand.getPassword());
        _userRegistrationCommand.setFirstName("somename");
        _userRegistrationCommand.setHow("Usp");
        _userRegistrationCommand.setTerms(true);
        bindingResult = new BeanPropertyBindingResult(_userRegistrationCommand, "_userRegistrationCommand");

        User user1 = new User();
        expect(_userDao.findUserFromEmailIfExists(_userRegistrationCommand.getEmail())).andReturn(user1);

        replayAllMocks();
        user = _service.registerUser(_userRegistrationCommand, registrationBehavior, bindingResult, request, response);
        verifyAllMocks();
        assertNotNull("Valid user.", user);
        resetAllMocks();
    }


}