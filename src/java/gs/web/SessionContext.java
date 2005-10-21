/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SessionContext.java,v 1.18 2005/10/21 00:16:01 apeterson Exp $
 */
package gs.web;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.content.IArticleDao;
import gs.data.school.ISchoolDao;
import gs.data.school.census.ICensusValueDao;
import gs.data.state.State;
import gs.data.state.StateManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * Implementation of the ISessionFacade interface based on Java servlet
 * sessions.
 *
 * @author Andrew J. Peterson <mailto:apeterson@greatschools.net>
 * @see SessionContextInterceptor
 */
public class SessionContext
        extends SessionFacade
        implements ApplicationContextAware, ISessionChanger {

    static final String BEAN_ID = "sessionContext";

    private IUserDao _userDao;
    private StateManager _stateManager;
    private IArticleDao _articleDao;
    private ISchoolDao _schoolDao;
    private ICensusValueDao _censusValueDao;

    private static final Log _log = LogFactory.getLog(SessionContextInterceptor.class);

    /**
     * The name of the cobrand (sfgate, azcentral, dps, etc...) or null
     */
    private String _cobrand;
    private String _hostName;
    private User _user;
    private State _state;

    private ApplicationContext _applicationContext;

    /**
     * Created by Spring as needed.
     */
    public SessionContext() {
    }


    /**
     * Set the current request. Will be called repeatedly, giving a chance
     * for "standard" request processing to be done.
     */
    public void setRequest(HttpServletRequest httpServletRequest) {

        // Find the cookie that pertains to the user
        // We don't need to do this every time, but for now
        // while we are jumping back and forth with the perl code, we do.
        // Doing it every time is really special-case code for Priya's QA scenarios;
        // sign in, visit java page, sign out (perl), and then return to java
        // page. It  used to think you were still signed in.
        // TODO make sure nobody can change IDs surreptitiously.
        Integer cookieId = null;
        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                Cookie thisCookie = cookies[i];
                if ("MEMBER".equals(thisCookie.getName())) {
                    String id = thisCookie.getValue();
                    cookieId = new Integer(id);
                }
            }
        }

        if (cookieId != null) {
            // No previous login information or different user.
            if (_user == null || !_user.getId().equals(cookieId)) {
                _user = _userDao.getUserFromId(cookieId.intValue());
            }
        }

        updateFromParams(httpServletRequest);
    }


    public void updateFromParams(HttpServletRequest httpServletRequest) {

        // Get the real hostname or see if it's been overridden
        String paramHost = httpServletRequest.getParameter(HOST_PARAM);
        if (!StringUtils.isEmpty(paramHost)) {
            _hostName = paramHost;
        } else {
            _hostName = httpServletRequest.getServerName();
        }

        // Determine if this is a cobrand
        String paramCobrand = httpServletRequest.getParameter(COBRAND_PARAM);
        if (!StringUtils.isEmpty(paramCobrand)) {
            _cobrand = paramCobrand;
        } else if (!(_hostName.startsWith("www") ||
                _hostName.startsWith("staging") ||
                _hostName.startsWith("dev") ||
                _hostName.startsWith("localhost") ||
                _hostName.indexOf(".") < 0)) {
            _cobrand = _hostName.substring(0, _hostName.indexOf("."));
        }

        // Now see if we need to override the _hostName
        if (StringUtils.contains(_hostName, "localhost")) {
            String dev = "dev.greatschools.net";
            _hostName = (_cobrand == null) ? dev : _cobrand + "." + dev;
            // Else if it's the main website but with the cobrand parameter passed
            // then we return the full cobrand URL
        } else if (_cobrand != null &&
                (_hostName.startsWith("www") ||
                _hostName.startsWith("staging") ||
                _hostName.startsWith("dev"))) {
            // dev.greatschools.net?cobrand=sfgate -> sfgate.dev.greatschools.net
            _hostName = _cobrand + "." + _hostName;
            // azcentral.www.greatschools.net -> azcentral.greatschools.net
            _hostName = _hostName.replaceFirst(".www.", ".");
        }

        // Set state, or change, if necessary
        String paramStateStr = httpServletRequest.getParameter(STATE_PARAM);
        if (!StringUtils.isEmpty(paramStateStr)) {
            _stateManager = new StateManager();
            State s = _stateManager.getState(paramStateStr);
            final State currState = _state;
            if (currState == null) {
                _state = s;
            } else if (!currState.equals(s)) {
                _state = s;
                _log.debug("switching user's state: " + s);
            }
        }
    }


    public ApplicationContext getApplicationContext() {
        return _applicationContext;
    }

    public User getUser() {
        return _user;
    }

    public void setUser(User user) {
        _user = user;
    }

    public State getState() {
        return _state;
    }

    public State getStateOrDefault() {
        return _state == null ? State.CA : _state;
    }

    public void setState(State state) {
        _state = state;
    }

    public String getCobrand() {
        return _cobrand;
    }

    public String getHostName() {
        return _hostName;
    }

    public boolean isCobrand() {
        return _cobrand != null;
    }

    public boolean isAdFree() {
        boolean sAdFree = false;
        if (_cobrand != null &&
                (_cobrand.matches("mcguire|framed|number1expert"))) {
            sAdFree = true;
        }
        return sAdFree;
    }

    public boolean isYahooCobrand() {
        boolean sYahooCobrand = false;
        if (_cobrand != null &&
                (_cobrand.matches("yahoo|yahooed"))) {
            sYahooCobrand = true;
        }
        return sYahooCobrand;
    }

    public String getSecureHostName() {
        String sHost = "secure.greatschools.net";

        if (StringUtils.contains(_hostName, "dev.greatschools.net")) {
            sHost = "secure.dev.greatschools.net";
        } else if (StringUtils.equalsIgnoreCase(_hostName, "staging.greatschools.net")) {
            sHost = "secure.staging.greatschools.net";
        }

        return sHost;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }

    public IArticleDao getArticleDao() {
        return _articleDao;
    }

    public void setArticleDao(IArticleDao articleDao) {
        _articleDao = articleDao;
    }


    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public ICensusValueDao getCensusValueDao() {
        return _censusValueDao;
    }

    public void setCensusValueDao(ICensusValueDao censusValueDao) {
        _censusValueDao = censusValueDao;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        _applicationContext = applicationContext;
    }
}
