package gs.web.community.registration;

import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.web.BaseTestCase;

import static gs.web.community.registration.LoginValidatorHelper.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class LoginValidatorHelperTest extends BaseTestCase {
    private User _provisionalUser;
    private User _noPasswordUser;
    private User _deactivatedUser;
    private User _registeredUser;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _provisionalUser = new User();
        _provisionalUser.setId(1);
        _provisionalUser.setPlaintextPassword("foobar");
        _provisionalUser.setEmailProvisional("foobar");
        _provisionalUser.setUserProfile(new UserProfile());

        _noPasswordUser = new User();
        _noPasswordUser.setId(2);

        _deactivatedUser = new User();
        _deactivatedUser.setId(3);
        _deactivatedUser.setPlaintextPassword("foobar");
        _deactivatedUser.setUserProfile(new UserProfile());
        _deactivatedUser.getUserProfile().setActive(false);

        _registeredUser = new User();
        _registeredUser.setId(4);
        _registeredUser.setPlaintextPassword("foobar");
        _registeredUser.setUserProfile(new UserProfile());
    }

    public void testNoSuchUser() {
        assertTrue(noSuchUser(null, true));
        assertTrue(noSuchUser(null, false));
        assertTrue("No such thing as a provisional user when we don't have double opt-in",
                   noSuchUser(_provisionalUser, false));
        assertFalse(noSuchUser(_provisionalUser, true));
        assertFalse(noSuchUser(_noPasswordUser, true));
        assertFalse(noSuchUser(_noPasswordUser, false));
        assertFalse(noSuchUser(_deactivatedUser, true));
        assertFalse(noSuchUser(_deactivatedUser, false));
        assertFalse(noSuchUser(_registeredUser, true));
        assertFalse(noSuchUser(_registeredUser, false));
    }

    public void testUserNotValidated() {
        assertTrue(userNotValidated(_provisionalUser, true));
        assertFalse("No such thing as a provisional user when we don't have double opt-in",
                    userNotValidated(_provisionalUser, false));
        assertFalse(userNotValidated(_noPasswordUser, true));
        assertFalse(userNotValidated(_noPasswordUser, true));
        assertFalse(userNotValidated(_deactivatedUser, true));
        assertFalse(userNotValidated(_deactivatedUser, false));
        assertFalse(userNotValidated(_registeredUser, true));
        assertFalse(userNotValidated(_registeredUser, false));
    }

    public void testUserNoPassword() {
        assertTrue(userNoPassword(_noPasswordUser));
        assertFalse(userNoPassword(_deactivatedUser));
        assertFalse(userNoPassword(_registeredUser));
    }

    public void testUserDeactivated() {
        assertTrue(userDeactivated(_deactivatedUser));
        assertFalse(userDeactivated(_registeredUser));
    }

    public void testPasswordMismatch() throws Exception {
        assertTrue(passwordMismatch(_registeredUser, "barfoo"));
        assertTrue(passwordMismatch(_registeredUser, ""));
        assertTrue(passwordMismatch(_registeredUser, null));
        assertFalse(passwordMismatch(_registeredUser, "foobar"));
    }
}
