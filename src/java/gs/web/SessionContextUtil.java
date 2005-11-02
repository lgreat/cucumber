/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SessionContextUtil.java,v 1.6 2005/11/02 21:00:47 apeterson Exp $
 */

package gs.web;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.util.UrlUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.ObjectRetrievalFailureException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    // user can change hosts by passing a parameter on the command line
    public static final String MEMBER_PARAM = "member";

    public static final String PATHWAY_PARAM = "path";

    private static final String MEMBER_ID_COOKIE = "MEMBER";
    private static final String PATHWAY_COOKIE = "PATHWAY";


    private static final Log _log = LogFactory.getLog(SessionContextUtil.class);
    private ApplicationContext _applicationContext;

    private IUserDao _userDao;
    private StateManager _stateManager;
    private UrlUtil _urlUtil = new UrlUtil();


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
                if (MEMBER_ID_COOKIE.equals(thisCookie.getName())) {
                    String id = thisCookie.getValue();
                    cookieId = new Integer(id);

                    // No previous login information or different user.
                    if (context.getUser() == null || !context.getUser().getId().equals(cookieId)) {
                        context.setUser(_userDao.getUserFromId(cookieId.intValue()));
                    }
                } else if (PATHWAY_COOKIE.equals(thisCookie.getName())) {
                    String path = thisCookie.getValue();
                    if (!path.equals(context.getPathway())) {
                        context.setPathway(path); // TODO validation
                    }
                }
            }
        }

    }

    /**
     * HandlerMapping may put information into the request for us to pull out.
     */
    public void updateFromRequestAttributes(HttpServletRequest httpServletRequest,
                                            SessionContext context) {

        Object s = httpServletRequest.getAttribute("state");
        if (s != null && s instanceof State) {
            context.setState((State) s);
        }
    }

    /**
     * Called at the beginning of the request. Allows this class to
     * do common operations for all pages.
     */
    public void updateFromParams(HttpServletRequest request,
                                 HttpServletResponse response,
                                 SessionContext context) {

        // Get the real hostname or see if it's been overridden
        String paramHost = request.getParameter(HOST_PARAM);
        String hostName = StringUtils.isEmpty(paramHost) ?
                request.getServerName() :
                paramHost;

        // Determine if this is a cobrand
        String cobrand = context.getCobrand();
        String paramCobrand = request.getParameter(COBRAND_PARAM);
        if (StringUtils.isNotEmpty(paramCobrand)) {
            cobrand = paramCobrand;
        } else {
            cobrand = _urlUtil.cobrandFromUrl(hostName);
        }

        // Now see if we need to override the hostName
        hostName = _urlUtil.buildPerlHostName(hostName, cobrand);

        updateStateFromParam(context, request);

        // Set state, or change, if necessary
        String paramPathwayStr = request.getParameter(PATHWAY_PARAM);
        if (StringUtils.isNotEmpty(paramPathwayStr)) {
            setPathway(context, response, paramPathwayStr);
        }

        // TODO make sure nobody can change IDs surreptitiously.
        String paramMember = request.getParameter(MEMBER_PARAM);
        if (StringUtils.isNotEmpty(paramMember)) {
            final int id;
            try {
                id = Integer.parseInt(paramMember);
                try {
                    User user = null;
                    user = _userDao.getUserFromId(id);
                    context.setUser(user);
                } catch (ObjectRetrievalFailureException e) {
                    _log.warn("HACKER? Bad member id passed as parameter (ignoring): '" +
                            paramMember + "' from IP " + request.getRemoteAddr() +
                            " named " + request.getRemoteHost()); // don't pass exception-- it's distracting
                }
            } catch (NumberFormatException e) {
                _log.warn("HACKER? Attempt to pass ill-formed member id as parameter: '" +
                        paramMember + "' from IP " + request.getRemoteAddr() +
                        " named " + request.getRemoteHost());// don't pass exception-- it's distracting
            }
        }

        context.setHostName(hostName);
        context.setCobrand(cobrand);
    }

    /**
     * Sets the pathway in both the context and the  pseudo-"session" (the cookie).
     */
    public void setPathway(SessionContext context, HttpServletResponse response, String paramPathwayStr) {
        final String currPathway = context.getPathway();
        if (currPathway == null ||
                !currPathway.equals(paramPathwayStr)) {
            Cookie c = new Cookie(PATHWAY_COOKIE, paramPathwayStr);
            c.setPath("/");
            response.addCookie(c);

            context.setPathway(paramPathwayStr);
        }
    }

    /**
     * Sets the pathway in both the context and the  pseudo-"session" (the cookie).
     */
    public void setPathway(HttpServletRequest request, HttpServletResponse response, String newPathway) {
        SessionContext context = (SessionContext) SessionContext.getInstance(request);
        setPathway(context, response, newPathway);
    }

    public void updateStateFromParam(SessionContext context, HttpServletRequest httpServletRequest) {
        // Set state, or change, if necessary
        String paramStateStr = httpServletRequest.getParameter(STATE_PARAM);
        if (!StringUtils.isEmpty(paramStateStr)) {
            final State currState = context.getState();
            State state = currState;
            State s = _stateManager.getState(paramStateStr);
            if (currState == null) {
                state = s;
            } else if (!currState.equals(s)) {
                state = s;
                _log.debug("switching user's state: " + s);
            }
            context.setState(state);
        }
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

}
