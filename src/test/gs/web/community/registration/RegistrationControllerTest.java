package gs.web.community.registration;

import gs.data.community.*;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.BaseControllerTestCase;
import gs.web.tracking.OmnitureTracking;
import gs.web.tracking.CookieBasedOmnitureTracking;
import gs.data.util.email.MockJavaMailSender;
import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableRow;
import gs.data.geo.IGeoDao;
import gs.data.soap.CreateOrUpdateUserRequest;
import gs.data.soap.CreateOrUpdateUserRequestBean;
import gs.data.soap.SoapRequestException;
import gs.data.school.ISchoolDao;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import static org.easymock.classextension.EasyMock.*;

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
    private ISchoolDao _schoolDao;
    private IGeoDao _geoDao;
    private ISubscriptionDao _subscriptionDao;
    private MockJavaMailSender _mailSender;
    private ITableDao _tableDao;

    private static final String SUCCESS_VIEW = "/community/registration/registrationSuccess";
    private CreateOrUpdateUserRequest _soapRequest;

    private UserCommand _command;
    private User _user;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new RegistrationController();
        _mailSender = new MockJavaMailSender();
        // have to set host else the mock mail sender will throw an exception
        // actual value is irrelevant
        _mailSender.setHost("greatschools.net");
        _controller.setMailSender(_mailSender);
        _geoDao = createStrictMock(IGeoDao.class);
        _userDao = createStrictMock(IUserDao.class);
        _schoolDao = createStrictMock(ISchoolDao.class);
        _subscriptionDao = createStrictMock(ISubscriptionDao.class);
        _tableDao = createStrictMock(ITableDao.class);
        _soapRequest = createStrictMock(CreateOrUpdateUserRequest.class);

        _controller.setGeoDao(_geoDao);
        _controller.setUserDao(_userDao);
        _controller.setSchoolDao(_schoolDao);
        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.setTableDao(_tableDao);
        _controller.setSoapRequest(_soapRequest);

        _controller.setSuccessView(SUCCESS_VIEW);

