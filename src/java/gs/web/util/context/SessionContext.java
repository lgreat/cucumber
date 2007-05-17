/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SessionContext.java,v 1.13 2007/05/17 20:41:59 dlee Exp $
 */
package gs.web.util.context;

import gs.data.admin.IPropertyDao;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.state.State;
import gs.data.util.DigestUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.ObjectRetrievalFailureException;

import java.io.Serializable;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;

/**
 * The purpose is to hold common "global" properties for a user throughout their
 * session. It's a facade over the regular session, provide type safety and
 * whatever integrity guarantees we need to add. This class is wired to always
 * be available to your page (via Spring), so you don't have to defensively check for null.
 * Additionally, we can enforce rules like "the user's current geographic state is available",
 * and not mess with checks to make sure values are in the session. See {@link #getStateOrDefault()} for
 * an example of this.
 * Finally, this class gets called at the beginning of each request, and can
 * perform global operations like changing the user's state, host or cobrand.
 *
 * @author Andrew J. Peterson <mailto:apeterson@greatschools.net>
 * @see SessionContextInterceptor
 */
public class SessionContext implements ApplicationContextAware, Serializable {
    public static final String REQUEST_ATTRIBUTE_NAME = "context";

    public static final String BEAN_ID = "sessionContext";

    private static final long serialVersionUID = -314159265358979323L;

    private static final Log _log = LogFactory.getLog(SessionContextInterceptor.class);

    /**
     * The name of the cobrand (sfgate, azcentral, dps, etc...) or null
     */
    private String _cobrand;
    private String _hostName;
    /**
     * Current user, if known. This does NOT guarantee that this is a subscribed
     * user. Other tests must be used to protect paid content.
     * This may be a costly operation. You should use individual fields, like email or
     * nickname, if they are sufficient.
     */
    private User _user;
    private String _userHash;
    private String _screenName;
    private Integer _memberId;
    private String _email;
    private String _nickname;
    private int _mslCount;
    private int _mssCount;
    /**
     * Current US state
     */
    private State _state;
    /**
     * A pathway of "1", "2" or "3", or null for no pathway.
     */
    private String _pathway;
    private String _remoteAddress;
    private String _abVersion;

    private ApplicationContext _applicationContext;
    private IPropertyDao _propertyDao;

    private SessionContextUtil _sessionContextUtil;
    private IUserDao _userDao;
    private boolean _readFromClient = false;
    private boolean _hasSearched = false;
    private boolean _crawler = false;

    /**
     * Created by Spring as needed.
     */
    public SessionContext() {
    }


    public ApplicationContext getApplicationContext() {
        return _applicationContext;
    }

    public boolean isUserValid() {
        User user = getUser();
        if (user != null && _userHash != null) {
            try {
                Object[] hashInput = new Object[]{User.SECRET_NUMBER, user.getId(), user.getEmail()};
                String realHash = DigestUtil.hashObjectArray(hashInput);
                return realHash.equals(_userHash);
            } catch (NoSuchAlgorithmException e) {
                // fall through to return false below
            }
        }
        return false;
    }

    public void setUserValid(boolean ignored) {
        // ignored
    }

    public User getUser() {
        if (_user == null) {
            if (_memberId != null) {
                try {
                    _user = _userDao.findUserFromId(_memberId.intValue());
                } catch (ObjectRetrievalFailureException e) {
                    _log.warn("Cookie pointed to non-existent user with id " + _memberId);
                }
            }
        }
        return _user;
    }

    public void setUser(final User user) {
        _user = user;
        if (user != null) {
            _email = user.getEmail();
            _memberId = user.getId();
        } else {
            _email = null;
            _memberId = null;
        }
    }

    public State getState() {
        return _state;
    }

    /**
     * Guaranteed non-null state.
     */
    public State getStateOrDefault() {
        return _state == null ? State.CA : _state;
    }

    public void setState(final State state) {
        _state = state;
    }

    /**
     * Set by SessionContextUtil.updateFromParams()
     */
    public String getCobrand() {
        return _cobrand;
    }

    public String getHostName() {
        return _hostName;
    }

    /**
     * Determine if this is our main website or a cobrand
     *
     * @return true if it's a cobrand
     */
    public boolean isCobranded() {
        return _cobrand != null;
    }

    /**
     * We only turn advertising off when our ad serving company has an outage
     *
     * @return true if the ad server is working
     */
    public boolean isAdvertisingOnline() {
        return "true".equals(_propertyDao.getProperty(IPropertyDao.ADVERTISING_ENABLED_KEY, "true"));
    }

