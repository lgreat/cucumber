/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SessionContextUtil.java,v 1.1 2005/10/24 21:53:04 apeterson Exp $
 */

package gs.web;

import gs.data.community.IUserDao;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.util.NetworkUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class SessionContextUtil {

    public static final String BEAN_ID = "sessionContextUtil";


    // user can change state by passing a parameter on the command line
    public static final String STATE_PARAM = "state";

    // user can change the cobrand by passing a parameter on the command line
    public static final String COBRAND_PARAM = "cobrand";

    // user can change hosts by passing a parameter on the command line
    public static final String HOST_PARAM = "host";


    private static final Log _log = LogFactory.getLog(SessionContextUtil.class);
    private ApplicationContext _applicationContext;

    private IUserDao _userDao;
    private StateManager _stateManager;
    private NetworkUtil _networkUtil = new NetworkUtil();


    public void readCookies(HttpServletRequest httpServletRequest,
                            final SessionContext context) {
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
            if (context.getUser() == null || !context.getUser().getId().equals(cookieId)) {
                context.setUser(_userDao.getUserFromId(cookieId.intValue()));
            }
        }

    }

    /**
     * Called at the beginning of the request. Allows this class to
     * do common operations for all pages.
     */
    public void updateFromParams(HttpServletRequest httpServletRequest,
                                 SessionContext context) {
        String cobrand = context.getCobrand();
        State state = context.getState();

        // Get the real hostname or see if it's been overridden
        String paramHost = httpServletRequest.getParameter(HOST_PARAM);
        String hostName = StringUtils.isEmpty(paramHost) ?
                httpServletRequest.getServerName() :
                paramHost;

        // Determine if this is a cobrand
        String paramCobrand = httpServletRequest.getParameter(COBRAND_PARAM);
        if (StringUtils.isNotEmpty(paramCobrand)) {
            cobrand = paramCobrand;
        } else {
            cobrand = _networkUtil.cobrandFromUrl(hostName);
        }

        // Now see if we need to override the hostName
        hostName = _networkUtil.buildPerlHostName(hostName, cobrand);

        // Set state, or change, if necessary
        String paramStateStr = httpServletRequest.getParameter(STATE_PARAM);
        if (!StringUtils.isEmpty(paramStateStr)) {
            State s = _stateManager.getState(paramStateStr);
            final State currState = state;
            if (currState == null) {
                state = s;
            } else if (!currState.equals(s)) {
                state = s;
                _log.debug("switching user's state: " + s);
            }
        }

        context.setHostName(hostName);
        context.setCobrand(cobrand);
        context.setState(state);
    }


    public ApplicationContext getApplicationContext() {
        return _applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        _applicationContext = applicationContext;
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

    public NetworkUtil getNetworkUtil() {
        return _networkUtil;
    }

    public void setNetworkUtil(NetworkUtil networkUtil) {
        _networkUtil = networkUtil;
    }

}
