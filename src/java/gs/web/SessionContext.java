/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SessionContext.java,v 1.6 2005/09/15 19:16:01 thuss Exp $
 */
package gs.web;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.content.IArticleDao;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.school.ISchoolDao;
import gs.data.school.census.ICensusValueDao;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.beans.BeansException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * The purpose is ...
 *
 * @author Andrew J. Peterson <mailto:apeterson@greatschools.net>
 */
public class SessionContext implements ApplicationContextAware {

    public static final String BEAN_ID = "sessionContext";

    public static final String SESSION_ATTRIBUTE_NAME = "context";

    // user can change state by passing a parameter on the command line
    private static final String STATE_PARAM = "state";

    // user can change hosts by passing a parameter on the command line
    private static final String HOST_PARAM = "host";

    // user can change the cobrand by passing a parameter on the command line
    private static final String COBRAND_PARAM = "cobrand";

    private IUserDao _userDao;
    private StateManager _stateManager;
    private IArticleDao _articleDao;
    private ISchoolDao _schoolDao;
    private ICensusValueDao _censusValueDao;

    private static final Log _log = LogFactory.getLog(SessionContextInterceptor.class);

    private String _hostName;
    private String _cobrand;
    private User _user;
    private State _state;

    private ApplicationContext _applicationContext;

    /**
     * Accessor
     */
    public static SessionContext getInstance(HttpSession session) {
        SessionContext sessionContext =
                (SessionContext) session.getAttribute(SESSION_ATTRIBUTE_NAME);
        return sessionContext;
    }

    public static SessionContext getInstance(HttpServletRequest request) {
        HttpSession session = request.getSession();
        SessionContext sessionContext =
                (SessionContext) session.getAttribute(SESSION_ATTRIBUTE_NAME);
        return sessionContext;
    }


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
                User user = _userDao.getUserFromId(cookieId);
                _user = user;
            }
        }

        updateFromParams(httpServletRequest);
    }


    public void updateFromParams(HttpServletRequest httpServletRequest) {
        String paramHost = httpServletRequest.getParameter(HOST_PARAM);
        if (!StringUtils.isEmpty(paramHost)) {
            _hostName = paramHost;
        } else {
            _hostName = httpServletRequest.getServerName();
        }

        String paramCobrand = httpServletRequest.getParameter(COBRAND_PARAM);
        if (!StringUtils.isEmpty(paramCobrand)) {
            _cobrand = paramCobrand;
        }

        // Set state, or change, if necessary
        String paramStateStr = httpServletRequest.getParameter(STATE_PARAM);
        if (!StringUtils.isEmpty(paramStateStr)) {
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

    public String getHostName() {
        String host = _hostName;
        // If it's a developers workstation, else it's a dev,staging, or live
        if (StringUtils.contains(_hostName, "localhost")) {
            String dev = "dev.greatschools.net";
            host = (_cobrand == null) ? dev : _cobrand + "." + dev;
        } else if (_cobrand != null) {
            // sfgate.dev.greatschools.net
            host = _cobrand + "." + host;
            // azcentral.www.greatschools.net -> azcentral.greatschools.net
            host.replaceFirst(".www.", ".");
        }
        return host;
    }

    /**
     * Determine if this is our main website or a cobrand
     *
     * @return true if it's a cobrand
     */
    public boolean isCobrand() {
        boolean cobrand = true;
        if (_cobrand == null &&
                (getHostName().startsWith("www") ||
                        getHostName().startsWith("staging") ||
                        getHostName().startsWith("dev"))) {
            cobrand = false;
        }
        return cobrand;
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
