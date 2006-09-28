package gs.web.community.registration;

import gs.data.community.*;
import gs.data.school.Grade;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.util.DigestUtil;
import gs.web.BaseControllerTestCase;
import org.springframework.context.ApplicationContext;
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
    private ISchoolDao _schoolDao;
    private ISubscriptionDao _mockSubscriptionDao;
    private MockControl _subscriptionControl;

    protected void setUp() throws Exception {
        super.setUp();
        ApplicationContext appContext = getApplicationContext();
        _subscriptionControl = MockControl.createControl(ISubscriptionDao.class);
        _controller = (RegistrationFollowUpController) appContext.getBean(RegistrationFollowUpController.BEAN_ID);
        _userDao = (IUserDao)appContext.getBean(IUserDao.BEAN_ID);
        _schoolDao = (ISchoolDao)appContext.getBean(ISchoolDao.BEAN_ID);
        _mockSubscriptionDao = (ISubscriptionDao)_subscriptionControl.getMock();
        _controller.setSubscriptionDao(_mockSubscriptionDao);
    }

    public void testRegistrationFollowUp() throws NoSuchAlgorithmException {
        FollowUpCommand command = new FollowUpCommand();
        BindException errors = new BindException(command, "");
        User user = new User();
        UserProfile userProfile = new UserProfile();
        user.setEmail("RegistrationFollowUpControllerTest1@greatschools.net");
        _userDao.saveUser(user);

        try {
            userProfile.setUser(user);
            userProfile.setNumSchoolChildren(new Integer(0));
            userProfile.setScreenName("screeny");
            user.setUserProfile(userProfile);
            _userDao.updateUser(user);

            command.setAboutMe("My children are so unique!");
            getRequest().addParameter("private", "checked");
            command.setUser(user);
            command.setOtherInterest("Other");
            String interestCode = UserProfile.getInterestsMap().keySet().iterator().next().toString();
            getRequest().addParameter(interestCode, "checked");

            School school = _schoolDao.getSchoolById(State.CA, new Integer(1));
            getRequest().addParameter("previousSchool1", school.getName());
            getRequest().addParameter("previousSchoolId1", String.valueOf(school.getId()));
            getRequest().addParameter("previousState1", State.CA.getAbbreviation());

            String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
            getRequest().addParameter("marker", hash);
            _controller.onBindAndValidate(getRequest(), command, errors);
            assertFalse(errors.hasErrors());

            // controller checks for previous subscriptions
            _mockSubscriptionDao.getUserSubscriptions(user, SubscriptionProduct.PREVIOUS_SCHOOLS);
            // detects none
            _subscriptionControl.setReturnValue(null);
            Subscription expectedSubscription = new Subscription();
            expectedSubscription.setUser(user);
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

            _controller.onSubmit(getRequest(), getResponse(), command, errors);
            assertFalse(errors.hasErrors());
            user = _userDao.findUserFromId(user.getId().intValue());
            userProfile = user.getUserProfile();
            assertEquals("My children are so unique!", userProfile.getAboutMe());
            assertTrue(userProfile.isPrivate());
            assertEquals("Other", userProfile.getOtherInterest());
            assertTrue(userProfile.getInterestsAsArray().length == 1);
            assertEquals(interestCode, userProfile.getInterestsAsArray()[0]);
            _subscriptionControl.verify();
        } finally {
            _userDao.removeUser(user.getId());
        }
    }

    public void testBadHash() throws NoSuchAlgorithmException {
        FollowUpCommand command = new FollowUpCommand();
        BindException errors = new BindException(command, "");
        User user = new User();
        UserProfile userProfile = new UserProfile();
        user.setEmail("RegistrationFollowUpControllerTest2@greatschools.net");
        _userDao.saveUser(user);

        try {
            userProfile.setUser(user);
            userProfile.setScreenName("screeny");
            user.setUserProfile(userProfile);
            _userDao.updateUser(user);

            command.setAboutMe("My children are so unique!");
            command.setPrivate(false);
            command.setUser(user);

            // don't add hash to request
            _controller.onBindAndValidate(getRequest(), command, errors);
            assertTrue(errors.hasErrors());
            assertEquals(1, errors.getErrorCount());
        } finally {
            _userDao.removeUser(user.getId());
        }
    }

    public void testAddChild() throws NoSuchAlgorithmException {
        FollowUpCommand command = new FollowUpCommand();
        BindException errors = new BindException(command, "");
        User user = new User();
        UserProfile userProfile = new UserProfile();
        user.setEmail("RegistrationFollowUpControllerTest3@greatschools.net");
        _userDao.saveUser(user);

        try {
            userProfile.setUser(user);
            userProfile.setScreenName("screeny");
            userProfile.setNumSchoolChildren(new Integer(1));
            user.setUserProfile(userProfile);
            _userDao.updateUser(user);

            user = _userDao.findUserFromId(user.getId().intValue());
            assertEquals(user.getUserProfile().getNumSchoolChildren(), new Integer(1));

            command.setUser(user);

            String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
            getRequest().addParameter("marker", hash);
            getRequest().addParameter("addChild", "addChild");
            _controller.onBindAndValidate(getRequest(), command, errors);
            assertTrue(errors.hasErrors());

            user = _userDao.findUserFromId(user.getId().intValue());
            assertEquals(user.getUserProfile().getNumSchoolChildren(), new Integer(2));

        } finally {
            _userDao.removeUser(user.getId());
        }
    }

    public void testRemoveChild() throws NoSuchAlgorithmException {
        FollowUpCommand command = new FollowUpCommand();
        BindException errors = new BindException(command, "");
        User user = new User();
        UserProfile userProfile = new UserProfile();
        user.setEmail("RegistrationFollowUpControllerTest4@greatschools.net");
        _userDao.saveUser(user);

        try {
            userProfile.setUser(user);
            userProfile.setScreenName("screeny");
            userProfile.setNumSchoolChildren(new Integer(2));
            user.setUserProfile(userProfile);
            _userDao.updateUser(user);

            user = _userDao.findUserFromId(user.getId().intValue());
            assertEquals(user.getUserProfile().getNumSchoolChildren(), new Integer(2));

            command.setUser(user);

            String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
            getRequest().addParameter("marker", hash);
            getRequest().addParameter("removeChild", "removeChild");
            _controller.onBindAndValidate(getRequest(), command, errors);
            assertTrue(errors.hasErrors());

            user = _userDao.findUserFromId(user.getId().intValue());
            assertEquals(user.getUserProfile().getNumSchoolChildren(), new Integer(1));

        } finally {
            _userDao.removeUser(user.getId());
        }
    }

    /**
     * Test that if a student is in the command, that student is added to the user and updateUser
     * is called on the dao. Uses mock so doesn't ever touch the DB.
     */
    public void testaddStudent() {
        IUserDao oldDao = _controller.getUserDao();
        FollowUpCommand command = new FollowUpCommand();
        BindException errors = new BindException(command, "");
        User user = new User();
        UserProfile userProfile = new UserProfile();
        user.setEmail("RegistrationFollowUpControllerTest5@greatschools.net");
        userProfile.setUser(user);
        userProfile.setNumSchoolChildren(new Integer(1));
        user.setUserProfile(userProfile);

        command.setUser(user);
        Student student = new Student();
        command.addStudent(student);
        assertNull(user.getStudents());

        MockControl userControl = MockControl.createControl(IUserDao.class);
        IUserDao mockUserDao = (IUserDao) userControl.getMock();
        mockUserDao.updateUser(user);
        userControl.replay();
        try {
            _controller.setUserDao(mockUserDao);

            _controller.onSubmit(getRequest(), getResponse(), command, errors);
            assertFalse(errors.hasErrors());
            assertNotNull(user.getStudents());
            assertEquals(user.getStudents().iterator().next(), student);
        } finally {
            _controller.setUserDao(oldDao);
        }
    }

    public void testValidateStudent() throws NoSuchAlgorithmException {
        FollowUpCommand command = new FollowUpCommand();
        BindException errors = new BindException(command, "");
        User user = new User();
        UserProfile userProfile = new UserProfile();
        user.setEmail("RegistrationFollowUpControllerTest6@greatschools.net");
        _userDao.saveUser(user);

        try {
            userProfile.setUser(user);
            userProfile.setScreenName("screeny");
            userProfile.setNumSchoolChildren(new Integer(1));
            user.setUserProfile(userProfile);
            _userDao.updateUser(user);

            command.setUser(user);

            String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
            getRequest().addParameter("marker", hash);

            getRequest().addParameter("childname1", "Anthony");
            getRequest().addParameter("grade1", Grade.G_10.getName());
            getRequest().addParameter("state1", "CA");
            School school = _schoolDao.getSchoolById(State.CA, new Integer(1));
            getRequest().addParameter("schoolId1", String.valueOf(school.getId()));
            getRequest().addParameter("school1", school.getName());

            assertEquals(0, command.getNumStudents());
            // successful validation should insert the student into the command
            _controller.onBindAndValidate(getRequest(), command, errors);
            assertFalse(errors.hasErrors());
            assertEquals(1, command.getNumStudents());
            Student student = (Student) command.getStudents().get(0);
            assertEquals("Anthony", student.getName());
            assertEquals(Grade.G_10, student.getGrade());
            assertEquals(new Integer(1), student.getSchoolId());
            assertEquals(State.CA, student.getState());
        } finally {
            _userDao.removeUser(user.getId());
        }
    }

    public void testStudentNameLength() throws NoSuchAlgorithmException {
        FollowUpCommand command = new FollowUpCommand();
        BindException errors = new BindException(command, "");
        User user = new User();
        UserProfile userProfile = new UserProfile();
        user.setEmail("RegistrationFollowUpControllerTest7@greatschools.net");
        _userDao.saveUser(user);

        try {
            userProfile.setUser(user);
            userProfile.setScreenName("screeny");
            userProfile.setNumSchoolChildren(new Integer(1));
            user.setUserProfile(userProfile);
            _userDao.updateUser(user);

            command.setUser(user);

            String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
            getRequest().addParameter("marker", hash);

            getRequest().addParameter("childname1",
                    "123456789012345678901234567890123456789012345678901"); // too long: 51 chars
            getRequest().addParameter("grade1", Grade.G_10.getName());
            getRequest().addParameter("state1", "CA");
            School school = _schoolDao.getSchoolById(State.CA, new Integer(1));
            getRequest().addParameter("schoolId1", String.valueOf(school.getId()));
            getRequest().addParameter("school1", school.getName());

            _controller.onBindAndValidate(getRequest(), command, errors);
            assertTrue(errors.hasErrors());
            assertTrue(errors.hasFieldErrors("students[0]"));
        } finally {
            _userDao.removeUser(user.getId());
        }
    }

    public void testAboutMeLength() throws NoSuchAlgorithmException {
        FollowUpCommand command = new FollowUpCommand();
        BindException errors = new BindException(command, "");
        User user = new User();
        UserProfile userProfile = new UserProfile();
        user.setEmail("RegistrationFollowUpControllerTest8@greatschools.net");
        _userDao.saveUser(user);

        try {
            userProfile.setUser(user);
            userProfile.setScreenName("screeny");
            userProfile.setNumSchoolChildren(new Integer(1));
            user.setUserProfile(userProfile);
            _userDao.updateUser(user);

            command.setUser(user);

            String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
            getRequest().addParameter("marker", hash);

            StringBuffer aboutMeText = new StringBuffer();
            for (int x=0; x < RegistrationFollowUpController.ABOUT_ME_MAX_LENGTH + 1; x++) {
                aboutMeText.append("x");
            } // too long
            command.setAboutMe(aboutMeText.toString());
            _controller.onBindAndValidate(getRequest(), command, errors);
            assertTrue(errors.hasErrors());
            assertTrue(errors.hasFieldErrors("aboutMe"));
        } finally {
            _userDao.removeUser(user.getId());
        }
    }

    public void testOtherInterestLength() throws NoSuchAlgorithmException {
        FollowUpCommand command = new FollowUpCommand();
        BindException errors = new BindException(command, "");
        User user = new User();
        UserProfile userProfile = new UserProfile();
        user.setEmail("RegistrationFollowUpControllerTest9@greatschools.net");
        _userDao.saveUser(user);

        try {
            userProfile.setUser(user);
            userProfile.setScreenName("screeny");
            userProfile.setNumSchoolChildren(new Integer(1));
            user.setUserProfile(userProfile);
            _userDao.updateUser(user);

            command.setUser(user);

            String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
            getRequest().addParameter("marker", hash);

            StringBuffer otherText = new StringBuffer();
            for (int x=0; x < RegistrationFollowUpController.OTHER_INTEREST_MAX_LENGTH + 1; x++) {
                otherText.append("x");
            } // too long
            command.setOtherInterest(otherText.toString());
            _controller.onBindAndValidate(getRequest(), command, errors);
            assertTrue(errors.hasErrors());
            assertTrue(errors.hasFieldErrors("otherInterest"));
        } finally {
            _userDao.removeUser(user.getId());
        }
    }

    public void testDuplicatePreviousSchools() throws NoSuchAlgorithmException {
        FollowUpCommand command = new FollowUpCommand();
        BindException errors = new BindException(command, "");
        User user = new User();
        UserProfile userProfile = new UserProfile();
        user.setEmail("RegistrationFollowUpControllerTest10@greatschools.net");
        _userDao.saveUser(user);

        try {
            userProfile.setUser(user);
            userProfile.setScreenName("screeny");
            user.setUserProfile(userProfile);
            _userDao.updateUser(user);

            command.setUser(user);

            String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
            getRequest().addParameter("marker", hash);

            School school = _schoolDao.getSchoolById(State.CA, new Integer(1));
            getRequest().addParameter("previousSchool1", school.getName());
            getRequest().addParameter("previousSchoolId1", String.valueOf(school.getId()));
            getRequest().addParameter("previousState1", State.CA.getAbbreviation());
            getRequest().addParameter("previousSchool2", school.getName());
            getRequest().addParameter("previousSchoolId2", String.valueOf(school.getId()));
            getRequest().addParameter("previousState2", State.CA.getAbbreviation());

            _controller.onBindAndValidate(getRequest(), command, errors);
            assertFalse(errors.hasErrors());
            List subs = command.getSubscriptions();
            assertNotNull(subs);
            assertEquals(1, subs.size());
        } finally {
            _userDao.removeUser(user.getId());
        }
    }
}
