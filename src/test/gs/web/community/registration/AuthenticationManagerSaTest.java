package gs.web.community.registration;

import junit.framework.TestCase;
import gs.data.community.User;

import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class AuthenticationManagerSaTest extends TestCase {

    private User _user;
    private AuthenticationManager _authManager;
    private AuthenticationManager.AuthInfo _authInfo;

    public void setUp() throws NoSuchAlgorithmException {
        _user = getUser(new Integer(155803), "AuthenticationManagerSaTest@greatschools.net");
        _authManager = new AuthenticationManager();
         _authInfo = _authManager.generateAuthInfo(_user);
    }

    private User getUser(Integer id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        return user;
    }

    public void testGenerateAuthInfo() throws NoSuchAlgorithmException {
        assertNotNull(_authInfo);
        assertEquals(_user.getId(), _authInfo.getUserId());
        assertNotNull(_authInfo.getHash());
        assertNotNull(_authInfo.getTimestamp());
    }

    public void testVerifyAuthInfo() throws NoSuchAlgorithmException {
        assertTrue(_authManager.verifyAuthInfo(_user, _authInfo));

        User user2 = getUser(new Integer(_user.getId().intValue()+1), _user.getEmail());
        assertFalse(_authManager.verifyAuthInfo(user2, _authInfo));

        User user3 = getUser(_user.getId(), "a" + _user.getEmail());
        assertFalse(_authManager.verifyAuthInfo(user3, _authInfo));

        Date origDate = _authInfo.getTimestamp();
        try {
            _authInfo.setTimestamp(new Date(origDate.getTime() + 5000));
            assertFalse(_authManager.verifyAuthInfo(_user, _authInfo));
        } finally {
            _authInfo.setTimestamp(origDate);
        }

        long origTimeout = _authManager.getTimeout();
        _authManager.setTimeout(50);

        try {
            Thread.sleep(100);
            assertFalse(_authManager.verifyAuthInfo(_user, _authInfo));
        } catch (InterruptedException e) {
            fail("Thread sleep interrupted");
        } finally {
            _authManager.setTimeout(origTimeout);
        }
    }

    public void testGetParameterValue() throws NoSuchAlgorithmException {
        String value = _authManager.getParameterValue(_authInfo);
        assertNotNull(value);

        Integer id = _authManager.getUserIdFromParameter(value);
        assertEquals(_user.getId(), id);
    }

    public void testVerifyAuthInfoString() throws NoSuchAlgorithmException {
        String paramString = _authManager.getParameterValue(_authInfo);
        assertTrue(_authManager.verifyAuthInfo(_user, paramString));

        assertFalse(_authManager.verifyAuthInfo(_user, (String)null));
        assertFalse(_authManager.verifyAuthInfo(_user, ""));
    }

    public void testNoAddParameter() throws NoSuchAlgorithmException {
        String url = "http://www.greatschools.net/article.page?id=500";
        String newUrl = _authManager.addParameterIfNecessary(url, _authInfo);

        assertNotNull(newUrl);
        assertEquals(url, newUrl);
    }

    public void testAddParameter() throws NoSuchAlgorithmException {
        String url = "/?14@@.598dae0f";
        String newUrl = _authManager.addParameterIfNecessary(url, _authInfo);

        assertNotNull(newUrl);
        assertFalse(url.equals(newUrl));
    }
}
