package gs.web.community.registration;

import gs.data.community.*;
import gs.data.school.Grade;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.school.Grades;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.util.DigestUtil;
import gs.web.BaseControllerTestCase;
import org.springframework.validation.BindException;
import org.easymock.MockControl;
import org.easymock.AbstractMatcher;

import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RegistrationFollowUpControllerTest extends BaseControllerTestCase {
    private RegistrationFollowUpController _controller;

    private IUserDao _userDao;
    private MockControl _userControl;
    private ISchoolDao _schoolDao;
    private MockControl _schoolControl;
    private ISubscriptionDao _mockSubscriptionDao;
    private MockControl _subscriptionControl;

    private FollowUpCommand _command;
    private BindException _errors;
    private User _user;
    private UserProfile _userProfile;

    protected void setUp() throws Exception {
        super.setUp();
        _userControl = MockControl.createControl(IUserDao.class);
        _schoolControl = MockControl.createControl(ISchoolDao.class);
        _subscriptionControl = MockControl.createControl(ISubscriptionDao.class);
        StateManager stateManager = new StateManager();

        _controller = new RegistrationFollowUpController();
        _userDao = (IUserDao)_userControl.getMock();
        _controller.setUserDao(_userDao);
        _schoolDao = (ISchoolDao)_schoolControl.getMock();
        _controller.setSchoolDao(_schoolDao);
        _mockSubscriptionDao = (ISubscriptionDao)_subscriptionControl.getMock();
        _controller.setSubscriptionDao(_mockSubscriptionDao);
        _controller.setStateManager(stateManager);

        setupBindings();
    }

    private void setupBindings() throws NoSuchAlgorithmException {
        _command = new FollowUpCommand();
        _errors = new BindException(_command, "");
        _user = new User();
        _userProfile = new UserProfile();
        _user.setEmail("RegistrationFollowUpControllerTest10@greatschools.net");
        _user.setId(new Integer(456));

        _userProfile.setUser(_user);
        _userProfile.setScreenName("screeny");
        _userProfile.setNumSchoolChildren(new Integer(0));
        _user.setUserProfile(_userProfile);
        _command.setUser(_user);

        String hash = DigestUtil.hashStringInt(_user.getEmail(), _user.getId());
        getRequest().addParameter("marker", hash);

        _userControl.expectAndReturn(_userDao.findUserFromId(456), _user);
        _userControl.replay();
    }

    public void testRegistrationFollowUp() throws NoSuchAlgorithmException {
        _command.setAboutMe("My children are so unique!");
        getRequest().addParameter("private", "checked");
        _command.setOtherInterest("Other");
        String interestCode = UserProfile.getInterestsMap().keySet().iterator().next().toString();
        getRequest().addParameter(interestCode, "checked");

        School school = new School();
        school.setName("School");
        school.setId(new Integer(1));
        school.setDatabaseState(State.CA);
        getRequest().addParameter("previousSchool1", school.getName());
        getRequest().addParameter("previousSchoolId1", String.valueOf(school.getId()));
        getRequest().addParameter("previousState1", State.CA.getAbbreviation());

        String hash = DigestUtil.hashStringInt(_user.getEmail(), _user.getId());
        getRequest().addParameter("marker", hash);

        _schoolControl.expectAndReturn(_schoolDao.getSchoolById(State.CA, school.getId()),
                school);
        _schoolControl.replay();

        _controller.onBindAndValidate(getRequest(), _command, _errors);
        _userControl.verify();
        _schoolControl.verify();
        assertFalse(_errors.hasErrors());

        // controller checks for previous subscriptions
        _mockSubscriptionDao.getUserSubscriptions(_user, SubscriptionProduct.PREVIOUS_SCHOOLS);
        // detects none
        _subscriptionControl.setReturnValue(null);
        Subscription expectedSubscription = new Subscription();
        expectedSubscription.setUser(_user);
        expectedSubscription.setProduct(SubscriptionProduct.PREVIOUS_SCHOOLS);
        expectedSubscription.setSchoolId(school.getId().intValue());
        // saves the new subscription
        _mockSubscriptionDao.saveSubscription(expectedSubscription);
        // I check that the correct user is matched to the correct subscription product
        // and the correct school id
        _subscriptionControl.setMatcher(new AbstractMatcher() {
            protected boolean argumentMatches(Object first, Object second) {
                Subscription one = (Subscription)first;
                Subscription two = (Subscription)second;
                // protect against NPE's
                if (one.getUser() == null || one.getUser().getId() == null ||
                        two.getUser() == null || two.getUser().getId() == null ||
                        one.getProduct() == null || two.getProduct() == null) {
                    return false;
                }
                return one.getUser().getId().equals(two.getUser().getId()) &&
                        one.getProduct().equals(two.getProduct()) &&
                        one.getSchoolId() == two.getSchoolId();
            }
        });
        // and that's it
        _subscriptionControl.replay();

        _userControl.reset();
        _userDao.updateUser(_user);
        _userControl.replay();

        _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        _userControl.verify();
        _subscriptionControl.verify();
        assertFalse(_errors.hasErrors());
    }

    public void testBadHash() throws NoSuchAlgorithmException {
        _command.setAboutMe("My children are so unique!");
        _command.setPrivate(false);

        // don't add hash to request
        getRequest().setParameter("marker", null);
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        _userControl.verify();
        assertTrue(_errors.hasErrors());
        assertEquals(1, _errors.getErrorCount());
    }

    public void testAddChild() throws NoSuchAlgorithmException {
        _userProfile.setNumSchoolChildren(new Integer(1));

        _userControl.reset(); // negate default settings from setupBindings
        _userControl.expectAndReturn(_userDao.findUserFromId(456), _user);
        _userDao.updateUser(_user);
        _userControl.replay();

        String hash = DigestUtil.hashStringInt(_user.getEmail(), _user.getId());
        getRequest().addParameter("marker", hash);
        getRequest().addParameter("addChild", "addChild");
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        _userControl.verify();
        assertTrue(_errors.hasErrors());

        assertEquals(_user.getUserProfile().getNumSchoolChildren(), new Integer(2));
    }

    public void testRemoveChild() throws NoSuchAlgorithmException {
        _userProfile.setNumSchoolChildren(new Integer(2));

        _userControl.reset(); // negate default settings from setupBindings
        _userControl.expectAndReturn(_userDao.findUserFromId(456), _user);
        _userDao.updateUser(_user);
        _userControl.replay();

        String hash = DigestUtil.hashStringInt(_user.getEmail(), _user.getId());
        getRequest().addParameter("marker", hash);
        getRequest().addParameter("removeChild", "removeChild");
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        _userControl.verify();
        assertTrue(_errors.hasErrors());

        assertEquals(_user.getUserProfile().getNumSchoolChildren(), new Integer(1));
    }

    /**
     * Test that if a student is in the command, that student is added to the user and updateUser
     * is called on the dao.
     */
    public void testaddStudent() throws NoSuchAlgorithmException {
        _userProfile.setNumSchoolChildren(new Integer(1));

        Student student = new Student();
        _command.addStudent(student);
        assertNull(_user.getStudents());

        _userControl.reset(); // negate default settings from setupBindings
        _userDao.updateUser(_user);
        _userControl.replay();

        _controller.onSubmit(getRequest(), getResponse(), _command, _errors);
        _userControl.verify();
        assertFalse(_errors.hasErrors());
        assertNotNull(_user.getStudents());
        assertEquals(_user.getStudents().iterator().next(), student);
    }

    public void testValidateStudent() throws NoSuchAlgorithmException {
        _userProfile.setNumSchoolChildren(new Integer(1));

        getRequest().addParameter("childname1", "Anthony");
        getRequest().addParameter("grade1", Grade.G_10.getName());
        getRequest().addParameter("state1", "CA");
        School school = new School();
        school.setName("School");
        school.setId(new Integer(1));
        school.setDatabaseState(State.CA);
        school.setGradeLevels(Grades.createGrades(Grade.G_9, Grade.G_12));
        getRequest().addParameter("schoolId1", String.valueOf(school.getId()));
        getRequest().addParameter("school1", school.getName());

        assertEquals(0, _command.getNumStudents());

        _schoolControl.expectAndReturn(_schoolDao.getSchoolById(State.CA, school.getId()),
                school);
        _schoolControl.replay();

        // successful validation should insert the student into the command
        _controller.onBindAndValidate(getRequest(), _command, _errors);
        _userControl.verify();
        _schoolControl.verify();
        assertFalse(_errors.hasErrors());
        assertEquals(1, _command.getNumStudents());
        Student student = (Student) _command.getStudents().get(0);
        assertEquals("Anthony", student.getName());
        assertEquals(Grade.G_10, student.getGrade());
        assertEquals(new Integer(1), student.getSchoolId());
        assertEquals(State.CA, student.getState());
    }

    public void testStudentNameLength() throws NoSuchAlgorithmException {
        _userProfile.setNumSchoolChildren(new Integer(1));

        StringBuffer childNameText = new StringBuffer();
        for (int x=0; x < RegistrationFollowUpController.STUDENT_NAME_MAX_LENGTH + 1; x++) {
            childNameText.append("x");
        } // too long

        getRequest().addParameter("childname1", childNameText.toString());
        getRequest().addParameter("grade1", Grade.G_10.getName());
        getRequest().addParameter("state1", "CA");

        _controller.onBindAndValidate(getRequest(), _command, _errors);
        _userControl.verify();
        assertTrue(_errors.hasErrors());
        assertTrue(_errors.hasFieldErrors("students[0]"));
    }

    public void testAboutMeLength() throws NoSuchAlgorithmException {
        StringBuffer aboutMeText = new StringBuffer();
        for (int x=0; x < RegistrationFollowUpController.ABOUT_ME_MAX_LENGTH + 1; x++) {
            aboutMeText.append("x");
        } // too long
        _command.setAboutMe(aboutMeText.toString());

        _controller.onBindAndValidate(getRequest(), _command, _errors);
        _userControl.verify();
        assertTrue(_errors.hasErrors());
        assertTrue(_errors.hasFieldErrors("aboutMe"));
    }

    public void testOtherInterestLength() throws NoSuchAlgorithmException {
        StringBuffer otherText = new StringBuffer();
        for (int x=0; x < RegistrationFollowUpController.OTHER_INTEREST_MAX_LENGTH + 1; x++) {
            otherText.append("x");
        } // too long
        _command.setOtherInterest(otherText.toString());

        _controller.onBindAndValidate(getRequest(), _command, _errors);
        _userControl.verify();
        assertTrue(_errors.hasErrors());
        assertTrue(_errors.hasFieldErrors("otherInterest"));
    }

    public void testDuplicatePreviousSchools() throws NoSuchAlgorithmException {
        String hash = DigestUtil.hashStringInt(_user.getEmail(), _user.getId());
        getRequest().addParameter("marker", hash);

        School school = new School();
        school.setName("School");
        school.setId(new Integer(1));
        school.setDatabaseState(State.CA);
        getRequest().addParameter("previousSchool1", school.getName());
        getRequest().addParameter("previousSchoolId1", String.valueOf(school.getId()));
        getRequest().addParameter("previousState1", State.CA.getAbbreviation());
        getRequest().addParameter("previousSchool2", school.getName());
        getRequest().addParameter("previousSchoolId2", String.valueOf(school.getId()));
        getRequest().addParameter("previousState2", State.CA.getAbbreviation());

        _schoolControl.expectAndReturn(_schoolDao.getSchoolById(State.CA, school.getId()),
                school, 2);
        _schoolControl.replay();

        _controller.onBindAndValidate(getRequest(), _command, _errors);
        _userControl.verify();
        _schoolControl.verify();
        assertFalse(_errors.hasErrors());
        List subs = _command.getSubscriptions();
        assertNotNull(subs);
        assertEquals(1, subs.size());
    }
}
