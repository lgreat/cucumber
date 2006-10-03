package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.util.DigestUtil;
import gs.web.BaseControllerTestCase;
import org.easymock.MockControl;

import java.security.NoSuchAlgorithmException;

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
    private MockControl _userControl;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new RegistrationRemoveController();

        _userControl = MockControl.createControl(IUserDao.class);
        _userDao = (IUserDao) _userControl.getMock();

        _controller.setUserDao(_userDao);
    }

    public void testRegistrationRemove() throws Exception {
        // 1) create user record with non-validated password
        User user = new User();
        user.setEmail("testRegistrationRemove@greatschools.net");
        user.setId(new Integer(135));

        UserProfile userProfile = new UserProfile();
        userProfile.setScreenName("screeny");
        userProfile.setUser(user);
        user.setUserProfile(userProfile);
        user.setPlaintextPassword("foobar");
        user.setEmailProvisional();
        assertFalse(user.isPasswordEmpty());
        assertNotNull(user.getUserProfile());

        // 2) generate hash for user from email/id, add to request
        String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
        String id = hash + user.getId();
        getRequest().addParameter("id", id);

        _userControl.expectAndReturn(_userDao.findUserFromId(135), user);
        _userDao.updateUser(user);
        _userControl.replay();

        // 3) call handleRequestInternal
        _controller.handleRequestInternal(getRequest(), getResponse());
        // 4) verify no errors
        _userControl.verify();

        // 5) verify that password has become empty
        assertTrue(user.isPasswordEmpty());
        assertNull(user.getUserProfile());
    }

    public void testRegistrationRemoveBadHash() throws NoSuchAlgorithmException {
        // 1) create user record with non-validated password
        User user = new User();
        user.setEmail("testRegistrationRemove@greatschools.net");
        user.setId(new Integer(135));
        user.setPlaintextPassword("foobar");
        user.setEmailProvisional();

        // 2) generate hash for user from email/id, add to request
        Integer badId = new Integer(user.getId().intValue()+1);
        String hash = DigestUtil.hashStringInt(user.getEmail(), badId);
        String id = hash + user.getId();
        getRequest().addParameter("id", id);

        _userControl.expectAndReturn(_userDao.findUserFromId(135), user);
        _userControl.replay();

        // 3) call handleRequestInternal
        assertTrue(user.isEmailProvisional());
        try {
            _controller.handleRequestInternal(getRequest(), getResponse());
            fail("Did not get expected exception when trying to cancel registration with fake hash");
        } catch (Exception ex) {
            // good
        }
        _userControl.verify();
        // 4) verify no change
        assertTrue(user.isEmailProvisional());
    }

    public void testNoPasswordUser() throws Exception {
        // 1) create user record with no password
        User user = new User();
        user.setEmail("testRegistrationRemove@greatschools.net");
        user.setId(new Integer(135));

        assertTrue(user.isPasswordEmpty());
        assertFalse(user.isEmailProvisional());

        // 2) generate hash for user from email/id, add to request
        String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
        String id = hash + user.getId();
        getRequest().addParameter("id", id);

        _userControl.expectAndReturn(_userDao.findUserFromId(135), user);
        _userControl.replay();

        // 3) call handleRequestInternal
        _controller.handleRequestInternal(getRequest(), getResponse());
        // 4) verify no errors
        _userControl.verify();

        // 5) verify no change
        assertTrue(user.isPasswordEmpty());
        assertFalse(user.isEmailProvisional());
    }
}
