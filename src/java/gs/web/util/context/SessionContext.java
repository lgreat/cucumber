/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SessionContext.java,v 1.3 2006/09/12 21:45:52 apeterson Exp $
 */
package gs.web.util.context;

import gs.data.admin.IPropertyDao;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.state.State;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.ObjectRetrievalFailureException;

import java.io.Serializable;

/**
 * Implementation of the ISessionContext interface based on Java servlet
 * sessions.
 *
 * @author Andrew J. Peterson <mailto:apeterson@greatschools.net>
 * @see SessionContextInterceptor
 */
public class SessionContext implements ISessionContext, ApplicationContextAware, Serializable {

    public static final String BEAN_ID = "sessionContext";

    private static final long serialVersionUID = -314159265358979323L;

    private static final Log _log = LogFactory.getLog(SessionContextInterceptor.class);

    /**
     * The name of the cobrand (sfgate, azcentral, dps, etc...) or null
     */
    private String _cobrand;
    private String _hostName;
    private User _user;
    private Integer _memberId;
    private String _email;
    private String _nickname;
    private int _mslCount;
    private int _mssCount;
    private State _state;
    private String _pathway;
    private String _remoteAddress;
    private String _abVersion;

    private ApplicationContext _applicationContext;
    private IPropertyDao _propertyDao;

    private SessionContextUtil _sessionContextUtil;
    private IUserDao _userDao;
    private boolean _readFromClient = false;
    private boolean _hasSearched = false;

    /**
     * Created by Spring as needed.
     */
    public SessionContext() {
    }


    public ApplicationContext getApplicationContext() {
        return _applicationContext;
    }

    public User getUser() {
        if (_user == null) {
            if (_memberId != null) {
                try {
                    _user = _userDao.findUserFromId(_memberId.intValue());
                }   catch (ObjectRetrievalFailureException e) {
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

    public State getStateOrDefault() {
        return _state == null ? State.CA : _state;
    }

    public void setState(final State state) {
        _state = state;
    }

    public String getCobrand() {
        return _cobrand;
    }

    public String getHostName() {
        return _hostName;
    }

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

    public boolean isYahooCobrand() {
        boolean sYahooCobrand = false;
        if (_cobrand != null &&
                (_cobrand.matches("yahoo|yahooed"))) {
            sYahooCobrand = true;
        }
        return sYahooCobrand;
    }

    public boolean isFamilyCobrand() {
        return isCobranded() &&
                "family".equals(getCobrand());
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
}
