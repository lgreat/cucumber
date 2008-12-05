/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SessionContext.java,v 1.29 2008/12/05 00:59:02 yfan Exp $
 */
package gs.web.util.context;

import gs.data.admin.IPropertyDao;
import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.state.State;
import gs.data.util.DigestUtil;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.ObjectRetrievalFailureException;

import java.io.IOException;
import java.io.Serializable;
import java.net.URLEncoder;

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
    private String _customCobrandFooter;
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
    private Integer _cityId; // greatschools city id from us_geo.city
    private City _city;

    /**
     * Current US state
     */
    private State _state;
    /**
     * A pathway of "1", "2" or "3", or null for no pathway.
     */
    private String _pathway;
    private String _abVersion = "a";

    private ApplicationContext _applicationContext;
    private IPropertyDao _propertyDao;

    private SessionContextUtil _sessionContextUtil;
    private IUserDao _userDao;
    private IGeoDao _geoDao;
    private boolean _readFromClient = false;
    private boolean _hasSearched = false;
    // Tells us whether we're running in the integration test environment
    private boolean _integrationTest = false;
    private boolean _crawler = false;

    private String _tempMsg;

    /**
     * This is true for pages that have primarily editorial (topic) content and shoul
     * display the topics search control by default.
     */
    private boolean _isTopicPage = false;
    private String _originalRequestURI;

    /**
     * Created by Spring as needed.
     */
    public SessionContext() {
    }


    public ApplicationContext getApplicationContext() {
        return _applicationContext;
    }

    public boolean isUserSeemsValid() {
        if (_memberId != null && _userHash != null && _userHash.length() > DigestUtil.MD5_HASH_LENGTH) {
            if (StringUtils.equals(_memberId.toString(), _userHash.substring(DigestUtil.MD5_HASH_LENGTH))) {
                return true;
            }
        }
        return false;
    }

    public User getUser() {
        if (_user == null) {
            if (_memberId != null) {
                try {
                    _user = _userDao.findUserFromId(_memberId);
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


    public void setCobrand(final String cobrand) {
        _cobrand = cobrand;
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

    public String getHostName() {
        return _hostName;
    }

    public boolean isCustomCobrandedFooter() {
        return isCobranded() && getCustomCobrandFooter().length() > 0;
    }

    public void setCustomCobrandFooter(String customCobrandFooter) {
        _customCobrandFooter = customCobrandFooter;
    }

    /**
     * @return an empty string if no cobrand footer, otherwise the HTML for the footer
     */
    public String getCustomCobrandFooter() {
        if (_customCobrandFooter == null) {
            if (isCobranded())
                try {
                    _customCobrandFooter = fetchCustomCobrandFooter();
                } catch (Exception e) {
                    _customCobrandFooter = "";
                }
            else _customCobrandFooter = "";
        }
        return _customCobrandFooter;
    }

    protected String fetchCustomCobrandFooter() throws IOException {
        String url = "http://" + getHostName() + "/templates/local/nav/bottomnav.insrt";
        GetMethod get = new GetMethod(url);
        try {
            new HttpClient().executeMethod(get);
            return StringUtils.trimToEmpty(get.getResponseBodyAsString());
        } finally {
            get.releaseConnection();
        }
    }

    /**
     * We only turn advertising off when our ad serving company has an outage
     *
     * @return true if the ad server is working
     */
    public boolean isAdvertisingOnline() {
        return "true".equals(_propertyDao.getProperty(IPropertyDao.ADVERTISING_ENABLED_KEY, "true"));
    }

    public boolean isInterstitialEnabled() {
        return !isCobranded() &&
                !isCrawler() &&
                "true".equals(_propertyDao.getProperty(IPropertyDao.INTERSTITIAL_ENABLED_KEY, "false"));
    }

    /**
     * Determine if this site is a framed site, in other words, no ads and no nav
     *
     * @return true if it's framed
     */
    public boolean isFramed() {
        return _cobrand != null &&
                _cobrand.matches("mcguire|framed|number1expert|vreo|e-agent|homegain|envirian|connectingneighbors|test|ocregister");
    }

    public void setHostName(final String hostName) {
        _hostName = hostName;
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

    public void setAbVersion(String s) {
        _abVersion = s;
    }

    /**
     * Returns either (lowercase) "a" or "b", initialized in ResponseInterceptor
     *
     * @return a <code>String</code>type
     */
    public String getABVersion() {
        return _abVersion;
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

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
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

    public boolean isIntegrationTest() {
        return _integrationTest;
    }

    public void setIntegrationTest(boolean integrationTest) {
        _integrationTest = integrationTest;
    }

    public boolean isTopicPage() {
        return _isTopicPage;
    }

    public void setIsTopicPage(boolean topicPage) {
        _isTopicPage = topicPage;
    }

    //A temporary message
    public String getTempMsg() {
        return _tempMsg;
    }

    public void setTempMsg(String tempMsg) {
        _tempMsg = tempMsg;
    }

    public String getOriginalRequestURI() {
        return _originalRequestURI;
    }

    public void setOriginalRequestURI(String requestURI) {
        _originalRequestURI = requestURI;
    }

    public Integer getCityId() {
        return _cityId;
    }

    /**
     * Try to mimic logic behind getUser
     * @return City object defined by cityID, or null
     */
    public City getCity() {
        if (_city == null) {
            if (_cityId != null) {
                try {
                    _city = _geoDao.findCityById(_cityId);
                } catch (ObjectRetrievalFailureException e) {
                    _log.warn("Cookie pointed to non-existent city with id " + _cityId);
                }
            }
        }
        return _city;
    }

    public void setCity(City city) {
        _city = city;
        if (city != null) {
            _cityId = city.getId();
        } else {
            _cityId = null;
        }
    }

    public void setCityId(Integer cityId) {
        this._cityId = cityId;
    }
}
