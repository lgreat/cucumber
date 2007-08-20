/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: ClientSideSessionCache.java,v 1.11 2007/08/20 18:41:58 aroy Exp $
 */

package gs.web.community;

import gs.data.community.Subscription;
import gs.data.community.SubscriptionProduct;
import gs.data.community.User;
import gs.data.state.State;
import gs.web.community.registration.AuthenticationManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.security.NoSuchAlgorithmException;

/**
 * Provides summary information about the user. Can be initialized either from
 * a user object or from a set of values. Normal usage is to create one when
 * the user first logs in, and then persist it client-side as a
 * session cookie. When a new request comes in, a ClientSideSessionCache will created
 * from the cookie. This class is Externalizable, and that value is appropriate
 * to stick in a cookie.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class ClientSideSessionCache {
    private String _nickname;
    private String _email;
    private Integer _memberId;
    private int _mslCount;
    private String _userHash;
    private String _screenName;

    private static final Log _log = LogFactory.getLog(ClientSideSessionCache.class);

    /**
     * of {@link #createMssKey(gs.data.state.State, int)} values
     * May be null.
     */
    private Set _mss;

    /**
     * of {@link SubscriptionProduct#getName() values}
     * May be null.
     */
    private Set _nonMss;


    private static final String COOKIE_LIST_DELIMETER = ",";
    private static final String INTRA_COOKIE_DELIMETER = ";";
    private static final String COOKIE_ENCODING = "ISO-8859-1";

    public ClientSideSessionCache() {
    }

    /**
     * Creates a ClientSideSessionCache from the specified cookie.
     * Returns null if it can't create the object for some reason.
     *
     * @see #getCookieRepresentation()
     */
    public static ClientSideSessionCache createClientSideSessionCache(String cookie) {
        try {
            final ClientSideSessionCache sessionCache = new ClientSideSessionCache();
            sessionCache.readFromCookie(cookie);
            return sessionCache;
        } catch (IOException e) {
            _log.info("Unparsable cookie: " + cookie);
        } catch (IllegalArgumentException e) {
            _log.info("Unparsable cookie: " + cookie);
        } catch (ClassNotFoundException e) {
            _log.info("Unparsable cookie: " + cookie);
        }
        return null;
    }

    /**
     * @noinspection FeatureEnvy
     */
    public ClientSideSessionCache(User user) {
        _memberId = user.getId();
        _email = user.getEmail();
        final String[] p = _email.split("@");
        _nickname = p[0];
        _screenName = (user.getUserProfile() != null)?user.getUserProfile().getScreenName():null;
        try {
            _userHash = AuthenticationManager.generateCookieValue(user);
        } catch (NoSuchAlgorithmException e) {
            // ignore
        }

        // Count their subscriptions
        final Set subscriptions = user.getSubscriptions();
        if (subscriptions != null) {
            _mss = new HashSet();
            _nonMss = new HashSet();
            for (Iterator iter = subscriptions.iterator(); iter.hasNext();) {
                Subscription sub = (Subscription) iter.next();
                if (sub.getProduct().equals(SubscriptionProduct.MYSTAT)) {
                    _mss.add(createMssKey(sub.getState(), sub.getSchoolId()));
                } else {
                    _nonMss.add(sub.getProduct().getName());
                }
            }
        }

        _mslCount = user.getFavoriteSchools() != null ? user.getFavoriteSchools().size() : 0;
    }

    /**
     * Creates a unique name for the MSS subscription. Really just CA1234, concatenating
     * the school ID on the back of the state.
     */
    private String createMssKey(State state, int schoolId) {
        return state.getAbbreviation() + schoolId;
    }

    public String getUserHash() {
        return _userHash;
    }

    public void setUserHash(String userHash) {
        this._userHash = userHash;
    }

    public String getScreenName() {
        return _screenName;
    }

    public void setScreenName(String screenName) {
        _screenName = screenName;
    }

    public int getMslCount() {
        return _mslCount;
    }

    public String getEmail() {
        return _email;
    }

    public String getNickname() {
        return _nickname;
    }

    public boolean hasMssSubscription(State state, int schoolId) {
        if (_mss == null) {
            return false;
        }
        String mssHash = createMssKey(state, schoolId);
        return _mss.contains(mssHash);
    }

    /**
     * Returns true if the user has one (or more) of the given subscriptions.
     * Doesn't check for "expiration" and other constraints. Returns true
     * generically for MSS subscriptions, and those must be checked against
     * a specific school using {@link #hasMssSubscription(gs.data.state.State, int)}.
     */
    public boolean hasSubscription(SubscriptionProduct product) {
        if (SubscriptionProduct.MYSTAT.equals(product)) {
            return _mss != null && !_mss.isEmpty();
        } else {
            return _nonMss != null && _nonMss.contains(product.getName());
        }
    }

    public int getMssCount() {
        return _mss == null ? 0 : _mss.size();
    }

    /* package visible for unit test access */
    String getMssCookie() {
        return _mss == null ? "" : StringUtils.join(_mss.iterator(), COOKIE_LIST_DELIMETER);
    }


    /* package visible for unit test access */
    void setMssCookie(String cookie) {
        String[] p = cookie.split(COOKIE_LIST_DELIMETER);
        if (p.length > 0) {
            _mss = new HashSet();
            for (int i = 0; i < p.length; i++) {
                String s = p[i];
                if (s.length() > 2) {
                    _mss.add(s);
                }
            }
        } else {
            _mss = null;
        }
    }

    /* package visible for unit test access */
    void setNonMssCookie(String cookie) {
        String[] p = cookie.split(COOKIE_LIST_DELIMETER);
        if (p.length > 0) {
            _nonMss = new HashSet();
            for (int i = 0; i < p.length; i++) {
                String s = p[i];
                SubscriptionProduct prod = SubscriptionProduct.getSubscriptionProduct(s);
                if (prod != null) {
                    _nonMss.add(prod.getName());
                }
            }
        } else {
            _nonMss = null;
        }
    }


    /* package visible for unit test access */
    String getNonMssCookie() {
        return _nonMss == null ? "" : StringUtils.join(_nonMss.iterator(), COOKIE_LIST_DELIMETER);
    }

    /**
     * Represent this object as a cookie value.
     * <br />
     * Netscape's cookie definition says "This string is a sequence of
     * characters excluding semi-colon, comma and white space. If there is a need
     * to place such data in the name or value, some encoding method such as
     * URL style %XX encoding is recommended, though no encoding is defined
     * or required."
     * <br />
     * I had some problems with this, detailed at the web reference below.
     * <br />
     * Version 3 adds user hash for community authentication.
     * <br />
     * Version 4 adds community screen name for nav header
     *
     * @see <a href="http://mail-archives.apache.org/mod_mbox/tomcat-dev/200210.mbox/%3CMGYSZTA6HD9375D8FBB0YSGAKE3X85C7.3d99546e@RAURAMO%3E">forum message</a>
     */
    public String getCookieRepresentation() {
        StringBuffer b = new StringBuffer();
        b.append("4"); // version
        b.append(INTRA_COOKIE_DELIMETER);
        b.append(_email);
        b.append(INTRA_COOKIE_DELIMETER);
        b.append(_nickname);
        b.append(INTRA_COOKIE_DELIMETER);
        b.append(getMssCookie());
        b.append(INTRA_COOKIE_DELIMETER);
        b.append(getNonMssCookie());
        b.append(INTRA_COOKIE_DELIMETER);
        b.append(Integer.toString(getMslCount()));
        b.append(INTRA_COOKIE_DELIMETER);
        b.append(getMemberId().toString());
        b.append(INTRA_COOKIE_DELIMETER);
        b.append( (getUserHash() == null)?"":getUserHash()); // 7
        b.append(INTRA_COOKIE_DELIMETER);
        b.append( (getScreenName() == null)?"":getScreenName()); // 8
        String cookie = b.toString();
        String encoded;
        try {
            encoded = URLEncoder.encode(cookie, COOKIE_ENCODING);
        } catch (UnsupportedEncodingException e) {
            encoded = cookie;
            _log.error("Cannot encode the cookie using specified encoding.");
        }
        return encoded;
    }


    public void readFromCookie(final String cookie) throws IOException, ClassNotFoundException {
        final String decoded = URLDecoder.decode(cookie, COOKIE_ENCODING);
        String[] s = StringUtils.splitPreserveAllTokens(decoded, INTRA_COOKIE_DELIMETER);
        if (s.length < 6) {
            throw new IllegalArgumentException("Not enough components to the cookie: " + cookie + " (" + decoded + ")");
        }
        int version = Integer.parseInt(s[0]);
        _email = s[1];
        _nickname = s[2];
        String mssCookie = StringUtils.trimToEmpty(s[3]);
        setMssCookie(mssCookie);
        String nonMssCookie = StringUtils.trimToEmpty(s[4]);
        setNonMssCookie(nonMssCookie);
        _mslCount = Integer.parseInt(s[5]);
        if (version > 1) {
            _memberId = new Integer(s[6]);
        }
        if (version > 2) {
            _userHash = s[7];
        }
        if (version > 3) {
            _screenName = s[8];
        }
    }

    public Integer getMemberId() {
        return _memberId;
    }
}
