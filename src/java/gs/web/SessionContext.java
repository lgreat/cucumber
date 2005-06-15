/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SessionContext.java,v 1.2 2005/06/15 17:16:24 apeterson Exp $
 */
package gs.web;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.content.IArticleDao;
import gs.data.state.State;
import gs.data.state.StateManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * The purpose is ...
 *
 * @author Andrew J. Peterson <mailto:apeterson@greatschools.net>
 */
public class SessionContext {

    public static final String BEAN_ID = "sessionContext";

    public static final String SESSION_ATTRIBUTE_NAME = "context";

    // user can change state by passing a parameter on the command line
    private static final String STATE_PARAM = "state";

    // user can change hosts by passing a parameter on the command line
    private static final String HOST_PARAM = "host";

    private IUserDao _userDao;
    private StateManager _stateManager;
    private IArticleDao _articleDao;

    private static final Log _log = LogFactory.getLog(SessionContextInterceptor.class);

    private String _hostName;
    private User _user;
    private State _state;

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
        if (_user == null) {
            // we probably don't need to do this every time, but for now
            // while we are jumping back and forth with the perl code, we do.
            Cookie[] cookies = httpServletRequest.getCookies();
            if (cookies != null) {
                for (int i = 0; i < cookies.length; i++) {
                    Cookie thisCookie = cookies[i];
                    if ("MEMBER".equals(thisCookie.getName())) {
                        String id = thisCookie.getValue();
                        User user = _userDao.getUserFromId(new Long(id));
                        _user = user;
                    }
                }
            }
        }

        updateFromParams(httpServletRequest);
    }


    public void updateFromParams(HttpServletRequest httpServletRequest) {
        String paramHost = httpServletRequest.getParameter(HOST_PARAM);
        if (!StringUtils.isEmpty(paramHost)) {
            _hostName = paramHost;
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
        return _hostName == null ? "www.greatschools.net" : _hostName;
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
}