    /**
     * Is this the yahoo cobrand?
     * yahoo cobrands are yahooed and yahoo
     */
    public boolean isYahooCobrand() {
        boolean sYahooCobrand = false;
        if (_cobrand != null &&
                (_cobrand.matches("yahoo|yahooed"))) {
            sYahooCobrand = true;
        }
        return sYahooCobrand;
    }

    /**
     * Is this disney's family cobrand?
     * family.greatschools.net
     */
    public boolean isFamilyCobrand() {
        return isCobranded() &&
                "family".equals(getCobrand());
    }

    /**
     * Determine if this site is a framed site, in other words, no ads and no nav
     *
     * @return true if it's framed
     */
    public boolean isFramed() {
        return _cobrand != null &&
                _cobrand.matches("mcguire|framed|number1expert|vreo|e-agent|homegain|envirian|connectingneighbors");
    }

    public void setHostName(final String hostName) {
        _hostName = hostName;
    }

    public void setCobrand(final String cobrand) {
        _cobrand = cobrand;
    }

    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        _applicationContext = applicationContext;
    }


    public String getPathway() {
        return _pathway;
    }

    public void setPathway(String pathway) {
        _pathway = pathway;
    }

    /**
     * Currently this is only used for determining A/B for multivariate testing,
     * therefore there is no accessor method.
     *
     * @param address ip address for A/B testing
     */
    public void setRemoteAddress(String address) {
        if (address != null) {
            _remoteAddress = address.trim();
        }
    }

    public void setAbVersion(String s) {
        _abVersion = s;
    }

    /**
     * Returns either (lowercase) "a" or "b" based on the odd/even-ness of
     * the remote host's ip address.  "a" is for even, "b" is for odd.
     *
     * @return a <code>String</code>type
     */
    public String getABVersion() {
        if (_abVersion != null) {
            return _abVersion;
        } else {
            String version = "a";
            if (_remoteAddress != null) {
                String digit =
                        _remoteAddress.substring(_remoteAddress.length() - 1,
                                _remoteAddress.length());
                try {
                    int i = Integer.parseInt(digit);
                    if (i % 2 != 0) {
                        version = "b";
                    }
                } catch (NumberFormatException nfe) {
                    // ignore
                }
            }
            return version;
        }
        //return getMultivariateVersion(2);
    }

    public IPropertyDao getPropertyDao() {
        return _propertyDao;
    }

    public void setPropertyDao(IPropertyDao propertyDao) {
        _propertyDao = propertyDao;
    }

    public SessionContextUtil getSessionContextUtil() {
        return _sessionContextUtil;
    }

    public void setSessionContextUtil(SessionContextUtil sessionContextUtil) {
        _sessionContextUtil = sessionContextUtil;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public String getNickname() {
        return _nickname;
    }

    public void setNickname(String nickname) {
        _nickname = nickname;
    }

    public int getMslCount() {
        return _mslCount;
    }

    public void setMslCount(int mslCount) {
        _mslCount = mslCount;
    }

    public int getMssCount() {
        return _mssCount;
    }

    public String getUserHash() {
        return _userHash;
    }

    public void setUserHash(String userHash) {
        _userHash = userHash;
    }

    public String getScreenName() {
        return _screenName;
    }

    public void setScreenName(String screenName) {
        _screenName = screenName;
    }

    public void setMssCount(int mssCount) {
        _mssCount = mssCount;
    }

    public Integer getMemberId() {
        return _memberId;
    }

    public void setMemberId(Integer memberId) {
        _memberId = memberId;
    }

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        _email = email;
    }

    public String getEmailUrlEncoded() {
        try {
            return URLEncoder.encode(_email, "UTF-8");
        } catch (Exception e) {
            return _email;
        }
    }

    public void setReadClientSideSessionCache(boolean readIt) {
        _readFromClient = readIt;
    }

    public boolean isReadFromClient() {
        return _readFromClient;
    }

    public boolean getHasSearched() {
        return _hasSearched;
    }

    public void setHasSearched(boolean hasSearched) {
        _hasSearched = hasSearched;
    }

    /**
     * Determine if the user is a crawler
     *
     * @return true if it's a cobrand
     */
    public boolean isCrawler() {
        return _crawler;
    }

    public void setCrawler(boolean crawler) {
        _crawler = crawler;
    }
}
