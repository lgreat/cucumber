package gs.web.community.registration;

import gs.data.community.*;
import gs.data.school.Grade;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.util.DigestUtil;
import gs.data.util.email.MockJavaMailSender;
import gs.data.geo.IGeoDao;
import gs.data.admin.IPropertyDao;
import gs.data.soap.CreateOrUpdateUserRequest;
import gs.data.soap.CreateOrUpdateUserRequestBean;
import gs.data.soap.SoapRequestException;
import gs.web.BaseControllerTestCase;
import org.springframework.validation.BindException;
import org.easymock.MockControl;
import static org.easymock.classextension.EasyMock.*;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RegistrationFollowUpControllerTest extends BaseControllerTestCase {
    private RegistrationFollowUpController _controller;

    private IUserDao _userDao;
    private MockControl _userControl;
    private ISchoolDao _schoolDao;
    private IPropertyDao _propertyDao;
    private MockControl _schoolControl;
    private ISubscriptionDao _subscriptionDao;
    private MockControl _subscriptionControl;
    private IGeoDao _geoDao;
    private MockControl _geoControl;
    private MockJavaMailSender _mailSender;
    private CreateOrUpdateUserRequest _soapRequest;

    private FollowUpCommand _command;
    private BindException _errors;
    private User _user;

    protected void setUp() throws Exception {
        super.setUp();
        _userControl = MockControl.createControl(IUserDao.class);
        _schoolControl = MockControl.createControl(ISchoolDao.class);
        _subscriptionControl = MockControl.createControl(ISubscriptionDao.class);
        _geoControl = MockControl.createControl(IGeoDao.class);
        StateManager stateManager = new StateManager();

        _controller = new RegistrationFollowUpController();
        _userDao = (IUserDao)_userControl.getMock();
        _controller.setUserDao(_userDao);

        _propertyDao = (IPropertyDao)
                getApplicationContext().getBean(IPropertyDao.BEAN_ID);
        _controller.setPropertyDao(_propertyDao);

        _schoolDao = (ISchoolDao)_schoolControl.getMock();
        _controller.setSchoolDao(_schoolDao);
        _subscriptionDao = (ISubscriptionDao)_subscriptionControl.getMock();
        _controller.setSubscriptionDao(_subscriptionDao);
        _geoDao = (IGeoDao) _geoControl.getMock();
        _controller.setGeoDao(_geoDao);
        _controller.setStateManager(stateManager);

        _mailSender = new MockJavaMailSender();
        _mailSender.setHost("greatschools.net");
        RegistrationConfirmationEmail email = (RegistrationConfirmationEmail)
                getApplicationContext().getBean(RegistrationConfirmationEmail.BEAN_ID);
        email.getEmailHelperFactory().setMailSender(_mailSender);
        _controller.setRegistrationConfirmationEmail(email);

        _controller.setAuthenticationManager(new AuthenticationManager());

        _soapRequest = createMock(CreateOrUpdateUserRequest.class);
        _controller.setSoapRequest(_soapRequest);

        setupBindings();
    }

    private void setupBindings() throws NoSuchAlgorithmException {
        _command = new FollowUpCommand();
        _errors = new BindException(_command, "");
        _user = new User();
        UserProfile userProfile = new UserProfile();
        _user.setEmail("RegistrationFollowUpControllerTest@greatschools.net");
        _user.setId(456);

        userProfile.setUser(_user);
        userProfile.setScreenName("screeny");
        userProfile.setNumSchoolChildren(1);
        _user.setUserProfile(userProfile);
        //_command.setUser(_user);

        String hash = DigestUtil.hashStringInt(_user.getEmail(), _user.getId());
        getRequest().addParameter("marker", hash);
        getRequest().addParameter("id", _user.getId().toString());
        getRequest().addParameter("recontactStr", "n");
        getRequest().addParameter("termsStr", "y");

        getRequest().addParameter("grade1", "6");
        getRequest().addParameter("state1", "CA");
        getRequest().addParameter("city1", "Alameda");
        getRequest().addParameter("school1", "24");

        _schoolDao.findSchoolsInCityByGrade(State.CA, "Alameda", Grade.G_6);
        _schoolControl.setReturnValue(new ArrayList());
        _schoolControl.replay();

        _userControl.expectAndReturn(_userDao.findUserFromId(456), _user);
        _userControl.replay();
    }

    public void testBindRequestData() throws Exception {
        _command.setRecontact("false");

        assertNull(_command.getUser().getId());
        assertFalse("n".equals(_command.getRecontact()));
        assertTrue(_command.getTerms());
        assertEquals(0, _command.getStudents().size());
        assertEquals(0, _command.getCityNames().size());
        assertEquals(0, _command.getSchools().size());

        _controller.bindRequestData(getRequest(), _command, _errors);
        _userControl.verify();
        _schoolControl.verify();


        String year = (String)getRequest().getAttribute("current_academic_year");
        assertEquals("2006-2007", year);
        
        assertFalse(_errors.hasErrors());
        assertNotNull(_command.getUser().getId());
        assertTrue("n".equals(_command.getRecontact()));
        assertTrue(_command.getTerms());
        assertEquals(1, _command.getStudents().size());
        assertEquals(1, _command.getCityNames().size());
        assertEquals(1, _command.getSchools().size());
    }

    public void testBindRequestDataIgnoresNewsletterNull() throws Exception {
        _user.getUserProfile().setNumSchoolChildren(0);
        _controller.onBind(getRequest(), _command, _errors);
        assertFalse("Newsletter should be false if no parameter passed", _command.getNewsletter());
    }

    public void testBindRequestDataCapturesNewsletterTrue() throws Exception {
        _request.setParameter(RegistrationController.NEWSLETTER_PARAMETER, "on");
        _user.getUserProfile().setNumSchoolChildren(0);
        _controller.onBind(getRequest(), _command, _errors);
        assertTrue("Expected newsletter to be true when 'on' is passed", _command.getNewsletter());
    }

    public void testBindRequestDataCapturesNewsletterFalse() throws Exception {
        _request.setParameter(RegistrationController.NEWSLETTER_PARAMETER, "n");
        _user.getUserProfile().setNumSchoolChildren(0);
        _controller.onBind(getRequest(), _command, _errors);
        assertFalse("Expected newsletter to be false when 'n' is passed", _command.getNewsletter());
    }

    public void testBindRequestDataIgnoresBetaNull() throws Exception {
        _user.getUserProfile().setNumSchoolChildren(0);
        _controller.onBind(getRequest(), _command, _errors);
        assertFalse("Beta should be false if no parameter passed", _command.isBeta());
    }

    public void testBindRequestDataCapturesBetaFalse() throws Exception {
        _request.setParameter(RegistrationController.BETA_PARAMETER, "off");
        _user.getUserProfile().setNumSchoolChildren(0);
        _controller.onBind(getRequest(), _command, _errors);
        assertFalse("Beta should be false if parameter is not \"on\"", _command.isBeta());
    }

    public void testBindRequestDataCapturesBetaTrue() throws Exception {
        _request.setParameter(RegistrationController.BETA_PARAMETER, "on");
        _user.getUserProfile().setNumSchoolChildren(0);
        _controller.onBind(getRequest(), _command, _errors);
        assertTrue("Beta should be true if parameter is \"on\"", _command.isBeta());
    }

    public void testLoadSchoolList() {
        Student student = new Student();
        student.setState(State.CA);
        student.setGrade(Grade.G_6);
        student.setOrder(1);
        String city = "Alameda";

        assertEquals(0, _command.getSchools().size());
        _controller.loadSchoolList(student, city, _command);
        _schoolControl.verify();
        assertEquals(1, _command.getSchools().size());
    }

    public void testRecontact() throws NoSuchAlgorithmException, SoapRequestException {
        _command.setRecontact("y");
        _command.setUser(_user);
        _command.setNewsletter(false);

        Student student = new Student();
        student.setSchoolId(1);
        student.setState(State.CA);
        _command.addStudent(student);
        _command.addStudent(student); // second student shares school
        Student studentNoSchool = new Student();
        studentNoSchool.setState(State.CA);
        _command.addStudent(studentNoSchool);

        _userControl.reset(); // negate default settings from setupBindings
        _userDao.updateUser(_user);
        _userControl.replay();

        // controller checks for previous subscriptions
        // detects none
        expect(_subscriptionDao.getUserSubscriptions(_user, SubscriptionProduct.PARENT_CONTACT)).andReturn(null);
        // a recontact subscription for the student's school will be saved
        Subscription sub = new Subscription();
        sub.setUser(_user);
        sub.setProduct(SubscriptionProduct.PARENT_CONTACT);
        sub.setState(State.CA);
        sub.setSchoolId(student.getSchoolId());
        // but only one, because 2nd student shares school, and 3rd student has no school listed
        _subscriptionDao.saveSubscription(isA(Subscription.class));

        expect(_subscriptionDao.getUserSubscriptions(_user, SubscriptionProduct.COMMUNITY)).andReturn(null);

        replay(_subscriptionDao);
        replay(_soapRequest);
        _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        _userControl.verify();
        verify(_subscriptionDao);
        verify(_soapRequest);
        assertFalse(_errors.hasErrors());
    }

    public void testBadHash() throws NoSuchAlgorithmException {
        // don't add hash to request
        getRequest().setParameter("marker", (String)null);

        _command.setRecontact("false");

        _controller.bindRequestData(getRequest(), _command, _errors);
        _userControl.verify();
        _schoolControl.verify();

        assertTrue(_errors.hasErrors());
        assertEquals(1, _errors.getErrorCount());
    }

    /**
     * Test that if a student is in the command, that student is added to the user and updateUser
     * is called on the dao.
     */
    public void testaddStudent() throws NoSuchAlgorithmException {
        _command.setUser(_user);
        Student student = new Student();
        _command.addStudent(student);
        assertNull(_user.getStudents());
        _command.setNewsletter(false);


        _userControl.reset(); // negate default settings from setupBindings
        _userDao.updateUser(_user);
        _userControl.replay();

        // controller checks for previous subscriptions
        // detects none
        _subscriptionControl.expectAndReturn(_subscriptionDao.getUserSubscriptions(_user, SubscriptionProduct.PARENT_CONTACT), null);
        _subscriptionControl.expectAndReturn(_subscriptionDao.getUserSubscriptions(_user, SubscriptionProduct.COMMUNITY), null);
        _subscriptionControl.replay();
        replay(_soapRequest);

        _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        _userControl.verify();
        _subscriptionControl.verify();
        verify(_soapRequest);
        assertFalse(_errors.hasErrors());
        assertNotNull(_user.getStudents());
        assertEquals(_user.getStudents().iterator().next(), student);
    }

    public void testOnBindSetsTermsAndNewsletter() throws Exception {
        _geoControl.expectAndReturn(_geoDao.findCitiesByState(State.CA), new ArrayList(), 2);
        _geoControl.replay();
        getRequest().setParameter("city", "San Francisco");

        FollowUpCommand followUpCommand = new FollowUpCommand();
        getRequest().setParameter(RegistrationController.TERMS_PARAMETER, "n");
        getRequest().setParameter(RegistrationController.NEWSLETTER_PARAMETER, "n");
        _controller.onBind(getRequest(), followUpCommand, null);
        assertFalse("Expected terms to be set to false", followUpCommand.getTerms());
        assertFalse("Expected newsletter to be set to false", followUpCommand.getNewsletter());
    }

    public void testOnBindSetsTermsAndNewsletterToTrue() throws Exception {
        _geoControl.expectAndReturn(_geoDao.findCitiesByState(State.CA), new ArrayList(), 2);
        _geoControl.replay();
        getRequest().setParameter("city", "San Francisco");

        FollowUpCommand followUpCommand = new FollowUpCommand();
        getRequest().setParameter(RegistrationController.TERMS_PARAMETER, "on");
        getRequest().setParameter(RegistrationController.NEWSLETTER_PARAMETER, "on");
        _controller.onBind(getRequest(), followUpCommand, null);
        assertTrue("Expected terms to be set to true", followUpCommand.getTerms());
        assertTrue("Expected newsletter to be set to true", followUpCommand.getNewsletter());
    }

    public void testOnBindAndValidate() {
        _command.setUser(_user);
        _command.setTerms(true);
        School school = new School();
        school.setName("School");
        school.setId(24);

        Student student = new Student();
        student.setGrade(Grade.G_6);
        student.setSchoolId(24);
        student.setState(State.CA);
        _command.addStudent(student);

        _schoolControl.reset();
        _schoolControl.expectAndReturn(_schoolDao.getSchoolById(State.CA, school.getId()),
                school);
        _schoolControl.replay();

        // successful validation should insert the student into the command
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        _schoolControl.verify();
        assertFalse(_errors.toString(), _errors.hasErrors());
        assertEquals(1, _command.getSchoolNames().size());
        assertEquals("School", _command.getSchoolNames().get(0));
    }

    public void testRegistrationSubscribesToCommunityNewsletter() throws Exception {
        FollowUpCommand followUpCommand = new FollowUpCommand();
        followUpCommand.getUser().setId(345); // to fake the database save
        followUpCommand.getUser().setEmail("a");
        UserProfile userProfile = new UserProfile();
        userProfile.setNumSchoolChildren(Integer.valueOf("0"));
        followUpCommand.getUser().setUserProfile(userProfile);
        followUpCommand.getUserProfile().setState(State.GA);

        Subscription newsletterSubscription = new Subscription();
        newsletterSubscription.setUser(followUpCommand.getUser());
        newsletterSubscription.setProduct(SubscriptionProduct.COMMUNITY);
        newsletterSubscription.setState(State.GA);

        expect(_subscriptionDao.getUserSubscriptions(followUpCommand.getUser(),
                SubscriptionProduct.PARENT_CONTACT))
                .andReturn(null);
        expect(_subscriptionDao.getUserSubscriptions(followUpCommand.getUser(),
                SubscriptionProduct.COMMUNITY))
                .andReturn(null);
        _subscriptionDao.addNewsletterSubscriptions(isA(User.class), (List)notNull());

        replay(_subscriptionDao);

        // user dao behavior is validated elsewhere
        setUpNiceUserDao();

        replay(_soapRequest);

        followUpCommand.setNewsletter(true);
        _controller.onSubmit(getRequest(), getResponse(), followUpCommand, null);
        verify(_subscriptionDao);
        verify(_soapRequest);
    }

    public void testRegistrationDoesNotSubscribeToCommunityNewsletter() throws Exception {
        FollowUpCommand followUpCommand = new FollowUpCommand();
        followUpCommand.getUser().setId(345); // to fake the database save
        followUpCommand.getUser().setEmail("a");
        UserProfile userProfile = new UserProfile();
        userProfile.setNumSchoolChildren(Integer.valueOf("0"));
        followUpCommand.getUser().setUserProfile(userProfile);
        Subscription newsletterSubscription = new Subscription();
        newsletterSubscription.setUser(followUpCommand.getUser());
        newsletterSubscription.setProduct(SubscriptionProduct.COMMUNITY);

        // no call to saveSubscription expected
        _subscriptionControl.expectAndReturn(_subscriptionDao.getUserSubscriptions(followUpCommand.getUser(), SubscriptionProduct.PARENT_CONTACT), null);
        _subscriptionControl.expectAndReturn(_subscriptionDao.getUserSubscriptions(followUpCommand.getUser(), SubscriptionProduct.COMMUNITY), null);
        _subscriptionControl.replay();

        // user dao behavior is validated elsewhere
        setUpNiceUserDao();

        replay(_soapRequest);

        followUpCommand.setNewsletter(false);
        _controller.onSubmit(getRequest(), getResponse(), followUpCommand, null);

        _subscriptionControl.verify();
        verify(_soapRequest);
    }

    public void testRegistrationDoesSubscribeToBeta() throws Exception {
        FollowUpCommand followUpCommand = new FollowUpCommand();
        followUpCommand.getUser().setId(345); // to fake the database save
        followUpCommand.getUser().setEmail("a");
        followUpCommand.getUserProfile().setState(State.GA);
        UserProfile userProfile = new UserProfile();
        userProfile.setNumSchoolChildren(0);
        followUpCommand.getUser().setUserProfile(userProfile);

        // no call to saveSubscription expected
        expect(_subscriptionDao.getUserSubscriptions(followUpCommand.getUser(), SubscriptionProduct.PARENT_CONTACT))
                .andReturn(null);
        expect(_subscriptionDao.getUserSubscriptions(followUpCommand.getUser(), SubscriptionProduct.COMMUNITY))
                .andReturn(null);
        expect(_subscriptionDao.getUserSubscriptions(followUpCommand.getUser(), SubscriptionProduct.BETA_GROUP))
                .andReturn(null);
        Subscription betaSubscription = new Subscription();
        betaSubscription.setUser(followUpCommand.getUser());
        betaSubscription.setProduct(SubscriptionProduct.BETA_GROUP);
        betaSubscription.setState(State.GA);
        _subscriptionDao.saveSubscription(betaSubscription);
        replay(_subscriptionDao);

        // user dao behavior is validated elsewhere
        setUpNiceUserDao();
        replay(_soapRequest);

        followUpCommand.setNewsletter(false);
        followUpCommand.setBeta(true);
        _controller.onSubmit(getRequest(), getResponse(), followUpCommand, null);
        verify(_subscriptionDao);
        verify(_soapRequest);
    }

    public void testRegistrationDoesNotSubscribeToBetaIfAlreadySubscribed() throws Exception {
        FollowUpCommand followUpCommand = new FollowUpCommand();
        followUpCommand.getUser().setId(345); // to fake the database save
        followUpCommand.getUser().setEmail("a");
        followUpCommand.getUserProfile().setState(State.GA);
        UserProfile userProfile = new UserProfile();
        userProfile.setNumSchoolChildren(0);
        followUpCommand.getUser().setUserProfile(userProfile);

        // no call to saveSubscription expected
        expect(_subscriptionDao.getUserSubscriptions(followUpCommand.getUser(), SubscriptionProduct.PARENT_CONTACT))
                .andReturn(null);
        expect(_subscriptionDao.getUserSubscriptions(followUpCommand.getUser(), SubscriptionProduct.COMMUNITY))
                .andReturn(null);
        expect(_subscriptionDao.getUserSubscriptions(followUpCommand.getUser(), SubscriptionProduct.BETA_GROUP))
                .andReturn(new ArrayList<Subscription>());
        // no save expected
        replay(_subscriptionDao);

        // user dao behavior is validated elsewhere
        setUpNiceUserDao();
        replay(_soapRequest);

        followUpCommand.setNewsletter(false);
        followUpCommand.setBeta(true);
        _controller.onSubmit(getRequest(), getResponse(), followUpCommand, null);
        verify(_subscriptionDao);
        verify(_soapRequest);
    }

    public void testRegistrationDoesNotSubscribeToBeta() throws Exception {
        FollowUpCommand followUpCommand = new FollowUpCommand();
        followUpCommand.getUser().setId(345); // to fake the database save
        followUpCommand.getUser().setEmail("a");
        UserProfile userProfile = new UserProfile();
        userProfile.setNumSchoolChildren(0);
        followUpCommand.getUser().setUserProfile(userProfile);

        // no call to saveSubscription expected
        _subscriptionControl.expectAndReturn(_subscriptionDao.getUserSubscriptions(followUpCommand.getUser(), SubscriptionProduct.PARENT_CONTACT), null);
        _subscriptionControl.expectAndReturn(_subscriptionDao.getUserSubscriptions(followUpCommand.getUser(), SubscriptionProduct.COMMUNITY), null);
        _subscriptionControl.replay();

        // user dao behavior is validated elsewhere
        setUpNiceUserDao();
        replay(_soapRequest);

        followUpCommand.setNewsletter(false);
        followUpCommand.setBeta(false);
        _controller.onSubmit(getRequest(), getResponse(), followUpCommand, null);

        _subscriptionControl.verify();
        verify(_soapRequest);
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
            _soapRequest.createOrUpdateUserRequest(isA(CreateOrUpdateUserRequestBean.class));
            replay(_soapRequest);
            _controller.notifyCommunity(1, "myname", "email@example.com", "foobar", new Date(), _request);
            verify(_soapRequest);
        } catch (SoapRequestException e) {
            fail(e.getMessage());
        }
    }    

    public void testNotifyCommunityLiveCobrand() {
        _request.setServerName("yahooed.greatschools.net");
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
