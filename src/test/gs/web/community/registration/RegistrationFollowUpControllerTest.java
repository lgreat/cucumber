package gs.web.community.registration;

import gs.data.community.*;
import gs.data.school.Grade;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.util.DigestUtil;
import gs.data.geo.IGeoDao;
import gs.web.BaseControllerTestCase;
import org.springframework.validation.BindException;
import org.easymock.MockControl;
import org.easymock.AbstractMatcher;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RegistrationFollowUpControllerTest extends BaseControllerTestCase {
    private RegistrationFollowUpController _controller;

    private IUserDao _userDao;
    private MockControl _userControl;
    private ISchoolDao _schoolDao;
    private MockControl _schoolControl;
    private ISubscriptionDao _subscriptionDao;
    private MockControl _subscriptionControl;
    private IGeoDao _geoDao;
    private MockControl _geoControl;

    private FollowUpCommand _command;
    private BindException _errors;
    private User _user;
    private UserProfile _userProfile;

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
        _schoolDao = (ISchoolDao)_schoolControl.getMock();
        _controller.setSchoolDao(_schoolDao);
        _subscriptionDao = (ISubscriptionDao)_subscriptionControl.getMock();
        _controller.setSubscriptionDao(_subscriptionDao);
        _geoDao = (IGeoDao) _geoControl.getMock();
        _controller.setGeoDao(_geoDao);
        _controller.setStateManager(stateManager);

        setupBindings();
    }

    private void setupBindings() throws NoSuchAlgorithmException {
        _command = new FollowUpCommand();
        _errors = new BindException(_command, "");
        _user = new User();
        _userProfile = new UserProfile();
        _user.setEmail("RegistrationFollowUpControllerTest@greatschools.net");
        _user.setId(new Integer(456));

        _userProfile.setUser(_user);
        _userProfile.setScreenName("screeny");
        _userProfile.setNumSchoolChildren(new Integer(1));
        _user.setUserProfile(_userProfile);
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

        assertFalse(_errors.hasErrors());
        assertNotNull(_command.getUser().getId());
        assertTrue("n".equals(_command.getRecontact()));
        assertTrue(_command.getTerms());
        assertEquals(1, _command.getStudents().size());
        assertEquals(1, _command.getCityNames().size());
        assertEquals(1, _command.getSchools().size());
    }

    public void testLoadSchoolList() {
        Student student = new Student();
        student.setState(State.CA);
        student.setGrade(Grade.G_6);
        student.setOrder(new Integer(1));
        String city = "Alameda";

        assertEquals(0, _command.getSchools().size());
        _controller.loadSchoolList(student, city, _command);
        _schoolControl.verify();
        assertEquals(1, _command.getSchools().size());
    }

    public void testRecontact() {
        _command.setRecontact("y");
        _command.setUser(_user);

        Student student = new Student();
        student.setSchoolId(new Integer(1));
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
        _subscriptionDao.getUserSubscriptions(_user, SubscriptionProduct.PARENT_CONTACT);
        // detects none
        _subscriptionControl.setReturnValue(null);
        // a recontact subscription for the student's school will be saved
        Subscription sub = new Subscription();
        sub.setUser(_user);
        sub.setProduct(SubscriptionProduct.PARENT_CONTACT);
        sub.setState(State.CA);
        sub.setSchoolId(student.getSchoolId().intValue());
        // but only one, because 2nd student shares school, and 3rd student has no school listed
        _subscriptionDao.saveSubscription(sub);
        _subscriptionControl.setMatcher(new SubscriptionMatcher());

        _subscriptionControl.replay();

        _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        _userControl.verify();
        _subscriptionControl.verify();
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
    public void testaddStudent() {
        _command.setUser(_user);
        Student student = new Student();
        _command.addStudent(student);
        assertNull(_user.getStudents());

        _userControl.reset(); // negate default settings from setupBindings
        _userDao.updateUser(_user);
        _userControl.replay();

        // controller checks for previous subscriptions
        _subscriptionDao.getUserSubscriptions(_user, SubscriptionProduct.PARENT_CONTACT);
        // detects none
        _subscriptionControl.setReturnValue(null);
        _subscriptionControl.replay();

        _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        _userControl.verify();
        _subscriptionControl.verify();
        assertFalse(_errors.hasErrors());
        assertNotNull(_user.getStudents());
        assertEquals(_user.getStudents().iterator().next(), student);
    }

    public void testOnBindAndValidate() {
        _command.setUser(_user);
        _command.setTerms(true);
        School school = new School();
        school.setName("School");
        school.setId(new Integer(24));

        Student student = new Student();
        student.setGrade(Grade.G_6);
        student.setSchoolId(new Integer(24));
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
        assertEquals("School", _command.getSchoolNames().get(0).toString());
    }

    private class SubscriptionMatcher extends AbstractMatcher {
        public SubscriptionMatcher() {
            // default
        }
        protected boolean argumentMatches(Object first, Object second) {
            Subscription one = (Subscription)first;
            Subscription two = (Subscription)second;
            // protect against NPE's
            if (one.getUser() == null || one.getUser().getId() == null ||
                    two.getUser() == null || two.getUser().getId() == null ||
                    one.getProduct() == null || two.getProduct() == null ||
                    one.getState() == null || two.getState() == null) {
                return false;
            }
            return one.getUser().getId().equals(two.getUser().getId()) &&
                    one.getProduct().equals(two.getProduct()) &&
                    one.getSchoolId() == two.getSchoolId() &&
                    one.getState().equals(two.getState());
        }
        protected String argumentToString(Object argument) {
            if (argument == null || !(argument instanceof Subscription)) {
                return super.argumentToString(argument);
            }
            Subscription sub = (Subscription)argument;
            return "user:" + sub.getUser().getId() + ";product:" + sub.getProduct().getName() +
                    ";schoolId:" + sub.getSchoolId() + ";state:" + sub.getState();
        }
    }
}
