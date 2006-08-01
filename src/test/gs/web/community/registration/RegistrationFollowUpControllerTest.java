package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.data.community.IUserDao;
import gs.data.community.IUserProfileDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.util.DigestUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.BindException;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RegistrationFollowUpControllerTest extends BaseControllerTestCase {
    private RegistrationFollowUpController _controller;

    private IUserDao _userDao;
    private IUserProfileDao _userProfileDao;

    protected void setUp() throws Exception {
        super.setUp();
        ApplicationContext appContext = getApplicationContext();
        _controller = (RegistrationFollowUpController) appContext.getBean(RegistrationFollowUpController.BEAN_ID);
        _userDao = (IUserDao)appContext.getBean(IUserDao.BEAN_ID);
        _userProfileDao = (IUserProfileDao)appContext.getBean(IUserProfileDao.BEAN_ID);
    }

    public void testRegistrationFollowUp() throws Exception {
        FollowUpCommand command = new FollowUpCommand();
        BindException errors = new BindException(command, "");
        User user = new User();
        UserProfile userProfile = new UserProfile();
        user.setEmail("RegistrationFollowUpControllerTest@greatschools.net");
        _userDao.saveUser(user);

        try {
            userProfile.setUser(user);
            _userProfileDao.saveUserProfile(userProfile);
            user.setUserProfile(userProfile);
            _userDao.updateUser(user);

            command.setAboutMe("My children are so unique!");
            command.setPrivate(false);
            command.setUser(user);

            String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
            getRequest().addParameter("marker", hash);
            _controller.onBindAndValidate(getRequest(), command, errors);
            assertFalse(errors.hasErrors());

            _controller.onSubmit(getRequest(), getResponse(), command, errors);
            assertFalse(errors.hasErrors());
            userProfile = _userProfileDao.findUserProfileFromId(user.getId());
            assertEquals("My children are so unique!", userProfile.getAboutMe());
            assertFalse(userProfile.isPrivate());
            _userProfileDao.removeUserProfile(user.getId());
        } finally {
            _userDao.removeUser(user.getId());
        }
    }

    public void testBadHash() throws Exception {
        FollowUpCommand command = new FollowUpCommand();
        BindException errors = new BindException(command, "");
        User user = new User();
        UserProfile userProfile = new UserProfile();
        user.setEmail("RegistrationFollowUpControllerTest@greatschools.net");
        _userDao.saveUser(user);

        try {
            userProfile.setUser(user);
            _userProfileDao.saveUserProfile(userProfile);
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
}
