/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: SessionContextUtil.java,v 1.24 2007/09/04 17:07:46 dlee Exp $
 */

package gs.web.util.context;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.community.ClientSideSessionCache;
import gs.web.community.registration.AuthenticationManager;
import gs.web.util.PageHelper;
import gs.web.util.UrlUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class SessionContextUtil implements ApplicationContextAware {

    public static final String BEAN_ID = "sessionContextUtil";

    // in seconds: 60 (in a minute) * 60 (in an hour) * 24 (in a day) * 365 (in a year) * 2 = two years
    public static final int COMMUNITY_COOKIE_MAX_AGE = 60 * 60 * 24 * 365 * 2;

    // user can change state by passing a parameter on the command line
    public static final String STATE_PARAM = "state";

    // user can change the cobrand by passing a parameter on the command line
    public static final String COBRAND_PARAM = "cobrand";

    // user can change hosts by passing a parameter on the command line
    public static final String HOST_PARAM = "host";

    // user can change hosts by passing a parameter on the command line
    public static final String MEMBER_PARAM = "member";

    public static final String PATHWAY_PARAM = "path";

    // used for A/B or multivariate testing
    public static final String VERSION_PARAM = "version";

    /**
     * Insider log-in cookie, from Java site. Domain is ".greatschools.net".
     */
    private static final String MEMBER_ID_INSIDER_COOKIE = "MEMBER";

    /**
     * My School List cookie, for backward compatibility. Domain used to be ".greatschools.net",
     * but now it the default (www.greatschools.net or cobrand domain).
     */
    public static final String MEMBER_ID_COOKIE = "MEMID";

    /**
     * TRNO cookie we set to uniquely identify a visitor
     */
    public static final String TRNO_COOKIE = "TRNO";

    /**
     * Cookie we set to track what cobrand a user is on
     */
    public static final String COBRAND_COOKIE = "COBRAND";

    /* Browsing state session cookies */
    // STATE, PATHWAY

    /* User information cached cookies */


    private static final Log _log = LogFactory.getLog(SessionContextUtil.class);
    private ApplicationContext _applicationContext;

    private IUserDao _userDao;
    private StateManager _stateManager;
    private UrlUtil _urlUtil = new UrlUtil();

    private CookieGenerator _stateCookieGenerator;
    private CookieGenerator _memberIdCookieGenerator;
    private CookieGenerator _hasSearchedCookieGenerator;
    private CookieGenerator _sessionCacheCookieGenerator;
    private CookieGenerator _communityCookieGenerator;
    private CookieGenerator _tempMsgCookieGenerator;
    public static final String COMMUNITY_LIVE_HOSTNAME = "community.greatschools.net";
    public static final String COMMUNITY_STAGING_HOSTNAME = "community.staging.greatschools.net";
    public static final String COMMUNITY_DEV_HOSTNAME = "community.dev.greatschools.net";


    public SessionContextUtil() {
    }

    protected void readCookies(HttpServletRequest httpServletRequest,
                             final SessionContext context) {
        // Find the cookie that pertains to the user
        // We don't need to do this every time, but for now
        // while we are jumping back and forth with the perl code, we do.
        // Doing it every time is really special-case code for Priya's QA scenarios;
        // sign in, visit java page, sign out (perl), and then return to java
        // page. It  used to think you were still signed in.
        // TODO make sure nobody can change IDs surreptitiously.
        int insiderId = -1;
        int mslId = -1;

        Cookie[] cookies = httpServletRequest.getCookies();
        ClientSideSessionCache cache = null;
        boolean isCommunity = false;
        if (cookies != null) {

            State cookiedState = null;
            State oldCookiedState = null;
            // Collect all the cookies
            for (Cookie thisCookie : cookies) {
                if (MEMBER_ID_INSIDER_COOKIE.equals(thisCookie.getName())) {
                    String id = thisCookie.getValue();
                    try {
                        insiderId = Integer.parseInt(id);
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                } else if (MEMBER_ID_COOKIE.equals(thisCookie.getName())) {
                    String id = thisCookie.getValue();
                    try {
                        mslId = Integer.parseInt(id);
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                } else if (StringUtils.equals(_hasSearchedCookieGenerator.getCookieName(), thisCookie.getName())) {
                    if (StringUtils.isNotBlank(thisCookie.getValue())) {
                        context.setHasSearched(true); // default is false
                    }
                } else if (StringUtils.equals(_stateCookieGenerator.getCookieName(), thisCookie.getName())) {
                    String state = thisCookie.getValue();
                    State s = _stateManager.getState(state);
                    if (s != null) {
                        cookiedState = s;
                        context.setState(s);
                    }
                } else if (StringUtils.equals(_sessionCacheCookieGenerator.getCookieName(), thisCookie.getName())) {
                    cache = ClientSideSessionCache.createClientSideSessionCache(thisCookie.getValue());
                } else if (StringUtils.equals("STATE", thisCookie.getName())) {
                    // Check for this value in case new state cookie isn't present.
                    // This allows old users to retain their state cookie.
                    // The value will be transferred from the SessionContext into a STATE2 cookie in
                    // the response.
                    String state = thisCookie.getValue();
                    State s = _stateManager.getState(state);
                    if (s != null) {
                        oldCookiedState = s;
                    }
                } else if (StringUtils.equals(_communityCookieGenerator.getCookieName(), thisCookie.getName())) {
                    // GS-3819
                    isCommunity = true;
                    // pull member id out of community cookie if necessary
                    if (insiderId == -1) {
                        try {
                            insiderId = AuthenticationManager.getUserIdFromCookieValue(thisCookie.getValue());
                        } catch (Exception e) {
                            _log.warn("Unable to parse member id out of community cookie with value: " + 
                                    thisCookie.getValue());
                        }
                    }
                } else if (StringUtils.equals(_tempMsgCookieGenerator.getCookieName(), thisCookie.getName())) {
                    String message = thisCookie.getValue();
                    context.setTempMsg(message);
                }
                // If new state cookie is not set, check for old state cookie and use that value if present
                if (cookiedState == null && oldCookiedState != null) {
                    context.setState(oldCookiedState);
                }
            }

            /*
                Process the membership related cookies.
                Only change user if there is no previous login information or
                different user. A member login overrides MSL cookie.
            */
            if (insiderId != -1) {
                context.setMemberId(insiderId);
            } else if (mslId != -1) {
                context.setMemberId(mslId);
            }

            if (isCommunity &&
                    ((cache == null) || StringUtils.isEmpty(cache.getScreenName()))) {
                // User has community cookie but either no cache or incomplete cache
                // This can only happen if the user signed in from the community site. The cache is
                // now out of sync and needs to be recreated (GS-3819)
                cache = new ClientSideSessionCache(context.getUser());
                updateContextFromCache(context, cache);
            } else if (context.getMemberId() != null &&
                    // Bring in the client cache only if it matches the member cookie set above--
                    // The perl side may have logged the user in as someone else
                    cache != null &&
                    cache.getMemberId() != null &&
                    cache.getMemberId().equals(context.getMemberId())) {
                updateContextFromCache(context, cache);
                context.setReadClientSideSessionCache(true);
            }
        }
    }

    private void updateContextFromCache(SessionContext context, ClientSideSessionCache cache) {
        context.setEmail(cache.getEmail());
        context.setMslCount(cache.getMslCount());
        context.setMssCount(cache.getMssCount());
        context.setNickname(cache.getNickname());
        context.setUserHash(cache.getUserHash());
        context.setScreenName(cache.getScreenName());
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
        context.setRemoteAddress(httpServletRequest.getRemoteAddr());

        String uri = httpServletRequest.getRequestURI();
        if (uri != null && uri.contains("/content/")) {
            context.setIsTopicPage(true);
        } else {
            String queryString = httpServletRequest.getQueryString();
            if (queryString != null && queryString.contains("c=topic")) {
                context.setIsTopicPage(true);
            }
        }
    }

    /**
     * Called at the beginning of the request; called after #readCookies is called.
     * Allows this class to do common operations for all pages.
     */
    public void updateFromParams(HttpServletRequest request,
                                 HttpServletResponse response,
                                 SessionContext context) {

        // Get the real hostname or see if it's been overridden
        String paramHost = request.getParameter(HOST_PARAM);
        String hostName = StringUtils.isEmpty(paramHost) ? request.getServerName() : paramHost;

        // Determine if this is a cobrand
        String cobrand = null;
        String paramCobrand = request.getParameter(COBRAND_PARAM);
        if (StringUtils.isNotEmpty(paramCobrand)) {
            if (!paramCobrand.equalsIgnoreCase("www")) {
                cobrand = paramCobrand;
            }
        } else {
            cobrand = _urlUtil.cobrandFromUrl(hostName);
        }

        // Determine if this is a crawler
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && userAgent.toLowerCase().matches(".*(googlebot|mediapartners-google|slurp|mmcrawler|msnbot|teoma|ia_archiver).*")) {
            context.setCrawler(true);
        } else {
            context.setCrawler(false);
        }

        // Now see if we need to override the hostName
        hostName = _urlUtil.buildPerlHostName(hostName, cobrand);

        updateStateFromParam(context, request, response);

        // Set state, or change, if necessary
        String paramPathwayStr = request.getParameter(PATHWAY_PARAM);
        if (StringUtils.isNotEmpty(paramPathwayStr)) {
            changePathway(context, response, paramPathwayStr);
        }

        // TODO make sure nobody can change IDs surreptitiously.
        String paramMember = request.getParameter(MEMBER_PARAM);
        if (StringUtils.isNotEmpty(paramMember)) {
            final int id;
            try {
                id = Integer.parseInt(paramMember);
                try {
                    User user = _userDao.findUserFromId(id);
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
    public void changePathway(SessionContext context, HttpServletResponse response, String paramPathwayStr) {
        final String currPathway = context.getPathway();
        if (currPathway == null ||
                !currPathway.equals(paramPathwayStr)) {
            context.setPathway(paramPathwayStr);
        }
    }

    /**
     * Sets the pathway in both the context and the  pseudo-"session" (the cookie).
     */
    public void setPathway(HttpServletRequest request, HttpServletResponse response, String newPathway) {
        SessionContext context = getSessionContext(request);
        changePathway(context, response, newPathway);
    }

    /**
     * Sets a marker to indicate that the user has executed a search.
     */
    public void setHasSearched(HttpServletResponse response) {
        _hasSearchedCookieGenerator.addCookie(response, "true");
    }

    public void setStateCookieGenerator(CookieGenerator stateCookieGenerator) {
        _stateCookieGenerator = stateCookieGenerator;
    }

    public void setMemberIdCookieGenerator(CookieGenerator memberIdCookieGenerator) {
        _memberIdCookieGenerator = memberIdCookieGenerator;
    }

    public void setHasSearchedCookieGenerator(CookieGenerator hasSearchedCookieGenerator) {
        _hasSearchedCookieGenerator = hasSearchedCookieGenerator;
    }

    public void setSessionCacheCookieGenerator(CookieGenerator sessionCacheCookieGenerator) {
        _sessionCacheCookieGenerator = sessionCacheCookieGenerator;
    }

    public CookieGenerator getCommunityCookieGenerator() {
        return _communityCookieGenerator;
    }

    public void setCommunityCookieGenerator(CookieGenerator communityCookieGenerator) {
        _communityCookieGenerator = communityCookieGenerator;
    }

    public CookieGenerator getTempMsgCookieGenerator() {
        return _tempMsgCookieGenerator;
    }

    public void setTempMsgCookieGenerator(CookieGenerator tempMsgCookieGenerator) {
        _tempMsgCookieGenerator = tempMsgCookieGenerator;
    }

    /**
     * Attempt to interpret a state= parameter and save it in the given context and to a cookie.
     * If it can't recognize the state, it just uses what was there before, if anything.
     * #readCookies() already called at this point so state variable in sessionContext
     * already populated with user's last known state value if available
     */
    public void updateStateFromParam(SessionContext context,
                                     HttpServletRequest httpServletRequest,
                                     HttpServletResponse httpServletResponse) {
        // Set state, or change, if necessary
        String paramStateStr = httpServletRequest.getParameter(STATE_PARAM);

        if ("US".equalsIgnoreCase(paramStateStr)) {
            context.setState(null);
            _log.debug("Internal testing:  clearing user's state context.");
            return;
        }

        if (!StringUtils.isEmpty(paramStateStr) && paramStateStr.length() >= 2) {
            final State currState = context.getState();
            State state = _stateManager.getState(paramStateStr.substring(0, 2));

            if (currState == null && state == null) {
                _log.debug("No existing state in session and bogus, non-empty state through url param.");
            }

            if (state != null) {
                context.setState(state);
                _stateCookieGenerator.addCookie(httpServletResponse, state.getAbbreviation());
                _log.debug("switching user's state: " + state);
            }
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

    /**
     * Create a new session context every time.
     * Then, pull information out of cookies and parameters.
     * We also pull things out the request because this is a convenient way
     * to pass things in from include tags.
     */
    public SessionContext guaranteeSessionContext(HttpServletRequest request) {
        SessionContext context =
                (SessionContext) _applicationContext.getBean(SessionContext.BEAN_ID);

        request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, context);
        return context;
    }

    /**
     * Do all that is necessary so that the SessionContext is in place and available.
     * This is useful in unit tests.
     */
    public SessionContext prepareSessionContext(
            HttpServletRequest request,
            HttpServletResponse response) {
        /*
        The normal case is that things are persisted in individual cookies. We support
        the ability to add request-level attributes for testing and a few other
        cases. We allow parameter overriding as a standard way to test and in some
        cases to change state. See the "state" parameter for an example of this.
        */
        SessionContext context = guaranteeSessionContext(request);
        readCookies(request, context);
        updateFromRequestAttributes(request, context);
        updateFromParams(request, response, context);
        saveCookies(response, context);
        return context;
    }

    private void saveCookies(HttpServletResponse response, SessionContext context) {

        // Stash away member's information so we don't look it up every time.
        if (context.getMemberId() != null && !context.isReadFromClient()) {
            User user = context.getUser();
            if (user != null) {
                ClientSideSessionCache sessionCache = new ClientSideSessionCache(user);
                // make sure the newly created session cache persists the user hash
                sessionCache.setUserHash(context.getUserHash());
                updateContextFromCache(context, sessionCache);
                String c = sessionCache.getCookieRepresentation();
                _sessionCacheCookieGenerator.addCookie(response, c);

            }
        }

    }

    public ClientSideSessionCache createUserInfo(User user) {
        return new ClientSideSessionCache(null);
    }

    public static SessionContext getSessionContext(HttpServletRequest request) {
        return (SessionContext) request.getAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME);
    }

    public void changeUser(SessionContext context, HttpServletResponse response, User user) {
        if (user != null) {
            _memberIdCookieGenerator.addCookie(response, user.getId().toString());
        } else {
            _log.error("Tried to set member id for a null user");
        }

    }
    public void changeAuthorization(HttpServletRequest request, HttpServletResponse response, User user, String hash) {
        changeAuthorization(request, response, user, hash, false);
    }

    public void changeAuthorization(HttpServletRequest request, HttpServletResponse response, User user, String hash, boolean rememberMe) {
        if (user != null) {
            ClientSideSessionCache cache = new ClientSideSessionCache(user);
            cache.setUserHash(hash);
            if (user.getUserProfile() != null) {
                cache.setScreenName(user.getUserProfile().getScreenName());
            }
            _sessionCacheCookieGenerator.addCookie(response, cache.getCookieRepresentation());
            if (StringUtils.isEmpty(_communityCookieGenerator.getCookieName())) {
                _communityCookieGenerator.setCookieName("community_" + getServerName(request));
                _communityCookieGenerator.setCookieDomain(".greatschools.net");
            }
            if (rememberMe) {
                _communityCookieGenerator.setCookieMaxAge(COMMUNITY_COOKIE_MAX_AGE);
            } else {
                _communityCookieGenerator.setCookieMaxAge(-1);
            }
            _communityCookieGenerator.addCookie(response, hash);
        } else {
            _log.warn("Attempt to change authorization information on null user ignored.");
        }
    }

    public void clearTempMsg(HttpServletResponse response) {
        _tempMsgCookieGenerator.removeCookie(response);
    }

    public void setTempMsg(HttpServletResponse response, String cookieValue) {
        _tempMsgCookieGenerator.setCookieMaxAge(-1);
        _tempMsgCookieGenerator.addCookie(response, cookieValue);
    }

    public static String getServerName(HttpServletRequest request) {
        PageHelper pageHelper = new PageHelper(getSessionContext(request), request);
        if (pageHelper.isStagingServer()) {
            return "staging";
        } else if (pageHelper.isDevEnvironment()) {
            return "dev";
        }
        return "www";
    }

    public String getCommunityHost(HttpServletRequest request) {
        String serverName = request.getServerName();
        if (_urlUtil.isStagingServer(serverName)) {
            return COMMUNITY_STAGING_HOSTNAME;
        } else if (_urlUtil.isDevEnvironment(serverName)) {
            return COMMUNITY_DEV_HOSTNAME;
        }
        return COMMUNITY_LIVE_HOSTNAME;
    }
}
