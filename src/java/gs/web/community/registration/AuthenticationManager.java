package gs.web.community.registration;

import gs.data.community.User;
import gs.data.util.DigestUtil;

import java.util.Date;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class AuthenticationManager {
    protected final Log _log = LogFactory.getLog(getClass());

    private long _timeout = 1000 * 60 * 5; // 5 minutes
    private String _parameterName = "authInfo"; // default
    private int _userIdLength = 10; // default
    public static final String WEBCROSSING_FORWARD_URL = "http://community.greatschools.net/entry/authInfo.";

    /**
     * Generates an AuthInfo object for the given user.
     * @param user
     * @return AuthInfo object for user
     * @throws NoSuchAlgorithmException
     */
    public AuthInfo generateAuthInfo(User user) throws NoSuchAlgorithmException {
        Integer userId = user.getId();
        String email = user.getEmail();
        Date now = new Date();
        Object[] hashInput = new Object[] {userId, email, now};
        String hash = DigestUtil.hashObjectArray(hashInput);
        return new AuthInfo(userId, hash, now);
    }

    /**
     * Helper method that parses the passed in String into an AuthInfo object, then
     * delegates to the other verifyAuthInfo method.
     * @param user
     * @param authInfo parameter value to be parsed into an AuthInfo object
     * @return true if the authentication information checks out, false otherwise
     * @throws NoSuchAlgorithmException
     * @throws NumberFormatException if the string cannot be parsed into a hash, an id, and a long
     */
    public boolean verifyAuthInfo(User user, String authInfo) throws NoSuchAlgorithmException {
        if (authInfo == null) {
            return false;
        } else if (authInfo.length() < DigestUtil.MD5_HASH_LENGTH+11) {
            return false;
        }
        String hash = authInfo.substring(0, DigestUtil.MD5_HASH_LENGTH);
        String dateString = authInfo.substring(DigestUtil.MD5_HASH_LENGTH+10);
        Integer id = getUserIdFromParameter(authInfo);
        Long ms = new Long(dateString);
        Date timestamp = new Date(ms.longValue());
        return verifyAuthInfo(user, new AuthInfo(id, hash, timestamp));
    }

    /**
     * Verifies that the given AuthInfo object is valid for the given user.
     * @param user
     * @param authInfo
     * @return true if the authentication information checks out, false otherwise
     * @throws NoSuchAlgorithmException
     */
    public boolean verifyAuthInfo(User user, AuthInfo authInfo) throws NoSuchAlgorithmException {
        String email = user.getEmail();
        Object[] hashInput = new Object[] {user.getId(), email, authInfo.getTimestamp()};
        String hash = DigestUtil.hashObjectArray(hashInput);
        if (hash.equals(authInfo.getHash())) {
            Date now = new Date();
            long timeDiff = now.getTime() - authInfo.getTimestamp().getTime();
            if (timeDiff < getTimeout()) {
                return true;
            } else {
                _log.warn("Authentication request fails timeout check: timeDiff=" + timeDiff +
                        ", should be less than " + getTimeout());
            }
        } else {
            _log.warn("Authentication request fails hash check: " + email + "(" + authInfo.getUserId() +
                    ") - " + authInfo.getTimestamp());
        }
        return false;
    }

    /**
     * If the original url looks like it belongs to a site that would want proof-of-authentication,
     * add that proof to the end of the URL.
     * @param originalUrl
     * @param authInfo
     * @return new url string
     */
    public String addParameterIfNecessary(String originalUrl, AuthInfo authInfo) {
        StringBuffer rval = new StringBuffer();
        if (!originalUrl.startsWith("http")) {
            // special case code for webcrossing
            rval.append(WEBCROSSING_FORWARD_URL);
            rval.append(getParameterValue(authInfo));
            rval.append(originalUrl);
            _log.info("Setting redirect URL to: " + rval.toString());
            return rval.toString(); // exit early
        }

        // for URLs that look normal
        rval.append(originalUrl);
        if (originalUrl.toLowerCase().indexOf("community.greatschools.net") > -1 ||
                originalUrl.toLowerCase().indexOf("localhost") > -1) {
            if (originalUrl.indexOf("!") > -1) {
                rval.append("&");
            } else {
                rval.append("!");
            }
            rval.append(getParameterName());
            rval.append("=");
            rval.append(getParameterValue(authInfo));
        }

        return rval.toString();
    }

    /**
     * Converts an AuthInfo object into a parameter string.
     * @param authInfo
     * @return string representing the AuthInfo object
     */
    public String getParameterValue(AuthInfo authInfo) {
        StringBuffer rval = new StringBuffer();
        rval.append(authInfo.getHash());
        rval.append(StringUtils.leftPad(authInfo.getUserId().toString(),getUserIdLength(),'0'));
        rval.append(authInfo.getTimestamp().getTime());
        return rval.toString();
    }

    /**
     * Parses the user id out of the string. If the string is not well-formed, various exceptions
     * could be thrown.
     * @param authInfo
     * @return user id
     */
    public Integer getUserIdFromParameter(String authInfo) {
        String idString = authInfo.substring(DigestUtil.MD5_HASH_LENGTH, DigestUtil.MD5_HASH_LENGTH+10);
        return new Integer(idString);
    }

    public long getTimeout() {
        return _timeout;
    }

    public void setTimeout(long timeout) {
        _timeout = timeout;
    }

    public String getParameterName() {
        return _parameterName;
    }

    public void setParameterName(String parameterName) {
        _parameterName = parameterName;
    }

    public int getUserIdLength() {
        return _userIdLength;
    }

    public void setUserIdLength(int userIdLength) {
        _userIdLength = userIdLength;
    }

    public static class AuthInfo {
        Integer _userId;
        String _hash;
        Date _timestamp;

        public AuthInfo(Integer userId, String hash, Date timestamp) {
            setUserId(userId);
            setHash(hash);
            setTimestamp(timestamp);
        }

        public Integer getUserId() {
            return _userId;
        }

        public void setUserId(Integer userId) {
            _userId = userId;
        }

        public String getHash() {
            return _hash;
        }

        public void setHash(String hash) {
            _hash = hash;
        }

        public Date getTimestamp() {
            return _timestamp;
        }

        public void setTimestamp(Date timestamp) {
            _timestamp = timestamp;
        }
    }
}
