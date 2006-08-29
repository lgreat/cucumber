package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.community.Student;
import gs.data.util.DigestUtil;
import gs.data.school.Grade;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.BindException;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RegistrationFollowUpControllerTest extends BaseControllerTestCase {
    private RegistrationFollowUpController _controller;

    private IUserDao _userDao;
    private ISchoolDao _schoolDao;

    protected void setUp() throws Exception {
        super.setUp();
        ApplicationContext appContext = getApplicationContext();
        _controller = (RegistrationFollowUpController) appContext.getBean(RegistrationFollowUpController.BEAN_ID);
        _userDao = (IUserDao)appContext.getBean(IUserDao.BEAN_ID);
        _schoolDao = (ISchoolDao)appContext.getBean(ISchoolDao.BEAN_ID);
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
            getRequest().addParameter(RegistrationFollowUpController.INTEREST_CODES[0], "checked");

            String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
            getRequest().addParameter("marker", hash);
            _controller.onBindAndValidate(getRequest(), command, errors);
            assertFalse(errors.hasErrors());

            _controller.onSubmit(getRequest(), getResponse(), command, errors);
            assertFalse(errors.hasErrors());
            user = _userDao.findUserFromId(user.getId().intValue());
            userProfile = user.getUserProfile();
            assertEquals("My children are so unique!", userProfile.getAboutMe());
            assertTrue(userProfile.isPrivate());
            assertEquals("Other", userProfile.getOtherInterest());
            assertTrue(userProfile.getInterestsAsArray().length == 1);
            assertEquals(RegistrationFollowUpController.INTEREST_CODES[0],
                    userProfile.getInterestsAsArray()[0]);
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

        MockUserDao mockUserDao = new MockUserDao();
        try {
            _controller.setUserDao(mockUserDao);
            assertFalse(mockUserDao.hasUpdateBeenCalled());

            _controller.onSubmit(getRequest(), getResponse(), command, errors);
            assertFalse(errors.hasErrors());
            assertTrue(mockUserDao.hasUpdateBeenCalled());
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

    // TODO: convert to easymock
    private static class MockUserDao implements IUserDao {

        private boolean _updateCalled = false;

        public boolean hasUpdateBeenCalled() {
            return _updateCalled;
        }

        public void evict(User user) {
        }

        public User findUserFromEmail(String email) {
            return null;
        }

        public User findUserFromEmailIfExists(String email) {
            return findUserFromEmail(email);
        }

        public User findUserFromId(int i) {
            return null;
        }

        public void saveUser(User user) {
        }

        public void updateUser(User user) {
            _updateCalled = true;
        }

        public void removeUser(Integer integer) {
        }

        public List findUsersModifiedSince(Date date) {
            return null;
        }

        public List findUsersModifiedBetween(Date begin, Date end) {
            return null;
        }

        public User findUserFromScreenNameIfExists(String screenName) {
            return null;
        }
    }
}
