package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.WelcomeMessageStatus;
import gs.web.BaseControllerTestCase;
import gs.web.auth.FacebookHelper;
import gs.web.auth.FacebookSession;
import gs.web.util.context.SessionContext;
import org.easymock.classextension.EasyMock;
import org.hibernate.validator.HibernateValidator;
import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.easymock.EasyMock.*;

public class UserRegistrationOrLoginServiceTest extends BaseControllerTestCase {
    UserRegistrationOrLoginService _service;

    private EmailVerificationEmail _emailVerificationEmail;

    private LocalValidatorFactoryBean _validatorFactory;

    private UserLoginCommand _userLoginCommand;

    private UserRegistrationCommand _userRegistrationCommand;

    private IUserDao _userDao;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        _service = new UserRegistrationOrLoginService(){
            public User createNewUser(UserRegistrationCommand userCommand, RegistrationBehavior registrationBehavior) {
                User user = new User();
                user.setId(1);
                user.setEmail("someone@somedomain.com");
                return user;
            }
        };

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

    @Test
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

    @Test
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
        assertTrue("User is email validated.", user.isEmailValidated());
    }

    @Test
    public void testRegisterUser() throws Exception{
        RegistrationBehavior registrationBehavior = new UspRegistrationBehavior();
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();

        //Empty command
        BindingResult bindingResult = new BeanPropertyBindingResult(_userRegistrationCommand, "userRegistrationCommand");
        User user = _service.registerUser(_userRegistrationCommand, registrationBehavior, bindingResult, request, response);

        assertNull("User email, password ,first name etc are not set in the command.Hence cannot register user.", user);
        assertTrue("There is no email, password, first name etc in the command.Hence errors.", bindingResult.hasErrors());
        resetAllMocks();

        //Invalid email,password etc.
        bindingResult = new BeanPropertyBindingResult(_userRegistrationCommand, "userRegistrationCommand");
        _userRegistrationCommand.setEmail("asd");
        _userRegistrationCommand.setPassword("a");
        _userRegistrationCommand.setConfirmPassword("as");
        _userRegistrationCommand.setFirstName("a");

        user = _service.registerUser(_userRegistrationCommand, registrationBehavior, bindingResult, request, response);
        assertNull("Invalid email, password ,first name etc in the command.Hence cannot register user.", user);
        assertTrue("Invalid email, password ,first name etc in the command.Hence errors.", bindingResult.hasErrors());
        resetAllMocks();

        //User already exists
        bindingResult = new BeanPropertyBindingResult(_userRegistrationCommand, "_userRegistrationCommand");
        _userRegistrationCommand.setEmail("someuser@somedomain.com");
        _userRegistrationCommand.setPassword("password");
        _userRegistrationCommand.setConfirmPassword(_userRegistrationCommand.getPassword());
        _userRegistrationCommand.setFirstName("somename");
        _userRegistrationCommand.setHow("Usp");
        _userRegistrationCommand.setTerms(true);

        User user1 = new User();
        expect(_userDao.findUserFromEmailIfExists(_userRegistrationCommand.getEmail())).andReturn(user1);

        replayAllMocks();
        user = _service.registerUser(_userRegistrationCommand, registrationBehavior, bindingResult, request, response);
        verifyAllMocks();
        assertNotNull("Valid user.", user);
        assertFalse("User is not email validated.", user.isEmailValidated());
        resetAllMocks();

        //No user exists.
        bindingResult = new BeanPropertyBindingResult(_userRegistrationCommand, "_userRegistrationCommand");
        registrationBehavior.setRedirectUrl("something");

        expect(_userDao.findUserFromEmailIfExists(_userRegistrationCommand.getEmail())).andReturn(null);
        User user2 = _service.createNewUser(_userRegistrationCommand, registrationBehavior);
        _userDao.saveUser(user2);
        expect(_userDao.findUserFromId(user2.getId())).andReturn(user2);
        _userDao.updateUser(user2);
        _userDao.updateUser(user2);
        try {
            _emailVerificationEmail.sendVerificationEmail(request, user2, registrationBehavior.getRedirectUrl(), null);
        } catch (Exception ex) {

        }

        replayAllMocks();
        user = _service.registerUser(_userRegistrationCommand, registrationBehavior, bindingResult, request, response);
        verifyAllMocks();
        assertNotNull("Valid user.", user);
        assertFalse("User is not email validated.", user.isEmailValidated());
        resetAllMocks();
    }

    @Test
    public void testRegisterUserWithNoFirstName() throws Exception{
        RegistrationBehavior registrationBehavior = new UspRegistrationBehavior();
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();
        BindingResult bindingResult;
        User user;

        bindingResult = new BeanPropertyBindingResult(_userRegistrationCommand, "_userRegistrationCommand");
        registrationBehavior.setRedirectUrl("something");

        _userRegistrationCommand = RegistrationTestUtils.validUserRegistrationCommand()
            .firstName(null);

        expect(_userDao.findUserFromEmailIfExists(_userRegistrationCommand.getEmail())).andReturn(null);

        User user2 = _service.createNewUser(_userRegistrationCommand, registrationBehavior);

        _userDao.saveUser(user2);

        expect(_userDao.findUserFromId(user2.getId())).andReturn(user2);

        _userDao.updateUser(user2);
        _userDao.updateUser(user2);
        try {
            _emailVerificationEmail.sendVerificationEmail(request, user2, registrationBehavior.getRedirectUrl(), null);
        } catch (Exception ex) {

        }

        replayAllMocks();
        user = _service.registerUser(_userRegistrationCommand, registrationBehavior, bindingResult, request, response);
        verifyAllMocks();
        assertNotNull("Valid user.", user);
        assertFalse("User is not email validated.", user.isEmailValidated());
        resetAllMocks();
    }

    @Test
    public void testRegisterUserWithFacebookId() throws Exception{
        RegistrationBehavior registrationBehavior = new UspRegistrationBehavior();
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();
        BindingResult bindingResult;
        User user;

        bindingResult = new BeanPropertyBindingResult(_userRegistrationCommand, "_userRegistrationCommand");
        registrationBehavior.setRedirectUrl("something");
        registrationBehavior.setFbSignedRequest("bogus");

        _userRegistrationCommand = RegistrationTestUtils.validUserRegistrationCommand()
            .facebookId("bogus")
            .password(null)
            .confirmPassword(null)
            .firstName("Timmy");

        expect(_userDao.findUserFromEmailIfExists(_userRegistrationCommand.getEmail())).andReturn(null);

        User user2 = _service.createNewUser(_userRegistrationCommand, registrationBehavior);
        user2.setEmailVerified(true);

        _userDao.saveUser(user2);

        expect(_userDao.findUserFromId(user2.getId())).andReturn(user2);

        // happens after user profile is created
        _userDao.updateUser(user2);

        try {
            _emailVerificationEmail.sendVerificationEmail(request, user2, registrationBehavior.getRedirectUrl(), null);
        } catch (Exception ex) {

        }

        replayAllMocks();
        user = _service.registerUser(_userRegistrationCommand, registrationBehavior, bindingResult, request, response);
        verifyAllMocks();
        assertNotNull("Expect valid user.", user);
        assertTrue("Expect user email to be flagged validated", user.getEmailVerified());
        assertEquals("Expect user facebookId to be correctly set", "bogus", user.getFacebookId());
        assertNull("Expect user password to be empty", user.getPasswordMd5());
        resetAllMocks();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterUserWithException() throws Exception{
        RegistrationBehavior registrationBehavior = new UspRegistrationBehavior();
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();
        BindingResult bindingResult = new BeanPropertyBindingResult(_userRegistrationCommand, "userRegistrationCommand");

        //Set valid fields on the command.
        _userRegistrationCommand.setEmail("someuser@somedomain.com");
        _userRegistrationCommand.setPassword("password");
        _userRegistrationCommand.setConfirmPassword(_userRegistrationCommand.getPassword());
        _userRegistrationCommand.setFirstName("somename");
        _userRegistrationCommand.setHow("Usp");
        _userRegistrationCommand.setTerms(true);

        //No user exists. But there was an error, therefore make sure the user is removed from the database.
        bindingResult = new BeanPropertyBindingResult(_userRegistrationCommand, "_userRegistrationCommand");
        registrationBehavior.setRedirectUrl("something");
        _service = new UserRegistrationOrLoginService();
        _service.setValidatorFactory(_validatorFactory);
        _service.setUserDao(_userDao);

        expect(_userDao.findUserFromEmailIfExists(_userRegistrationCommand.getEmail())).andReturn(null);
        _userDao.saveUser(isA(User.class));
        //Make sure the user is removed.
        _userDao.removeUser(null);

        replayAllMocks();
        User user = _service.registerUser(_userRegistrationCommand, registrationBehavior, bindingResult, request, response);
        verifyAllMocks();
        assertNull("Valid user.", user);
        resetAllMocks();
    }

    @Test
    public void testConvertToFacebookAccountIfNeeded_noExistingFacebookId() throws Exception {
        RegistrationBehavior registrationBehavior = new UspRegistrationBehavior();
        registrationBehavior.setFbSignedRequest("bogus");

        FacebookSession facebookSession = org.easymock.classextension.EasyMock.createStrictMock(FacebookSession.class);
        getRequest().setAttribute(FacebookSession.REQUEST_ATTRIBUTE, facebookSession);

        _userLoginCommand.setEmail("someuser@somedomain.com");

        User user = new User();
        user.setId(1);
        user.setEmail("someuser@somedomain.com");
        user.setEmailVerified(true);
        user.setFacebookId(null);
        user.setPlaintextPassword("abc");
        user.setEmailProvisional("abc");

        org.easymock.classextension.EasyMock.expect(facebookSession.getUserId()).andReturn("bogusUserId");
        _userDao.saveUser(user);

        replayAllMocks();
        org.easymock.classextension.EasyMock.replay(facebookSession);

        _service.convertToFacebookAccountIfNeeded(user, getRequest());

        verifyAllMocks();
        org.easymock.classextension.EasyMock.verify(facebookSession);

        assertEquals("Expect user's facebook ID to have been set", "bogusUserId", user.getFacebookId());
        assertEquals("Expect user's email to still be verified", Boolean.TRUE, user.getEmailVerified());
        assertFalse("Expect user to still not be provisional", user.isEmailProvisional());
    }

    @Test
    public void testConvertToFacebookAccountIfNeeded_emailNotAlreadyVerified() throws Exception {
        RegistrationBehavior registrationBehavior = new UspRegistrationBehavior();
        registrationBehavior.setFbSignedRequest("bogus");

        FacebookSession facebookSession = org.easymock.classextension.EasyMock.createStrictMock(FacebookSession.class);
        getRequest().setAttribute(FacebookSession.REQUEST_ATTRIBUTE, facebookSession);

        _userLoginCommand.setEmail("someuser@somedomain.com");

        User user = new User();
        user.setId(1);
        user.setEmail("someuser@somedomain.com");
        user.setEmailVerified(false);
        user.setFacebookId(null);

        org.easymock.classextension.EasyMock.expect(facebookSession.getUserId()).andReturn("bogusUserId");
        _userDao.saveUser(user);

        replayAllMocks();
        org.easymock.classextension.EasyMock.replay(facebookSession);

        _service.convertToFacebookAccountIfNeeded(user, getRequest());

        verifyAllMocks();
        org.easymock.classextension.EasyMock.verify(facebookSession);

        assertEquals("Expect user's facebook ID to have been set", "bogusUserId", user.getFacebookId());
        assertTrue("Expect user's email to have been marked verified", user.getEmailVerified());
    }

    @Test
    public void testConvertToFacebookAccountIfNeeded_withProvisionalEmail() throws Exception {
        RegistrationBehavior registrationBehavior = new UspRegistrationBehavior();
        registrationBehavior.setFbSignedRequest("bogus");

        FacebookSession facebookSession = org.easymock.classextension.EasyMock.createStrictMock(FacebookSession.class);
        getRequest().setAttribute(FacebookSession.REQUEST_ATTRIBUTE, facebookSession);

        _userLoginCommand.setEmail("someuser@somedomain.com");

        User user = new User();
        user.setId(1);
        user.setEmail("someuser@somedomain.com");
        user.setPlaintextPassword("abc");
        user.setEmailProvisional("abc");

        org.easymock.classextension.EasyMock.expect(facebookSession.getUserId()).andReturn("bogusUserId");
        _userDao.saveUser(user);

        replayAllMocks();
        org.easymock.classextension.EasyMock.replay(facebookSession);

        _service.convertToFacebookAccountIfNeeded(user, getRequest());

        verifyAllMocks();
        org.easymock.classextension.EasyMock.verify(facebookSession);

        assertEquals("Expect user's facebook ID to have been set", "bogusUserId", user.getFacebookId());
        assertFalse("Expect user to no longer be provisional", user.isEmailProvisional());
    }

}