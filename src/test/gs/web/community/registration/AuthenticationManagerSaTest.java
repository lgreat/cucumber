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

    public void setUp() throws NoSuchAlgorithmException {
        _user = getUser(155803, "AuthenticationManagerSaTest@greatschools.net");
    }

    private User getUser(Integer id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        return user;
    }

    public void testGenerateUserHash() throws NoSuchAlgorithmException {
        String hash = AuthenticationManager.generateUserHash(_user);

        assertNotNull("Hash should be non-null", hash);
        assertEquals("Hash (" + hash + ") expected to be of specific length",
                DigestUtil.MD5_HASH_LENGTH, hash.length());

        // verify method works according to spec
        // Hash of User.SECRET_NUMBER concatenated with 155803 is tNWDVqW/+whaciBffVpUdA==
        assertEquals("Computed hash does not equal pre-computed hash. Perhaps User.SECRET_NUMBER has changed?",
                "tNWDVqW/+whaciBffVpUdA==", hash);
    }

    public void testGenerateCookieValue() throws NoSuchAlgorithmException {
        String cookieValue = AuthenticationManager.generateCookieValue(_user);

        assertNotNull("Cookie value should be non-null", cookieValue);
        assertEquals("Cookie value (" + cookieValue + ") expected to be of specific length",
                DigestUtil.MD5_HASH_LENGTH + String.valueOf(_user.getId()).length(), cookieValue.length());
        // verify method works according to spec
        // Hash of User.SECRET_NUMBER concatenated with 155803 is tNWDVqW/+whaciBffVpUdA==
        assertEquals("Cookie value (" + cookieValue + ") should consist of hash followed by user id",
                "tNWDVqW/+whaciBffVpUdA==155803", cookieValue);
    }

    public void testGetUserIdFromCookieValue() throws NoSuchAlgorithmException {
        String cookieValue = AuthenticationManager.generateCookieValue(_user);
        Integer userId = AuthenticationManager.getUserIdFromCookieValue(cookieValue);

        assertEquals(_user.getId(), userId);
        // verify method works according to spec
        assertEquals(new Integer(155803),
                AuthenticationManager.getUserIdFromCookieValue("tNWDVqW/+whaciBffVpUdA==155803"));
    }
}
