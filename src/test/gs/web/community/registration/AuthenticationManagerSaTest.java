package gs.web.community.registration;

import junit.framework.TestCase;
import gs.data.community.User;
import gs.data.util.DigestUtil;

import java.security.NoSuchAlgorithmException;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class AuthenticationManagerSaTest extends TestCase {

    private User _user;
    private AuthenticationManager _authManager;

    public void setUp() throws NoSuchAlgorithmException {
        _user = getUser(155803, "AuthenticationManagerSaTest@greatschools.net");
        _authManager = new AuthenticationManager();
    }

    private User getUser(Integer id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        return user;
    }

    public void testGenerateUserHash() throws NoSuchAlgorithmException {
        String hash = _authManager.generateUserHash(_user);

        assertNotNull("Hash should be non-null", hash);
        assertEquals("Hash (" + hash + ") expected to be of specific length",
                DigestUtil.MD5_HASH_LENGTH, hash.length());
    }

    public void testGenerateCookieValue() throws NoSuchAlgorithmException {
        String cookieValue = _authManager.generateCookieValue(_user);

        assertNotNull("Cookie value should be non-null", cookieValue);
        assertEquals("Cookie value (" + cookieValue + ") expected to be of specific length",
                DigestUtil.MD5_HASH_LENGTH + String.valueOf(_user.getId()).length(), cookieValue.length());
    }

    public void testGetUserIdFromCookieValue() throws NoSuchAlgorithmException {
        String cookieValue = _authManager.generateCookieValue(_user);
        Integer userId = _authManager.getUserIdFromCookieValue(cookieValue);

        assertEquals(_user.getId(), userId);
    }
}
