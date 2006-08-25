package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.util.DigestUtil;
import gs.web.BaseControllerTestCase;
import org.springframework.context.ApplicationContext;

/**
 * Created by IntelliJ IDEA.
 * User: UrbanaSoft
 * Date: Jul 11, 2006
 * Time: 4:34:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class RegistrationRemoveControllerTest extends BaseControllerTestCase {
    private RegistrationRemoveController _controller;

    private IUserDao _userDao;

    protected void setUp() throws Exception {
        super.setUp();
        ApplicationContext appContext = getApplicationContext();
        _controller = (RegistrationRemoveController) appContext.getBean(RegistrationRemoveController.BEAN_ID);

        _userDao = (IUserDao)appContext.getBean(IUserDao.BEAN_ID);
    }

    public void testRegistrationRemove() {
        // 1) create user record with non-validated password
        User user = new User();
        user.setEmail("testRegistrationRemove@greatschools.net");
        _userDao.saveUser(user);
        try {
            UserProfile userProfile = new UserProfile();
            userProfile.setScreenName("screeny");
            userProfile.setUser(user);
            user.setUserProfile(userProfile);
            user.setPlaintextPassword("foobar");
            user.setEmailProvisional();
            _userDao.updateUser(user);

            // 2) generate hash for user from email/id, add to request
            String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
            String id = hash + user.getId();
            getRequest().addParameter("id", id);

            // 3) call handleRequestInternal
            _controller.handleRequestInternal(getRequest(), getResponse());
            // 4) verify no errors
            // no-op

            // 5) verify that password has become empty
            assertTrue(_userDao.findUserFromId(user.getId().intValue()).isPasswordEmpty());
            assertNull(_userDao.findUserFromId(user.getId().intValue()).getUserProfile());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            // 6) remove user record (finally block)
            try {
                _userDao.removeUser(user.getId());
            }   catch (Exception e) {
                _log.error(e);
            }
        }
    }
    public void testRegistrationRemoveBadHash() {
        // 1) create user record with non-validated password
        User user = new User();
        user.setEmail("testRegistrationRemove@greatschools.net");
        _userDao.saveUser(user);
        try {
            user.setPlaintextPassword("foobar");
            user.setEmailProvisional();
            _userDao.updateUser(user);

            // 2) generate hash for user from email/id, add to request
            Integer badId = new Integer(user.getId().intValue()+1);
            String hash = DigestUtil.hashStringInt(user.getEmail(), badId);
            String id = hash + user.getId();
            getRequest().addParameter("id", id);

            // 3) call handleRequestInternal
            try {
                _controller.handleRequestInternal(getRequest(), getResponse());
                fail("Did not get expected exception when trying to cancel registration with fake hash");
            } catch (Exception ex) {
                // good
            }
            // 4) verify no change
            assertTrue(_userDao.findUserFromId(user.getId().intValue()).isEmailProvisional());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            // 6) rollback DB changes
            _userDao.removeUser(user.getId());
        }
    }
}
