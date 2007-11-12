package gs.web.community.registration;

import gs.data.community.*;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.data.util.email.MockJavaMailSender;
import gs.data.geo.IGeoDao;
import gs.data.soap.CreateOrUpdateUserRequest;
import gs.data.soap.CreateOrUpdateUserRequestBean;
import gs.data.soap.SoapRequestException;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.easymock.MockControl;
import static org.easymock.classextension.EasyMock.*;
import org.easymock.classextension.MockClassControl;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * Provides ...
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RegistrationControllerTest extends BaseControllerTestCase {
    private RegistrationController _controller;

    private IUserDao _userDao;
    private MockControl _userControl;
    private IGeoDao _geoDao;
    private MockControl _geoControl;
    private MockJavaMailSender _mailSender;

    private static final String SUCCESS_VIEW = "/community/registration/registrationSuccess";
    private MockControl _subscriptionDaoMock;
    private ISubscriptionDao _subscriptionDao;
    private MockControl _authenticationManagerMock;
    private AuthenticationManager _authenticationManager;
    private CreateOrUpdateUserRequest _soapRequest;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new RegistrationController();
        _mailSender = new MockJavaMailSender();
        // have to set host else the mock mail sender will throw an exception
        // actual value is irrelevant
        _mailSender.setHost("greatschools.net");
        _controller.setMailSender(_mailSender);
        _geoControl = MockControl.createControl(IGeoDao.class);
        _geoDao = (IGeoDao) _geoControl.getMock();
        _userControl = MockControl.createControl(IUserDao.class);
        _userDao = (IUserDao) _userControl.getMock();
        _subscriptionDaoMock = MockControl.createControl(ISubscriptionDao.class);
        _subscriptionDao = (ISubscriptionDao) _subscriptionDaoMock.getMock();

        _authenticationManagerMock = MockClassControl.createNiceControl(AuthenticationManager.class);
        _authenticationManager = (AuthenticationManager) _authenticationManagerMock.getMock();
        _authenticationManagerMock.replay();

        _soapRequest = createMock(CreateOrUpdateUserRequest.class);
        _controller.setSoapRequest(_soapRequest);

        _controller.setGeoDao(_geoDao);
        _controller.setUserDao(_userDao);
        _controller.setSuccessView(SUCCESS_VIEW);
        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.setAuthenticationManager(_authenticationManager);

        RegistrationConfirmationEmail email = (RegistrationConfirmationEmail)
                getApplicationContext().getBean(RegistrationConfirmationEmail.BEAN_ID);
        email.getEmailHelperFactory().setMailSender(_mailSender);
        _controller.setRegistrationConfirmationEmail(email);
    }

    /**
     * Test successful registration with a new user
     *
     * @throws Exception
     */
    public void testRegistration() throws Exception {
        UserCommand userCommand = new UserCommand();
        BindException errors = new BindException(userCommand, "");
        String email = "testRegistration@RegistrationControllerTest.com";
        String password = "foobar";
        userCommand.setEmail(email);
        userCommand.setPassword(password);
        userCommand.setConfirmPassword(password);
        userCommand.setState(State.CA);
        userCommand.setScreenName("screeny");
        userCommand.setNumSchoolChildren(0);
        userCommand.setNewsletter(false);

        userCommand.getUser().setId(345); // to fake the database save

        _userControl.expectAndReturn(_userDao.findUserFromEmailIfExists(email), null);
        _userDao.saveUser(userCommand.getUser());
        _userDao.updateUser(userCommand.getUser());
        _userDao.updateUser(userCommand.getUser());
        _userControl.replay();

        getRequest().addParameter("next", "next"); // submit button for 2-step process

        // no calls expected if "next" is clicked
        _subscriptionDaoMock.replay();

        _soapRequest.createOrUpdateUserRequest(isA(CreateOrUpdateUserRequestBean.class));
        replay(_soapRequest);
        
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), userCommand, errors);
        _userControl.verify();
        _subscriptionDaoMock.verify();
        verify(_soapRequest);
    }

    public void testRegistrationSubscribesToCommunityNewsletter() throws Exception {
        UserCommand userCommand = new UserCommand();
        userCommand.setEmail("a");
        userCommand.getUser().setId(new Integer(345)); // to fake the database save
        userCommand.setPassword("test");
        userCommand.setNumSchoolChildren(new Integer(0));
        userCommand.setState(State.GA);

        Subscription newsletterSubscription = new Subscription();
        newsletterSubscription.setUser(userCommand.getUser());
        newsletterSubscription.setProduct(SubscriptionProduct.COMMUNITY);
        newsletterSubscription.setState(State.GA);

        getRequest().addParameter("join", "join"); // submit button

        _subscriptionDao.addNewsletterSubscriptions((User)notNull(), (List)notNull());
        replay(_subscriptionDao);

        // user dao behavior is validated elsewhere
        setUpNiceUserDao();

        _soapRequest.createOrUpdateUserRequest(isA(CreateOrUpdateUserRequestBean.class));
        replay(_soapRequest);

        userCommand.setNewsletter(true);
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), userCommand, null);

        verify(_subscriptionDao);
        verify(_soapRequest);
    }

    public void testRegistrationSubscribesToBeta() throws Exception {
        UserCommand userCommand = new UserCommand();
        userCommand.setEmail("a");
        userCommand.getUser().setId(345); // to fake the database save
        userCommand.setPassword("test");
        userCommand.setNumSchoolChildren(0);
        userCommand.setState(State.GA);

        Subscription betaSubscription = new Subscription();
        betaSubscription.setUser(userCommand.getUser());
        betaSubscription.setProduct(SubscriptionProduct.BETA_GROUP);
        betaSubscription.setState(State.GA);

        expect(_subscriptionDao.getUserSubscriptions(userCommand.getUser(), SubscriptionProduct.BETA_GROUP))
                .andReturn(null);
        _subscriptionDao.saveSubscription(betaSubscription);
        replay(_subscriptionDao);

        // user dao behavior is validated elsewhere
        setUpNiceUserDao();

        _soapRequest.createOrUpdateUserRequest(isA(CreateOrUpdateUserRequestBean.class));
        replay(_soapRequest);

        userCommand.setBeta(true);
        userCommand.setNewsletter(false);
        _controller.onSubmit(getRequest(), getResponse(), userCommand, null);

        verify(_subscriptionDao);
        verify(_soapRequest);
    }

    public void testRegistrationDoesntSubscribeToBetaIfAlreadySubscribed() throws Exception {
        UserCommand userCommand = new UserCommand();
        userCommand.setEmail("a");
        userCommand.getUser().setId(345); // to fake the database save
        userCommand.setPassword("test");
        userCommand.setNumSchoolChildren(0);
        userCommand.setState(State.GA);

        expect(_subscriptionDao.getUserSubscriptions(userCommand.getUser(), SubscriptionProduct.BETA_GROUP))
                .andReturn(new ArrayList<Subscription>());
        // no subscription saved
        replay(_subscriptionDao);

        // user dao behavior is validated elsewhere
        setUpNiceUserDao();

        _soapRequest.createOrUpdateUserRequest(isA(CreateOrUpdateUserRequestBean.class));
        replay(_soapRequest);

        userCommand.setBeta(true);
        userCommand.setNewsletter(false);
        _controller.onSubmit(getRequest(), getResponse(), userCommand, null);

        verify(_subscriptionDao);
        verify(_soapRequest);
    }

    public void testRegistrationDoesNotSubscribeToCommunityNewsletter() throws Exception {
        UserCommand userCommand = new UserCommand();
        userCommand.setEmail("a");
        userCommand.getUser().setId(new Integer(345)); // to fake the database save
        userCommand.setPassword("test");
        userCommand.setNumSchoolChildren(new Integer(0));

        getRequest().addParameter("join", "join"); // submit button

        // no calls expected
        _subscriptionDaoMock.replay();

        // user dao behavior is validated elsewhere
        setUpNiceUserDao();

        _soapRequest.createOrUpdateUserRequest(isA(CreateOrUpdateUserRequestBean.class));
        replay(_soapRequest);

        userCommand.setNewsletter(false);
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), userCommand, null);

        _subscriptionDaoMock.verify();
        verify(_soapRequest);
    }


    /**
     * Test successful registration with an existing user
     *
     * @throws NoSuchAlgorithmException
     */
    public void testExistingUser() throws Exception {
        String email = "testExistingUser@greatschools.net";
        Integer userId = new Integer(346);

        UserCommand userCommand = new UserCommand();
        BindException errors = new BindException(userCommand, "");
        String password = "foobar";
        userCommand.getUser().setEmail(email);

        userCommand.setPassword(password);
        userCommand.setConfirmPassword(password);
        userCommand.setScreenName("screeny");
        userCommand.setNumSchoolChildren(new Integer(0));
        userCommand.getUser().setId(userId);

        assertTrue(userCommand.getUser().isPasswordEmpty());
        assertFalse(userCommand.getUser().isEmailProvisional());

        _userControl.expectAndReturn(_userDao.findUserFromEmailIfExists(email),
                userCommand.getUser());
        _userDao.updateUser(userCommand.getUser());
        _userDao.updateUser(userCommand.getUser());
        _userControl.replay();

        _soapRequest.createOrUpdateUserRequest(isA(CreateOrUpdateUserRequestBean.class));
        replay(_soapRequest);

        try {
            _controller.onSubmit(getRequest(), getResponse(), userCommand, errors);
            _userControl.verify();
            verify(_soapRequest);
            assertTrue(userCommand.getUser().isEmailProvisional());
            assertFalse(userCommand.getUser().isPasswordEmpty());
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    /**
     * Regression testing for GS-4065
     */
    public void testExistingUserWithOtherGender() throws Exception {
        String email = "testExistingUser@greatschools.net";
        Integer userId = 346;

        UserCommand userCommand = new UserCommand();
        BindException errors = new BindException(userCommand, "");
        String password = "foobar";
        userCommand.getUser().setEmail(email);
        userCommand.setGender("u");

        userCommand.setPassword(password);
        userCommand.setConfirmPassword(password);
        userCommand.setScreenName("screeny");
        userCommand.setNumSchoolChildren(0);
        userCommand.getUser().setId(userId);

        assertTrue(userCommand.getUser().isPasswordEmpty());
        assertFalse(userCommand.getUser().isEmailProvisional());

        User dbUser = new User();
        dbUser.setId(userId);
        dbUser.setEmail(email);

        _userControl.expectAndReturn(_userDao.findUserFromEmailIfExists(email),
                dbUser);
        _userDao.updateUser(dbUser);
        _userDao.updateUser(dbUser);
        _userControl.replay();

        _soapRequest.createOrUpdateUserRequest(isA(CreateOrUpdateUserRequestBean.class));
        replay(_soapRequest);

        try {
            _controller.onSubmit(getRequest(), getResponse(), userCommand, errors);
            _userControl.verify();
            verify(_soapRequest);
            assertTrue(userCommand.getUser().isEmailProvisional());
            assertFalse(userCommand.getUser().isPasswordEmpty());
            // following line is test for GS-4065
            assertEquals("Expect gender to be set on existing user", "u", dbUser.getGender());
        } catch (Exception e) {
            fail(e.toString());
        }
    }


    /**
     * Test that on serious error during the registration process, no partially completed records
     * are left in the database.
     */
    public void testRegistrationFailureOnNewUser() throws SoapRequestException {
        UserCommand userCommand = new UserCommand();
        BindException errors = new BindException(userCommand, "");
        String email = "testRegistrationFailureOnNewUser@RegistrationControllerTest.com";
        String password = "foobar";
        Integer userId = new Integer(347);
        userCommand.setEmail(email);
        userCommand.setPassword(password);
        userCommand.setConfirmPassword(password);
        userCommand.getUser().setId(userId);

        _userControl.expectAndReturn(_userDao.findUserFromEmailIfExists(email), null);
        _userDao.saveUser(userCommand.getUser());
        _userDao.updateUser(userCommand.getUser());
        _userDao.removeUser(userId);
        _userControl.replay();

        // set the mock mail sender to throw an exception
        _mailSender.setThrowOnSendMessage(true);

        replay(_soapRequest);

        try {
            _controller.onSubmit(getRequest(), getResponse(), userCommand, errors);
            fail("Expected mail exception not thrown");
        } catch (Exception ex) {
            _userControl.verify();
            verify(_soapRequest);
        } finally {
            _mailSender.setThrowOnSendMessage(false);
        }
    }

    /**
     * Test that on serious error during the registration process, no partially completed records
     * are left in the database.
     */
    public void testRegistrationFailureOnExistingUser() {
        UserCommand userCommand = new UserCommand();
        BindException errors = new BindException(userCommand, "");
        String email = "testRegistrationFailureOnExistingUser@greatschools.net";
        String password = "foobar";
        Integer userId = new Integer(348);
        userCommand.setEmail(email);
        userCommand.setPassword(password);
        userCommand.setConfirmPassword(password);
        userCommand.getUser().setId(userId);

        User user = userCommand.getUser();
        assertTrue(user.isPasswordEmpty());

        _userControl.expectAndReturn(_userDao.findUserFromEmailIfExists(email), user);
        _userDao.updateUser(userCommand.getUser());
        _userDao.updateUser(userCommand.getUser());
        _userControl.replay();

        // set the mock mail sender to throw an exception
        _mailSender.setThrowOnSendMessage(true);
        try {
            _controller.onSubmit(getRequest(), getResponse(), userCommand, errors);
            fail("Expected mail exception not thrown");
        } catch (Exception e) {
            _userControl.verify();
            assertTrue(user.isPasswordEmpty());
        } finally {
            _mailSender.setThrowOnSendMessage(false);
        }
    }

    public void testOnBindWithoutTermsOrNewsletter() throws Exception {
        UserCommand userCommand = new UserCommand();
        _geoControl.expectAndReturn(_geoDao.findCitiesByState(State.CA), new ArrayList());
        _geoControl.replay();

        getRequest().setParameter("city", "San Francisco");
        getRequest().setParameter(RegistrationController.TERMS_PARAMETER, "n");
        getRequest().setParameter(RegistrationController.NEWSLETTER_PARAMETER, "n");
        _controller.onBind(getRequest(), userCommand);
        assertFalse(userCommand.getTerms());
        assertFalse("Expected newsletter to be set to false", userCommand.getNewsletter());
    }

    public void testOnBindWithTermsAndNewsletter() throws Exception {
        UserCommand userCommand = new UserCommand();
        _geoControl.expectAndReturn(_geoDao.findCitiesByState(State.CA), new ArrayList());
        _geoControl.replay();

        getRequest().setParameter(RegistrationController.TERMS_PARAMETER, "on");
        getRequest().setParameter(RegistrationController.NEWSLETTER_PARAMETER, "on");
        _controller.onBind(getRequest(), userCommand);
        assertTrue("Expected terms to be set to true", userCommand.getTerms());
        assertTrue("Expected newsletter to be set to true", userCommand.getNewsletter());
    }

    public void testOnBindWithoutNewsletterParameter() throws Exception {
        UserCommand userCommand = new UserCommand();
        _geoControl.expectAndReturn(_geoDao.findCitiesByState(State.CA), new ArrayList());
        _geoControl.replay();

        _controller.onBind(getRequest(), userCommand);
        assertFalse("Newsletter should be set to false if newsletterStr is not passed", userCommand.getNewsletter());
    }

    public void testOnBindWithBetaParameter() throws Exception {
        UserCommand userCommand = new UserCommand();
        _geoControl.expectAndReturn(_geoDao.findCitiesByState(State.CA), new ArrayList());
        _geoControl.replay();

        assertFalse("Expected beta to default to false", userCommand.isBeta());
        getRequest().setParameter(RegistrationController.BETA_PARAMETER, "on");
        _controller.onBind(getRequest(), userCommand);
        assertTrue("Expected beta to be set to true", userCommand.isBeta());
    }

    public void testOnBindAndValidate() throws Exception {
        UserCommand userCommand = new UserCommand();
        BindException errors = new BindException(userCommand, "");
        _controller.onBindAndValidate(getRequest(), userCommand, errors);
        assertEquals(7, errors.getErrorCount());
    }

    public void testOnBindOnNewForm() throws Exception {
        UserCommand userCommand = new UserCommand();
        userCommand.setState(null);
        BindException errors = new BindException(userCommand, "");

        // Test with no user
        _geoControl.expectAndReturn(_geoDao.findCitiesByState(State.CA), new ArrayList(), 4);
        _geoControl.replay();
        _controller.onBindOnNewForm(getRequest(), userCommand, errors);
        assertNull(userCommand.getEmail());

        // Test with user found, make sure it clears first
        String email = "testRegistrationFailureOnExistingUser@greatschools.net";
        String password = "foobar";
        Integer userId = new Integer(348);
        userCommand.setEmail(email);
        userCommand.setPassword(password);
        userCommand.setConfirmPassword(password);
        userCommand.setState(State.CA);
        userCommand.getUser().setId(userId);
        userCommand.setFirstName("X");
        userCommand.setLastName("Y");
        User user = userCommand.getUser();
        _userControl.expectAndReturn(_userDao.findUserFromEmailIfExists(email), user, 2);
        _userControl.expectAndReturn(_userDao.findUserFromEmailIfExists(email), null);
        _userDao.evict(user);
        _userControl.setVoidCallable(2);
        _userDao.updateUser(user);
        _userControl.replay();
        _controller.onBindOnNewForm(getRequest(), userCommand, errors);
        assertNull(userCommand.getFirstName());
        assertNull(userCommand.getLastName());

        // Test provisional password reset
        user.setPlaintextPassword("test");
        user.setEmailProvisional("test");
        assertNotNull(user.getPasswordMd5());
        getRequest().addParameter("reset", "true");
        _controller.onBindOnNewForm(getRequest(), userCommand, errors);
        assertNull(user.getPasswordMd5());

        // Test with user not found
        _controller.onBindOnNewForm(getRequest(), userCommand, errors);

        _userControl.verify();
        _geoControl.verify();
    }

    public void testNotifyCommunity() {
        try {
            _soapRequest.createOrUpdateUserRequest(isA(CreateOrUpdateUserRequestBean.class));
            replay(_soapRequest);
            _controller.notifyCommunity(1, "myname", "email@example.com", "foobar", new Date(), _request);
            verify(_soapRequest);
        } catch (SoapRequestException e) {
            fail(e.getMessage());
        }
    }

    public void testNotifyCommunityDev() {
        _request.setServerName("dev.greatschools.net");
        try {
            _soapRequest.setTarget("http://community.dev.greatschools.net/soap/user");
            _soapRequest.createOrUpdateUserRequest(isA(CreateOrUpdateUserRequestBean.class));
            replay(_soapRequest);
            _controller.notifyCommunity(1, "myname", "email@example.com", "foobar", new Date(), _request);
            verify(_soapRequest);
        } catch (SoapRequestException e) {
            fail(e.getMessage());
        }
    }

    public void testNotifyCommunityDevWorkstation() {
        _request.setServerName("aroy.office.greatschools.net");
        try {
            _soapRequest.createOrUpdateUserRequest(isA(CreateOrUpdateUserRequestBean.class));
            replay(_soapRequest);
            _controller.notifyCommunity(1, "myname", "email@example.com", "foobar", new Date(), _request);
            verify(_soapRequest);
        } catch (SoapRequestException e) {
            fail(e.getMessage());
        }
    }

    public void testNotifyCommunityStaging() {
        _request.setServerName("staging.greatschools.net");
        try {
            _soapRequest.setTarget("http://community.staging.greatschools.net/soap/user");
            _soapRequest.createOrUpdateUserRequest(isA(CreateOrUpdateUserRequestBean.class));
            replay(_soapRequest);
            _controller.notifyCommunity(1, "myname", "email@example.com", "foobar", new Date(), _request);
            verify(_soapRequest);
        } catch (SoapRequestException e) {
            fail(e.getMessage());
        }
    }

    public void testNotifyCommunityLive() {
        _request.setServerName("www.greatschools.net");
        try {
            _soapRequest.setTarget("http://community.greatschools.net/soap/user");
            _soapRequest.createOrUpdateUserRequest(isA(CreateOrUpdateUserRequestBean.class));
            replay(_soapRequest);
            _controller.notifyCommunity(1, "myname", "email@example.com", "foobar", new Date(), _request);
            verify(_soapRequest);
        } catch (SoapRequestException e) {
            fail(e.getMessage());
        }
    }

    public void testNotifyCommunityLiveCobrand() {
        _soapRequest.setTarget("http://community.greatschools.net/soap/user");
        _request.setServerName("encarta.greatschools.net");
        try {
            _soapRequest.createOrUpdateUserRequest(isA(CreateOrUpdateUserRequestBean.class));
            replay(_soapRequest);
            _controller.notifyCommunity(1, "myname", "email@example.com", "foobar", new Date(), _request);
            verify(_soapRequest);
        } catch (SoapRequestException e) {
            fail(e.getMessage());
        }
    }

    private void setUpNiceUserDao() {
        _userControl = MockControl.createNiceControl(IUserDao.class);
        _userDao = (IUserDao) _userControl.getMock();
        _controller.setUserDao(_userDao);
        _userControl.replay();
    }
}