//        GoogleSpreadsheetDaoFactory tableDaoFactory =
//                (GoogleSpreadsheetDaoFactory)getApplicationContext().
//                        getBean("communityRegistrationTableFactory");
//        tableDaoFactory.setGoogleKey("pYwV1uQwaOCKLqeupJ7WqXA");
//        tableDaoFactory.setVisibility("public");
//        tableDaoFactory.setProjection("values");
//        tableDaoFactory.setWorksheetName("od7");

        _controller.setRequireEmailValidation(true);
        _controller.setErrorView("error");
        _controller.setStateManager(new StateManager());
        _controller.setHow("RegistrationControllerTest");
        _controller.setChooserRegistration(false);
        _controller.setHoverView(null);

        _command = new UserCommand();
        _user = new User();
        _command.setUser(_user);
    }
    
    public void testBasics() {
        assertSame(_userDao, _controller.getUserDao());
        assertSame(_geoDao, _controller.getGeoDao());
        assertSame(_schoolDao, _controller.getSchoolDao());
        assertNotNull(_controller.getTableDao());
        assertSame(_subscriptionDao, _controller.getSubscriptionDao());
        assertSame(_mailSender, _controller.getMailSender());
        assertSame(_soapRequest, _controller.getSoapRequest());
        assertSame(_tableDao, _controller.getTableDao());
    }

    public void replayAllMocks() {
        replayMocks(_userDao, _schoolDao, _geoDao, _subscriptionDao, _soapRequest, _tableDao);
    }

    public void verifyAllMocks() {
        verifyMocks(_userDao, _schoolDao, _geoDao, _subscriptionDao, _soapRequest, _tableDao);
    }

    public void resetAllMocks() {
        resetMocks(_userDao, _schoolDao, _geoDao, _subscriptionDao, _soapRequest, _tableDao);
    }

    /**
     * Common set up for   testRegistration(),  testRegistrationWithRedirect() and testRegistrationWithRedirectAndParameters()
     * @param userCommand
     * @throws SoapRequestException
     */
    private void setupBasicOnSubmit(UserCommand userCommand) throws SoapRequestException {
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

        expect(_userDao.findUserFromEmailIfExists(email)).andReturn(null);
        _userDao.saveUser(userCommand.getUser());
        _userDao.updateUser(userCommand.getUser());
        _userDao.updateUser(userCommand.getUser());
        expect(_userDao.findUserFromId(userCommand.getUser().getId())).andReturn(userCommand.getUser());

        getRequest().addParameter("next", "next"); // submit button for 2-step process

        _soapRequest.setTarget("http://community.greatschools.net/soap/user");
        _soapRequest.createOrUpdateUserRequest(isA(CreateOrUpdateUserRequestBean.class));

        expect(_tableDao.getFirstRowByKey("ip", getRequest().getRemoteAddr())).andReturn(null);
    }

    /**
     * Test successful registration with a new user
     *
     * @throws Exception
     */
    public void testBasicOnSubmit() throws Exception {
        BindException errors = new BindException(_command, "");
        setupBasicOnSubmit(_command);
        replayAllMocks();
        _controller.onSubmit(getRequest(), getResponse(), _command, errors);
        verifyAllMocks();
    }

    public void testIsIPBlocked() {
        expect(_tableDao.getFirstRowByKey("ip", getRequest().getRemoteAddr())).andReturn(null);
        replayAllMocks();
        assertFalse(_controller.isIPBlocked(getRequest()));
        verifyAllMocks();

        resetAllMocks();

        expect(_tableDao.getFirstRowByKey("ip", getRequest().getRemoteAddr())).andReturn(createMock(ITableRow.class));
        replayAllMocks();
        assertTrue(_controller.isIPBlocked(getRequest()));
        verifyAllMocks();
    }

    public void testSetUsersPassword() throws Exception {
        _command.setPassword("123456");
        _user.setId(1);

        _userDao.updateUser(_user);

        replayAllMocks();
        assertTrue(_user.isPasswordEmpty());
        _controller.setUsersPassword(_user, _command, true);
        verifyAllMocks();
        assertFalse(_user.isPasswordEmpty());
        assertTrue(_user.isEmailProvisional());
    }

    public void testSetUsersPasswordError() throws Exception {
        _command.setPassword("123456");
        _user.setId(1);

        _userDao.updateUser(_user);
        expectLastCall().andThrow(new RuntimeException("testing error on setting password"));
        _userDao.removeUser(1);

        replayAllMocks();
        assertTrue(_user.isPasswordEmpty());
        try {
            _controller.setUsersPassword(_user, _command, false);
        } catch (Exception e) {
            // ok
        }
        verifyAllMocks();
    }

    public void testSaveSubscriptionsForUser() {
        OmnitureTracking ot = new CookieBasedOmnitureTracking(getRequest(), getResponse());
        _user.setId(1);

        replayAllMocks();
        _controller.saveSubscriptionsForUser(_command, ot);
        verifyAllMocks();

        resetAllMocks();
        Subscription sub = new Subscription();
        _command.addSubscription(sub);
        _subscriptionDao.addNewsletterSubscriptions(same(_user), isA(List.class));
        replayAllMocks();
        _controller.saveSubscriptionsForUser(_command, ot);
        verifyAllMocks();
    }

    public void testProcessNewsletterSubscriptions() {
        _command.setState(State.AK);

        assertEquals(0, _command.getSubscriptions().size());
        replayAllMocks();
        _controller.processNewsletterSubscriptions(_command);
        verifyAllMocks();

        assertEquals(1, _command.getSubscriptions().size());
        List<Subscription> subs = _command.getSubscriptions();
        Subscription sub = subs.get(0);
        assertEquals(SubscriptionProduct.PARENT_ADVISOR, sub.getProduct());
        assertEquals(_user, sub.getUser());
        assertEquals(State.AK, sub.getState());
    }

    public void testIPAddressBlockingWithAttributeOnly() throws Exception {
        BindException errors = new BindException(_command, "");
        setupBasicOnSubmit(_command);
        getRequest().setAttribute("HTTP_X_CLUSTER_CLIENT_IP", "1.2.3.4");
        resetAllMocks();
        expect(_tableDao.getFirstRowByKey("ip", "1.2.3.4")).andReturn(createMock(ITableRow.class));
        replayAllMocks();
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, errors);
        verifyAllMocks();
        assertEquals("Error view should be returned when the request is from a blocked IP.",
                _controller.getErrorView(), mAndV.getViewName());
    }

    public void testIPAddressBlockingWithAttributeUndefined() throws Exception {
        BindException errors = new BindException(_command, "");
        setupBasicOnSubmit(_command);
        getRequest().setAttribute("HTTP_X_CLUSTER_CLIENT_IP", "Undefined");
        getRequest().setRemoteAddr("1.2.3.4");
        resetAllMocks();
        expect(_tableDao.getFirstRowByKey("ip", "1.2.3.4")).andReturn(createMock(ITableRow.class));
        replayAllMocks();
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, errors);
        assertEquals("Error view should be returned when the request is from a blocked IP.",
                _controller.getErrorView(), mAndV.getViewName());
    }

    public void testIPAddressBlockingWithRequestIPOnly() throws Exception {
        BindException errors = new BindException(_command, "");
        setupBasicOnSubmit(_command);
        getRequest().setRemoteAddr("127.0.0.1");
        reset(_tableDao);
        expect(_tableDao.getFirstRowByKey("ip", "127.0.0.1")).andReturn(null);
        replayAllMocks();
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, errors);
        assertTrue ("Request from valid IP address should return non-error viewname",
                mAndV.getViewName().contains("redirect:http://community.greatschools.net"));
    }

    public void testIPAddressBlockingWithNoIP() throws Exception {
        BindException errors = new BindException(_command, "");
        setupBasicOnSubmit(_command);
        getRequest().setRemoteAddr(null);
        reset(_tableDao);
        expect(_tableDao.getFirstRowByKey("ip", null)).andReturn(null);
        replayAllMocks();
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, errors);
        assertTrue ("Request from no IP address should return non-error viewname",
                mAndV.getViewName().contains("redirect:http://community.greatschools.net"));
    }

    public void testIPAddressBlockingWithValidRequestIPandBlockedAttributeIP () throws Exception {
        BindException errors = new BindException(_command, "");
        setupBasicOnSubmit(_command);
        getRequest().setAttribute("HTTP_X_CLUSTER_CLIENT_IP", "1.2.3.4");
        getRequest().setRemoteAddr("127.0.0.1");
        resetAllMocks();
        expect(_tableDao.getFirstRowByKey("ip", "1.2.3.4")).andReturn(createMock(ITableRow.class));
        replayAllMocks();
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), _command, errors);
        assertEquals("Error view should be returned when the request is from a blocked IP.",
                _controller.getErrorView(), mAndV.getViewName());
    }

    public void testNotifyCommunity() {
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

    public void testNotifyCommunityError() {
        _request.setServerName("dev.greatschools.net");
        _soapRequest.setTarget("http://community.dev.greatschools.net/soap/user");
        ModelAndView mAndV = new ModelAndView();
        _user.setUserProfile(_command.getUserProfile());
        _user.setId(1);
        try {
            _user.setPlaintextPassword("123456");
            _soapRequest.createOrUpdateUserRequest(isA(CreateOrUpdateUserRequestBean.class));
            expectLastCall().andThrow(new SoapRequestException());
            replay(_soapRequest);
            _controller.notifyCommunity(_user, _command, mAndV, getRequest());
            verify(_soapRequest);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    // test that if an exception occurs during registration, after user creation, but before
    // community sync, that the user is rolled back to provisional status
    public void testRegistrationWithError() throws Exception {
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
        userCommand.setNewsletter(true);
        _controller.setRequireEmailValidation(false);

        userCommand.getUser().setId(345); // to fake the database save

        expect(_userDao.findUserFromEmailIfExists(email)).andReturn(null);
        _userDao.saveUser(userCommand.getUser());
        _userDao.updateUser(userCommand.getUser()); // setting password
        _userDao.updateUser(userCommand.getUser()); // updating user profile
        expect(_userDao.findUserFromId(userCommand.getUser().getId())).andReturn(userCommand.getUser());
        _userDao.updateUser(userCommand.getUser()); // rolling back to provisional

        // no calls expected if "next" is clicked
        _subscriptionDao.addNewsletterSubscriptions(isA(User.class), isA(List.class));
        expectLastCall().andThrow(new RuntimeException("Some exception that happened during subscription processing"));

        expect(_tableDao.getFirstRowByKey("ip", getRequest().getRemoteAddr())).andReturn(null);

        replayAllMocks();
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), userCommand, errors);
        verifyAllMocks();

        assertTrue("Expect user to be rolled back to provisional status",
                userCommand.getUser().isEmailProvisional());
        assertEquals("Expect error view because of exception", _controller.getErrorView(), mAndV.getViewName());
    }


//    OLD AND POTENTIALLY DEPRECATED TEST CASES
//
//    /**
//     * Test successful registration with a new user that has a redirect defined without a query parameter
//     *
//     * @throws Exception
//     */
//    public void testRegistrationWithRedirect() throws Exception {
//        UserCommand userCommand = new UserCommand();
//        BindException errors = new BindException(userCommand, "");
//        String testRedirectUrl = "community.greatschools.net/advice/write";
//        userCommand.setRedirectUrl(testRedirectUrl );
//        setupRegistrationTest(userCommand);
//
//        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), userCommand, errors);
//
//        assertNotNull("Expect mAndV.getViewName() to have the redirect value", mAndV.getViewName());
//        String expectedResult = "redirect:" + testRedirectUrl;
//        assertEquals("Expect the redirect url", expectedResult, mAndV.getViewName());
//    }
//
//    /**
//     * Test successful registration with a new user  that has a redirect defined with a query parameter
//     *
//     * @throws Exception
//     */
//    public void testRegistrationWithRedirectAndParameters() throws Exception {
//        UserCommand userCommand = new UserCommand();
//        BindException errors = new BindException(userCommand, "");
//        String testRedirectUrl = "community.greatschools.net/advice/write?id=2112";
//        userCommand.setRedirectUrl(testRedirectUrl );
//
//        setupRegistrationTest(userCommand);
//
//        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), userCommand, errors);
//        String expectedResult = "redirect:" + testRedirectUrl;
//
//        assertNotNull("Expect mAndV.getViewName() to have the redirect value", mAndV.getViewName());
//        assertEquals("Expect the redirect url", expectedResult, mAndV.getViewName());
//    }
//
//
//    public void testRegistrationSubscribesToCommunityNewsletter() throws Exception {
//        UserCommand userCommand = new UserCommand();
//        userCommand.setEmail("a");
//        userCommand.getUser().setId(new Integer(345)); // to fake the database save
//        userCommand.setPassword("test");
//        userCommand.setNumSchoolChildren(new Integer(0));
//        userCommand.setState(State.GA);
//
//        Subscription newsletterSubscription = new Subscription();
//        newsletterSubscription.setUser(userCommand.getUser());
//        newsletterSubscription.setProduct(SubscriptionProduct.PARENT_ADVISOR);
//        newsletterSubscription.setState(State.GA);
//
//        getRequest().addParameter("join", "join"); // submit button
//
//        _subscriptionDao.addNewsletterSubscriptions((User)notNull(), (List)notNull());
//        replay(_subscriptionDao);
//        setUpFindUserFromEmailIfExistsAndSoapRequest(userCommand);
//
//        userCommand.setNewsletter(true);
//        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), userCommand, null);
//
//        verify(_subscriptionDao);
//        verify(_soapRequest);
//        verify(_userDao);
//    }
//
//    public void testSetRedirectUrl() throws Exception {
//        UserCommand userCommand = new UserCommand();
//        BindException errors = new BindException(userCommand, "");
//        setupRegistrationTest(userCommand);
//
//        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), userCommand, errors);
//        //_userControl.verify();
//        //_subscriptionDaoMock.verify();
//        //verify(_soapRequest);
//    }
//
//
//
//    public void testRegistrationDoesNotSubscribeToCommunityNewsletter() throws Exception {
//        UserCommand userCommand = new UserCommand();
//        userCommand.setEmail("a");
//        userCommand.getUser().setId(new Integer(345)); // to fake the database save
//        userCommand.setPassword("test");
//        userCommand.setNumSchoolChildren(new Integer(0));
//
//        getRequest().addParameter("join", "join"); // submit button
//
//        // no calls expected
//        _subscriptionDaoMock.replay();
//
//        setUpFindUserFromEmailIfExistsAndSoapRequest(userCommand);
//
//        userCommand.setNewsletter(false);
//        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), userCommand, null);
//
//        _subscriptionDaoMock.verify();
//        verify(_soapRequest);
//        verify(_userDao);
//    }
//
//
//    /**
//     * Test successful registration with an existing user
//     *
//     * @throws NoSuchAlgorithmException
//     */
//    public void xtestExistingUser() throws Exception {
//        String email = "testExistingUser@greatschools.net";
//        Integer userId = 346;
//
//        UserCommand userCommand = new UserCommand();
//        BindException errors = new BindException(userCommand, "");
//        String password = "foobar";
//        userCommand.getUser().setEmail(email);
//
//        userCommand.setPassword(password);
//        userCommand.setConfirmPassword(password);
//        userCommand.setScreenName("screeny");
//        userCommand.setNumSchoolChildren(0);
//        userCommand.getUser().setId(userId);
//
//        assertTrue(userCommand.getUser().isPasswordEmpty());
//        assertFalse(userCommand.getUser().isEmailProvisional());
//
////        _userControl.expectAndReturn(_userDao.findUserFromEmailIfExists(email),
////                userCommand.getUser());
//         expect(_userDao.findUserFromEmailIfExists(email)).andReturn(userCommand.getUser());
//        _userDao.updateUser(userCommand.getUser());
//        _userDao.updateUser(userCommand.getUser());
////        _userControl.replay();
//
//        _soapRequest.setTarget("http://community.greatschools.net/soap/user");
//        _soapRequest.createOrUpdateUserRequest(isA(CreateOrUpdateUserRequestBean.class));
//        expect(_userDao.findUserFromEmailIfExists(email)).andReturn(userCommand.getUser());
//        replay(_soapRequest);
//        replay(_userDao);
//
//        try {
//            _controller.onSubmit(getRequest(), getResponse(), userCommand, errors);
//            _userControl.verify();
//            verify(_soapRequest);
//            verify(_userDao);
//            assertTrue(userCommand.getUser().isEmailProvisional());
//            assertFalse(userCommand.getUser().isPasswordEmpty());
//        } catch (Exception e) {
//            fail(e.toString());
//        }
//    }
//
//    /**
//     * Test that on serious error during the registration process, no partially completed records
//     * are left in the database.
//     */
//    public void testRegistrationFailureOnNewUser() throws SoapRequestException {
//        UserCommand userCommand = new UserCommand();
//        BindException errors = new BindException(userCommand, "");
//        String email = "testRegistrationFailureOnNewUser@RegistrationControllerTest.com";
//        String password = "foobar";
//        Integer userId = new Integer(347);
//        userCommand.setEmail(email);
//        userCommand.setPassword(password);
//        userCommand.setConfirmPassword(password);
//        userCommand.getUser().setId(userId);
//
//        _userControl.expectAndReturn(_userDao.findUserFromEmailIfExists(email), null);
//        _userDao.saveUser(userCommand.getUser());
//        _userDao.updateUser(userCommand.getUser());
//        _userDao.removeUser(userId);
//        _userControl.replay();
//
//        // set the mock mail sender to throw an exception
//        _mailSender.setThrowOnSendMessage(true);
//
//        replay(_soapRequest);
//
//        try {
//            _controller.onSubmit(getRequest(), getResponse(), userCommand, errors);
//            fail("Expected mail exception not thrown");
//        } catch (Exception ex) {
//            _userControl.verify();
//            verify(_soapRequest);
//        } finally {
//            _mailSender.setThrowOnSendMessage(false);
//        }
//    }
//
//    /**
//     * Test that on serious error during the registration process, no partially completed records
//     * are left in the database.
//     */
//    public void testRegistrationFailureOnExistingUser() {
//        UserCommand userCommand = new UserCommand();
//        BindException errors = new BindException(userCommand, "");
//        String email = "testRegistrationFailureOnExistingUser@greatschools.net";
//        String password = "foobar";
//        Integer userId = new Integer(348);
//        userCommand.setEmail(email);
//        userCommand.setPassword(password);
//        userCommand.setConfirmPassword(password);
//        userCommand.getUser().setId(userId);
//
//        User user = userCommand.getUser();
//        assertTrue(user.isPasswordEmpty());
//
//        _userControl.expectAndReturn(_userDao.findUserFromEmailIfExists(email), user);
//        _userDao.updateUser(userCommand.getUser());
//        _userDao.updateUser(userCommand.getUser());
//        _userControl.replay();
//
//        // set the mock mail sender to throw an exception
//        _mailSender.setThrowOnSendMessage(true);
//        try {
//            _controller.onSubmit(getRequest(), getResponse(), userCommand, errors);
//            fail("Expected mail exception not thrown");
//        } catch (Exception e) {
//            _userControl.verify();
//            assertTrue(user.isPasswordEmpty());
//        } finally {
//            _mailSender.setThrowOnSendMessage(false);
//        }
//    }
//
//    public void testOnBindOnNewForm() throws Exception {
//        UserCommand userCommand = new UserCommand();
//        userCommand.setState(null);
//        BindException errors = new BindException(userCommand, "");
//
//        // Test with no user
//        _geoControl.expectAndReturn(_geoDao.findCitiesByState(State.CA), new ArrayList(), 3);
//        _geoControl.replay();
//        _controller.onBindOnNewForm(getRequest(), userCommand, errors);
//        assertNull(userCommand.getEmail());
//
//        // Test with user found, make sure it clears first
//        String email = "testRegistrationFailureOnExistingUser@greatschools.net";
//        String password = "foobar";
//        Integer userId = new Integer(348);
//        userCommand.setEmail(email);
//        userCommand.setPassword(password);
//        userCommand.setConfirmPassword(password);
//        userCommand.setState(State.CA);
//        userCommand.getUser().setId(userId);
//        userCommand.setFirstName("X");
//        userCommand.setLastName("Y");
//        User user = userCommand.getUser();
//        _userControl.expectAndReturn(_userDao.findUserFromEmailIfExists(email), user, 1);
//        _userControl.expectAndReturn(_userDao.findUserFromEmailIfExists(email), null);
//        _userDao.evict(user);
//        _userControl.replay();
//        _controller.onBindOnNewForm(getRequest(), userCommand, errors);
//        assertNull(userCommand.getFirstName());
//        assertNull(userCommand.getLastName());
//
//        // Test with user not found
//        _controller.onBindOnNewForm(getRequest(), userCommand, errors);
//
//        _userControl.verify();
//        _geoControl.verify();
//    }
//
//    private void setUpNiceUserDao() {
//       _userControl = MockControl.createNiceControl(IUserDao.class);
//       _userDao = (IUserDao) _userControl.getMock();
//       _controller.setUserDao(_userDao);
//       _userControl.replay();
//    }
//    private void setUpFindUserFromEmailIfExistsAndSoapRequest(UserCommand userCommand) throws Exception{
//        _soapRequest.setTarget("http://community.greatschools.net/soap/user");
//        _soapRequest.createOrUpdateUserRequest(isA(CreateOrUpdateUserRequestBean.class));
//        expect(_userDao.findUserFromEmailIfExists(userCommand.getEmail())).andReturn(null);
//        expect(_userDao.findUserFromEmailIfExists(userCommand.getEmail())).andReturn(userCommand.getUser());
//        _userDao.saveUser(userCommand.getUser());
//        _userDao.updateUser(userCommand.getUser());
//        _userDao.updateUser(userCommand.getUser());
//        replay(_soapRequest);
//        replay(_userDao);
//
//    }
//
}
