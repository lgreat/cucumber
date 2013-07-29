package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.web.BaseControllerTestCase;
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
            public User createNewUser(UserRegistrationCommand userCommand, RegistrationOrLoginBehavior registrationOrLoginBehavior) {
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
        HttpServletRequest request = getRequest();
        UserRegistrationOrLoginService.Summary summary = _service.getUserFromSession(request);

        assertNull("No user in the session.", summary);

        //put user in session.
        SessionContext sessionContext = getSessionContext();
        User newUser = new User();
        sessionContext.setUser(newUser);
        summary = _service.getUserFromSession(request);

        assertEquals("Get the user from the session.", newUser, summary.getUser());
        assertTrue("User was in the session.", summary.wasUserInSession());
    }

    @Test
    public void testLoginUser() throws Exception {
        RegistrationOrLoginBehavior registrationOrLoginBehavior = new UspRegistrationOrLoginBehavior();
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();

        //Empty command
        UserRegistrationOrLoginService.Summary summary = _service.loginUser(_userLoginCommand, request, response);
        assertNull("There is no email and password in the command.Hence cannot log in user.", summary);

        //Invalid email.
        _userLoginCommand.setEmail("asd");
        summary = _service.loginUser(_userLoginCommand, request, response);
        assertNull("Invalid email in the command.Hence cannot log in user.", summary);
        resetAllMocks();

      //Valid email and password in the command. User is not email validated.Hence cannot log in the user.
        _userLoginCommand.setEmail("someuser@somedomain.com");
        _userLoginCommand.setPassword("password");

        // User is not email validated.
        User user1 = new User();
        user1.setId(1);
        user1.setEmail("someuser@somedomain.com");
        user1.setPlaintextPassword("password");
        user1.setEmailVerified(false);
        user1.setEmailProvisional("password");

        expect(_userDao.findUserFromEmailIfExists("someuser@somedomain.com")).andReturn(user1);

        replayAllMocks();
        summary = _service.loginUser(_userLoginCommand, request, response);
        verifyAllMocks();
        assertNotNull("Valid user.", summary.getUser());
        assertFalse("User is not email validated.", summary.getUser().isEmailValidated());
        assertFalse("User was not email validated.Hence cannot log in.", summary.wasUserLoggedIn());
        resetAllMocks();
        resetAllMocks();

        //Valid email and password in the command. User is email validated.Therefore login the user.
        user1 = new User();
        user1.setId(1);
        user1.setEmail("someuser@somedomain.com");
        user1.setPlaintextPassword("password");

        expect(_userDao.findUserFromEmailIfExists("someuser@somedomain.com")).andReturn(user1);

        replayAllMocks();
        summary = _service.loginUser(_userLoginCommand, request, response);
        verifyAllMocks();

        assertNotNull("Valid user.", summary.getUser());
        assertTrue("User is email validated.", summary.getUser().isEmailValidated());
        assertTrue("User was email validated.Hence log in.", summary.wasUserLoggedIn());
    }

    @Test
    public void testSendValidationEmail() throws Exception {
        RegistrationOrLoginBehavior registrationOrLoginBehavior = new UspRegistrationOrLoginBehavior();
        registrationOrLoginBehavior.setRedirectUrl("index.page");

        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();

        _userLoginCommand.setEmail("someuser@somedomain.com");

        // User is not email validated.Hence send the verification email.
        User user1 = new User();
        user1.setId(1);
        user1.setEmail("someuser@somedomain.com");
        user1.setPlaintextPassword("password");
        user1.setEmailVerified(false);
        user1.setEmailProvisional("password");

        expect(_userDao.findUserFromEmailIfExists("someuser@somedomain.com")).andReturn(user1);
        _emailVerificationEmail.sendVerificationEmail(request, user1, registrationOrLoginBehavior.getRedirectUrl(), null);

        replayAllMocks();
        UserRegistrationOrLoginService.Summary summary = _service.sendVerificationEmail(_userLoginCommand, registrationOrLoginBehavior, request);
        verifyAllMocks();
        assertNotNull("Valid user.", summary.getUser());
        assertFalse("User is not email validated.", summary.getUser().isEmailValidated());
        assertTrue("User is not email validated. Hence send verification email.", summary.wasVerificationEmailSent());
        resetAllMocks();
    }

    @Test
    public void testRegisterUser() throws Exception{
        RegistrationOrLoginBehavior registrationOrLoginBehavior = new UspRegistrationOrLoginBehavior();
        registrationOrLoginBehavior.setRedirectUrl("something");
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();

        //Empty command
        BindingResult bindingResult = new BeanPropertyBindingResult(_userRegistrationCommand, "userRegistrationCommand");
        UserRegistrationOrLoginService.Summary summary = _service.registerUser(_userRegistrationCommand, registrationOrLoginBehavior, bindingResult, request);

        assertNull("User email, password ,first name etc are not set in the command.Hence cannot register user.", summary);
        assertTrue("There is no email, password, first name etc in the command.Hence errors.", bindingResult.hasErrors());
        resetAllMocks();

        //Invalid email,password etc.
        bindingResult = new BeanPropertyBindingResult(_userRegistrationCommand, "userRegistrationCommand");
        _userRegistrationCommand.setEmail("asd");
        _userRegistrationCommand.setPassword("a");
        _userRegistrationCommand.setConfirmPassword("as");
        _userRegistrationCommand.setFirstName("a");

        summary = _service.registerUser(_userRegistrationCommand, registrationOrLoginBehavior, bindingResult, request);
        assertNull("Invalid email, password ,first name etc in the command.Hence cannot register user.", summary);
        assertTrue("Invalid email, password ,first name etc in the command.Hence errors.", bindingResult.hasErrors());
        resetAllMocks();

        //User already exists.User is email provisional.
        bindingResult = new BeanPropertyBindingResult(_userRegistrationCommand, "_userRegistrationCommand");
        _userRegistrationCommand.setEmail("someuser@somedomain.com");
        _userRegistrationCommand.setPassword("password");
        _userRegistrationCommand.setConfirmPassword(_userRegistrationCommand.getPassword());
        _userRegistrationCommand.setFirstName("somename");
        _userRegistrationCommand.setHow("Usp");
        _userRegistrationCommand.setTerms(true);

        User user1 = new User();
        user1.setId(1);
        user1.setEmail("someuser@somedomain.com");
        user1.setPlaintextPassword("password");
        user1.setEmailVerified(false);
        user1.setEmailProvisional("password");
        expect(_userDao.findUserFromEmailIfExists(_userRegistrationCommand.getEmail())).andReturn(user1);

        replayAllMocks();
        summary = _service.registerUser(_userRegistrationCommand, registrationOrLoginBehavior, bindingResult, request);
        verifyAllMocks();
        assertNotNull("Valid user.", summary.getUser());
        assertFalse("User is not email validated.", summary.getUser().isEmailValidated());
        assertFalse("User already exists.", summary.wasUserRegistered());
        resetAllMocks();

        //User already exists.But the user is Email only user. Therefore register the user.
        bindingResult = new BeanPropertyBindingResult(_userRegistrationCommand, "_userRegistrationCommand");
        _userRegistrationCommand.setEmail("someuser@somedomain.com");
        _userRegistrationCommand.setPassword("password");
        _userRegistrationCommand.setConfirmPassword(_userRegistrationCommand.getPassword());
        _userRegistrationCommand.setFirstName("somename");
        _userRegistrationCommand.setHow("Usp");
        _userRegistrationCommand.setTerms(true);

        user1 = new User();
        user1.setId(1);
        user1.setEmail("someuser@somedomain.com");
        expect(_userDao.findUserFromEmailIfExists(_userRegistrationCommand.getEmail())).andReturn(user1);

        _service.setAttributesOnUser(user1,_userRegistrationCommand, registrationOrLoginBehavior);
        _userDao.saveUser(user1);
        expect(_userDao.findUserFromId(user1.getId())).andReturn(user1);
        _userDao.updateUser(user1);
        _userDao.updateUser(user1);
        try {
            _emailVerificationEmail.sendVerificationEmail(request, user1, registrationOrLoginBehavior.getRedirectUrl(), null);
        } catch (Exception ex) {

        }

        replayAllMocks();
        summary = _service.registerUser(_userRegistrationCommand, registrationOrLoginBehavior, bindingResult, request);
        verifyAllMocks();
        assertNotNull("Valid user.", summary.getUser());
        assertFalse("User is not email validated.", summary.getUser().isEmailValidated());
        assertTrue("User is email only user. Therefore registered the user.", summary.wasUserRegistered());
        resetAllMocks();

        //User does not already exist.Therefore register user.
        bindingResult = new BeanPropertyBindingResult(_userRegistrationCommand, "_userRegistrationCommand");

        expect(_userDao.findUserFromEmailIfExists(_userRegistrationCommand.getEmail())).andReturn(null);
        User user2 = _service.createNewUser(_userRegistrationCommand, registrationOrLoginBehavior);
        _userDao.saveUser(user2);
        expect(_userDao.findUserFromId(user2.getId())).andReturn(user2);
        _userDao.updateUser(user2);
        _userDao.updateUser(user2);
        try {
            _emailVerificationEmail.sendVerificationEmail(request, user2, registrationOrLoginBehavior.getRedirectUrl(), null);
        } catch (Exception ex) {

        }

        replayAllMocks();
        summary = _service.registerUser(_userRegistrationCommand, registrationOrLoginBehavior, bindingResult, request);
        verifyAllMocks();
        assertNotNull("Valid user.", summary.getUser());
        assertFalse("User is not email validated.", summary.getUser().isEmailValidated());
        assertTrue("New user. Therefore registered the user.", summary.wasUserRegistered());
        resetAllMocks();
    }

    @Test
    public void testRegisterUserWithNoFirstName() throws Exception{
        RegistrationOrLoginBehavior registrationOrLoginBehavior = new UspRegistrationOrLoginBehavior();
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();
        BindingResult bindingResult;
        User user;

        bindingResult = new BeanPropertyBindingResult(_userRegistrationCommand, "_userRegistrationCommand");
        registrationOrLoginBehavior.setRedirectUrl("something");

        _userRegistrationCommand = RegistrationTestUtils.validUserRegistrationCommand()
            .firstName(null);

        expect(_userDao.findUserFromEmailIfExists(_userRegistrationCommand.getEmail())).andReturn(null);

        User user2 = _service.createNewUser(_userRegistrationCommand, registrationOrLoginBehavior);

        _userDao.saveUser(user2);

        expect(_userDao.findUserFromId(user2.getId())).andReturn(user2);

        _userDao.updateUser(user2);
        _userDao.updateUser(user2);
        try {
            _emailVerificationEmail.sendVerificationEmail(request, user2, registrationOrLoginBehavior.getRedirectUrl(), null);
        } catch (Exception ex) {

        }

        replayAllMocks();
        UserRegistrationOrLoginService.Summary summary = _service.registerUser(_userRegistrationCommand, registrationOrLoginBehavior, bindingResult, request);
        verifyAllMocks();
        assertNotNull("Valid user.", summary.getUser());
        assertFalse("User is not email validated.", summary.getUser().isEmailValidated());
        resetAllMocks();
    }

    @Test
    public void testRegisterUserWithFacebookId() throws Exception{
        RegistrationOrLoginBehavior registrationOrLoginBehavior = new UspRegistrationOrLoginBehavior();
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();
        BindingResult bindingResult;
        User user;

        bindingResult = new BeanPropertyBindingResult(_userRegistrationCommand, "_userRegistrationCommand");
        registrationOrLoginBehavior.setRedirectUrl("something");
        registrationOrLoginBehavior.setFbSignedRequest("bogus");

        _userRegistrationCommand = RegistrationTestUtils.validUserRegistrationCommand()
            .facebookId("bogus")
            .password(null)
            .confirmPassword(null)
            .firstName("Timmy");

        expect(_userDao.findUserFromEmailIfExists(_userRegistrationCommand.getEmail())).andReturn(null);

        User user2 = _service.createNewUser(_userRegistrationCommand, registrationOrLoginBehavior);
        user2.setEmailVerified(true);

        _userDao.saveUser(user2);

        expect(_userDao.findUserFromId(user2.getId())).andReturn(user2);

        // happens after user profile is created
        _userDao.updateUser(user2);
        _userDao.updateUser(user2);

        try {
            _emailVerificationEmail.sendVerificationEmail(request, user2, registrationOrLoginBehavior.getRedirectUrl(), null);
        } catch (Exception ex) {

        }

        replayAllMocks();
        UserRegistrationOrLoginService.Summary summary = _service.registerUser(_userRegistrationCommand, registrationOrLoginBehavior, bindingResult, request);
        verifyAllMocks();
        assertNotNull("Expect valid user.", summary.getUser());
        assertTrue("Expect user email to be flagged validated", summary.getUser().getEmailVerified());
        assertEquals("Expect user facebookId to be correctly set", "bogus", summary.getUser().getFacebookId());
        assertNotNull("Expect user password to be set to random password", summary.getUser().getPasswordMd5());
        resetAllMocks();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterUserWithException() throws Exception{
        RegistrationOrLoginBehavior registrationOrLoginBehavior = new UspRegistrationOrLoginBehavior();
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
        registrationOrLoginBehavior.setRedirectUrl("something");
        _service = new UserRegistrationOrLoginService();
        _service.setValidatorFactory(_validatorFactory);
        _service.setUserDao(_userDao);

        expect(_userDao.findUserFromEmailIfExists(_userRegistrationCommand.getEmail())).andReturn(null);
        _userDao.saveUser(isA(User.class));
        //Make sure the user is removed.
        _userDao.removeUser(null);

        replayAllMocks();
        UserRegistrationOrLoginService.Summary summary= _service.registerUser(_userRegistrationCommand, registrationOrLoginBehavior, bindingResult, request);
        verifyAllMocks();
        assertNull("Valid user.", summary.getUser());
        resetAllMocks();
    }

    @Test
    public void testConvertToFacebookAccountIfNeeded_noExistingFacebookId() throws Exception {
        RegistrationOrLoginBehavior registrationOrLoginBehavior = new UspRegistrationOrLoginBehavior();
        registrationOrLoginBehavior.setFbSignedRequest("bogus");

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

        replayAllMocks();
        org.easymock.classextension.EasyMock.replay(facebookSession);

        boolean result = _service.convertToFacebookAccountIfNeeded(user, getRequest());

        verifyAllMocks();
        org.easymock.classextension.EasyMock.verify(facebookSession);

        assertEquals("Expect user's facebook ID to have been set", "bogusUserId", user.getFacebookId());
        assertEquals("Expect user's email to still be verified", Boolean.TRUE, user.getEmailVerified());
        assertFalse("Expect user to still not be provisional", user.isEmailProvisional());
        assertTrue("Expect user to have been modified", result);
    }

    @Test
    public void testConvertToFacebookAccountIfNeeded_emailNotAlreadyVerified() throws Exception {
        RegistrationOrLoginBehavior registrationOrLoginBehavior = new UspRegistrationOrLoginBehavior();
        registrationOrLoginBehavior.setFbSignedRequest("bogus");

        FacebookSession facebookSession = org.easymock.classextension.EasyMock.createStrictMock(FacebookSession.class);
        getRequest().setAttribute(FacebookSession.REQUEST_ATTRIBUTE, facebookSession);

        _userLoginCommand.setEmail("someuser@somedomain.com");

        User user = new User();
        user.setId(1);
        user.setEmail("someuser@somedomain.com");
        user.setEmailVerified(false);
        user.setFacebookId(null);

        org.easymock.classextension.EasyMock.expect(facebookSession.getUserId()).andReturn("bogusUserId");

        replayAllMocks();
        org.easymock.classextension.EasyMock.replay(facebookSession);

        boolean result = _service.convertToFacebookAccountIfNeeded(user, getRequest());

        verifyAllMocks();
        org.easymock.classextension.EasyMock.verify(facebookSession);

        assertEquals("Expect user's facebook ID to have been set", "bogusUserId", user.getFacebookId());
        assertTrue("Expect user's email to have been marked verified", user.getEmailVerified());
        assertTrue("Expect user to have been modified", result);
    }

    @Test
    public void testConvertToFacebookAccountIfNeeded_withProvisionalEmail() throws Exception {
        RegistrationOrLoginBehavior registrationOrLoginBehavior = new UspRegistrationOrLoginBehavior();
        registrationOrLoginBehavior.setFbSignedRequest("bogus");

        FacebookSession facebookSession = org.easymock.classextension.EasyMock.createStrictMock(FacebookSession.class);
        getRequest().setAttribute(FacebookSession.REQUEST_ATTRIBUTE, facebookSession);

        _userLoginCommand.setEmail("someuser@somedomain.com");

        User user = new User();
        user.setId(1);
        user.setEmail("someuser@somedomain.com");
        user.setPlaintextPassword("abc");
        user.setEmailProvisional("abc");

        org.easymock.classextension.EasyMock.expect(facebookSession.getUserId()).andReturn("bogusUserId");

        replayAllMocks();
        org.easymock.classextension.EasyMock.replay(facebookSession);

        boolean result = _service.convertToFacebookAccountIfNeeded(user, getRequest());

        verifyAllMocks();
        org.easymock.classextension.EasyMock.verify(facebookSession);

        assertEquals("Expect user's facebook ID to have been set", "bogusUserId", user.getFacebookId());
        assertFalse("Expect user to no longer be provisional", user.isEmailProvisional());
        assertTrue("Expect user to have been modified", result);
    }

    @Test
    public void testConvertToFacebookAccountIfNeeded_noModificationsNeeded() throws Exception {
        RegistrationOrLoginBehavior registrationBehavior = new RegistrationOrLoginBehavior();
        registrationBehavior.setFbSignedRequest("bogus");

        FacebookSession facebookSession = org.easymock.classextension.EasyMock.createStrictMock(FacebookSession.class);
        getRequest().setAttribute(FacebookSession.REQUEST_ATTRIBUTE, facebookSession);

        _userLoginCommand.setEmail("someuser@somedomain.com");

        User user = new User();
        user.setId(1);
        user.setEmail("someuser@somedomain.com");
        user.setPlaintextPassword("abc");
        user.setEmailProvisional("abc");
        user.setEmailValidated();
        user.setEmailVerified(true);
        user.setFacebookId("bogusUserId");

        replayAllMocks();
        org.easymock.classextension.EasyMock.replay(facebookSession);

        boolean result = _service.convertToFacebookAccountIfNeeded(user, getRequest());

        verifyAllMocks();
        org.easymock.classextension.EasyMock.verify(facebookSession);

        assertEquals("Expect user's facebook ID to still be set", "bogusUserId", user.getFacebookId());
        assertFalse("Expect user to still not be provisional", user.isEmailProvisional());
        assertFalse("Expect user to have not been modified", result);
    }

    @Test
    public void testLoginFacebookUser_existingFacebookUser() throws Exception {
        RegistrationOrLoginBehavior registrationBehavior = new RegistrationOrLoginBehavior();
        registrationBehavior.setFbSignedRequest("bogus");

        FacebookSession facebookSession = org.easymock.classextension.EasyMock.createMock(FacebookSession.class);
        getRequest().setAttribute(FacebookSession.REQUEST_ATTRIBUTE, facebookSession);

        _userLoginCommand.setEmail("someuser@somedomain.com");

        User user = RegistrationTestUtils.facebookUser();
        user.setEmail("someuser@somedomain.com");
        user.setPlaintextPassword("abc");
        user.setEmailProvisional("abc");
        user.setEmailValidated();
        user.setEmailVerified(true);

        expect(_userDao.findUserFromEmailIfExists("someuser@somedomain.com")).andReturn(user);
        org.easymock.classextension.EasyMock.expect(facebookSession.isOwnedBy(user)).andReturn(true);

        replayAllMocks();
        org.easymock.classextension.EasyMock.replay(facebookSession);

        _service.loginFacebookUser(_userLoginCommand, _request, _response);

        verifyAllMocks();
        org.easymock.classextension.EasyMock.verify(facebookSession);

        assertNotNull("Expect MEMID cookie to have been set", _response.getCookie("MEMID"));
        assertEquals("Expect facebookId to still be set", "facebookId", user.getFacebookId());
    }

    @Test
    public void testLoginFacebookUser_existingRegularUser() throws Exception {
        RegistrationOrLoginBehavior registrationBehavior = new RegistrationOrLoginBehavior();
        registrationBehavior.setFbSignedRequest("bogus");

        FacebookSession facebookSession = org.easymock.classextension.EasyMock.createMock(FacebookSession.class);
        getRequest().setAttribute(FacebookSession.REQUEST_ATTRIBUTE, facebookSession);

        _userLoginCommand.setEmail("someuser@somedomain.com");

        User user = new User();
        user.setId(1);
        user.setEmail("someuser@somedomain.com");
        user.setPlaintextPassword("abc");
        user.setEmailProvisional("abc");
        user.setEmailValidated();
        user.setEmailVerified(true);

        expect(_userDao.findUserFromEmailIfExists("someuser@somedomain.com")).andReturn(user);
        _userDao.saveUser(user);
        expect(facebookSession.getUserId()).andReturn("facebookId");

        replayAllMocks();
        org.easymock.classextension.EasyMock.replay(facebookSession);

        _service.loginFacebookUser(_userLoginCommand, _request, _response);

        verifyAllMocks();
        org.easymock.classextension.EasyMock.verify(facebookSession);

        assertNotNull("Expect MEMID cookie to have been set", _response.getCookie("MEMID"));
        assertEquals("Expect facebookId to have been set on user", "facebookId", user.getFacebookId());
    }

    @Test
    public void testLoginFacebookUser_wrongEmail() throws Exception {
        RegistrationOrLoginBehavior registrationBehavior = new RegistrationOrLoginBehavior();
        registrationBehavior.setFbSignedRequest("bogus");

        FacebookSession facebookSession = org.easymock.classextension.EasyMock.createMock(FacebookSession.class);
        getRequest().setAttribute(FacebookSession.REQUEST_ATTRIBUTE, facebookSession);

        _userLoginCommand.setEmail("someuser@somedomain.com");

        expect(_userDao.findUserFromEmailIfExists("someuser@somedomain.com")).andReturn(null);

        replayAllMocks();
        org.easymock.classextension.EasyMock.replay(facebookSession);

        _service.loginFacebookUser(_userLoginCommand, _request, _response);

        verifyAllMocks();
        org.easymock.classextension.EasyMock.verify(facebookSession);

        assertNull("Expect MEMID cookie to have NOT been set", _response.getCookie("MEMID"));
    }

    @Test
    public void testLoginFacebookUser_wrongFacebookId() throws Exception {
        RegistrationOrLoginBehavior registrationBehavior = new RegistrationOrLoginBehavior();
        registrationBehavior.setFbSignedRequest("bogus");

        FacebookSession facebookSession = org.easymock.classextension.EasyMock.createMock(FacebookSession.class);
        getRequest().setAttribute(FacebookSession.REQUEST_ATTRIBUTE, facebookSession);

        _userLoginCommand.setEmail("someuser@somedomain.com");

        User user = RegistrationTestUtils.facebookUser();
        user.setEmail("someuser@somedomain.com");
        user.setPlaintextPassword("abc");
        user.setEmailProvisional("abc");
        user.setEmailValidated();
        user.setEmailVerified(true);

        expect(_userDao.findUserFromEmailIfExists("someuser@somedomain.com")).andReturn(user);
        org.easymock.classextension.EasyMock.expect(facebookSession.isOwnedBy(user)).andReturn(false);

        replayAllMocks();
        org.easymock.classextension.EasyMock.replay(facebookSession);

        _service.loginFacebookUser(_userLoginCommand, _request, _response);

        verifyAllMocks();
        org.easymock.classextension.EasyMock.verify(facebookSession);

        assertNull("Expect MEMID cookie to have NOT been set", _response.getCookie("MEMID"));
    }
}