package gs.web.community.registration;

import gs.data.community.User;
import gs.data.util.DigestUtil;

import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class AuthenticationManager {
    protected final Log _log = LogFactory.getLog(getClass());

    public static final String BEAN_ID = "authenticationManager";

    /**
     * Generates an md5 hash identifying the given user.
     *
     * @param user User to generate hash for
     * @throws NoSuchAlgorithmException On fatal error running the md5 algorithm
     * @return the md5 hash for this user
     */
    protected static String generateUserHash(User user) throws NoSuchAlgorithmException {
        Integer userId = user.getId();
        String convertValue = String.valueOf(User.SECRET_NUMBER) + String.valueOf(userId);
        return DigestUtil.hashString(convertValue);
    }

    public static String generateCookieValue(User user) throws NoSuchAlgorithmException {
        return AuthenticationManager.generateUserHash(user) + String.valueOf(user.getId());
    }

    /**
     * Parses the user id out of a string. The string is assumed to begin with an md5 hash
     * and be immediately followed by the user id.
     * @return user id
     * @param cookieValue String consisting of an md5 hash concatenated with the user id
     */
    public static Integer getUserIdFromCookieValue(String cookieValue) {
        String myCookieValue = StringUtils.replace(cookieValue, "\\\"", "");
        myCookieValue = StringUtils.replace(myCookieValue, "\"", "");
        String idString = myCookieValue.substring(DigestUtil.MD5_HASH_LENGTH);
        return new Integer(idString);
    }

    /**
     * Parses the user id out of a string. The string is assumed to begin with an md5 hash
     * and be immediately followed by the user id.
     * @return user id
     * @param cookieValue String consisting of an md5 hash concatenated with the user id
     */
    public static String getHashFromCookieValue(String cookieValue) {
        String myCookieValue = StringUtils.replace(cookieValue, "\\\"", "");
        myCookieValue = StringUtils.replace(myCookieValue, "\"", "");
        return myCookieValue.substring(0, DigestUtil.MD5_HASH_LENGTH);
    }
}
