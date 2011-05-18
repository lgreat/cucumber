/*
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: SessionContextUtil.java,v 1.85 2011/05/18 02:03:49 yfan Exp $
 */

package gs.web.util.context;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.geo.City;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.community.ClientSideSessionCache;
import gs.web.community.registration.AuthenticationManager;
import gs.web.util.CookieUtil;
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.org">Andrew J. Peterson</a>
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
     * Insider log-in cookie, from Java site. Domain is ".greatschools.org".
     */
    private static final String MEMBER_ID_INSIDER_COOKIE = "MEMBER";

    /**
     * My School List cookie, for backward compatibility. Domain used to be ".greatschools.org",
     * but now it the default (www.greatschools.org or cobrand domain).
     */
    public static final String MEMBER_ID_COOKIE = "MEMID";

    /**
     * Tracking number cookie we set to determine variant
     */
    public static final String TRACKING_NUMBER = "TRACKING_NUMBER";

    /**
     * Cookie we set to track what cobrand a user is on
     */
    public static final String COBRAND_COOKIE = "COBRAND";

    /**
     * Cookie we set to track what cobrand a user is on
     */
    public static final String COBRAND_TYPE_COOKIE = "COBRAND_TYPE";

    // City ID cookie - id is based on us_geo.city.id  - Domain is ".greatschools.org".
    public static final String CITY_ID_COOKIE = "CITYID";

    /* Browsing state session cookies */
    // STATE, PATHWAY

    /* User information cached cookies */

    private static final Pattern CRAWLER_USER_AGENTS =
            Pattern.compile(".*(googlebot|mediapartners-google|slurp|mmcrawler|msnbot|teoma|ia_archiver|twiceler).*");

    private static Pattern LONG_STATE_URI_PATTERN = null;

    private static final Log _log = LogFactory.getLog(SessionContextUtil.class);
    private ApplicationContext _applicationContext;

    private IUserDao _userDao;
    private StateManager _stateManager;
    private UrlUtil _urlUtil = new UrlUtil();

    private CookieGenerator _omnitureSubCookieGenerator;
    private CookieGenerator _stateCookieGenerator;
    private CookieGenerator _memberCookieGenerator;
    private CookieGenerator _newMemberCookieGenerator;
    private CookieGenerator _memberIdCookieGenerator;
    private CookieGenerator _hasSearchedCookieGenerator;
    private CookieGenerator _sessionCacheCookieGenerator;
    private CookieGenerator _communityCookieGenerator;
    private CookieGenerator _tempMsgCookieGenerator;
    private CookieGenerator _cityIdCookieGenerator;
    private CookieGenerator _sitePrefCookieGenerator;
    private CookieGenerator _kindercareLeadGenCookieGenerator;
    private CookieGenerator _care2PromoCookieGenerator;
    private CookieGenerator _searchResultsCookieGenerator;
    public static final String COMMUNITY_LIVE_HOSTNAME = "community.greatschools.org";
    public static final String COMMUNITY_STAGING_HOSTNAME = "community.staging.greatschools.org";
    public static final String COMMUNITY_DEV_HOSTNAME = "community.dev.greatschools.org";
    public static final String COMMUNITY_PRERELEASE_HOSTNAME = "comgen1.greatschools.org:8000";

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
                } else if (StringUtils.equals(_cityIdCookieGenerator.getCookieName(), thisCookie.getName())) {
                    try {
                        context.setCityId(Integer.parseInt(thisCookie.getValue()));
                    } catch (NumberFormatException e) {
                        _log.warn("Invalid CityID: " + thisCookie.getValue());
                    }
                } else if (StringUtils.equals("STATE", thisCookie.getName())) {
                    // Check for this value in case new state cookie isn't present.
                    // This allows old users to retain their state cookie.
                    // The value will be transferred from the SessionContext into a STATE3 cookie in
                    // the response.
                    String state = thisCookie.getValue();
                    State s = _stateManager.getState(state);
                    if (s != null) {
                        oldCookiedState = s;
                    }
                } else if (StringUtils.equals("STATE2", thisCookie.getName())) {
                    // Check for this value in case new state cookie isn't present.
                    // This allows old users to retain their state cookie.
                    // The value will be transferred from the SessionContext into a STATE3 cookie in
                    // the response.
                    String state = thisCookie.getValue();
                    State s = _stateManager.getState(state);
                    if (s != null) {
                        oldCookiedState = s;
                    }
                } else if (StringUtils.equals("community_" + getServerName(httpServletRequest), thisCookie.getName())) {
                    // GS-3819
                    isCommunity = true;
                    // pull member id out of community cookie if necessary
                    if (insiderId == -1) {
                        try {
                            insiderId = AuthenticationManager.getUserIdFromCookieValue(thisCookie.getValue());
                        } catch (Exception e) {
                            _log.warn("Unable to parse member id out of community cookie: " + e);
                            _log.warn("Cookie info {name:" + thisCookie.getName() +
                                    "; value:" + thisCookie.getValue() +
                                    "; domain:" + thisCookie.getDomain() +
                                    "; path:" + thisCookie.getPath() +
                                    "; version:" + thisCookie.getVersion() + "}");
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
                    ((cache == null) || StringUtils.isEmpty(cache.getScreenName())) &&
                    context.getUser() != null) {
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

        String uri = httpServletRequest.getRequestURI();
        if (uri != null && (uri.contains("/content/") || uri.contains("/education-topics/") || uri.contains("/articles/") || uri.contains("/school-choice/") || uri.contains(".gs") || uri.contains(".topic"))) {
            context.setIsTopicPage(true);
        } else if (uri != null && (uri.endsWith("/community.gs") || uri.endsWith("/community/discussion.gs")  || uri.endsWith("/community/questions.gs")|| uri.startsWith("/account/") || uri.startsWith("/members/") || uri.startsWith("/search/contentSearch.page")) || uri.startsWith("/community/questions/")) {
            context.setIsTopicPage(true);
        } else {
            String queryString = httpServletRequest.getQueryString();
            if (queryString != null && queryString.contains("c=topic")) {
                context.setIsTopicPage(true);
            }
        }
    }

    /**
     * Grab the original request URI before tomcat resets it to the JSP that is forwarded to
     *
     * @param request
     * @param sessionContext
     */
    public void updateFromRequestURI(HttpServletRequest request, HttpServletResponse response, SessionContext sessionContext) {
        sessionContext.setOriginalRequestURI(request.getRequestURI());
        updateStateFromRequestURI(request, response, sessionContext);
    }

    private void updateHostnameCobrandFromParams(HttpServletRequest request, SessionContext context) {
        // Get the real hostname or see if it's been overridden
        String paramHost = request.getParameter(HOST_PARAM);
        String hostName = StringUtils.isEmpty(paramHost) ? request.getServerName() : paramHost;

        // Determine if we're running in the integration test environment
        if ("localhost.greatschools.org".equals(hostName)) {
            context.setIntegrationTest(true);
        }

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

        // Now see if we need to override the hostName
        hostName = _urlUtil.buildPerlHostName(hostName, cobrand);

        context.setHostName(hostName);
        context.setCobrand(cobrand);
    }

    public static boolean isKnownCrawler(String userAgent) {
        return userAgent != null && CRAWLER_USER_AGENTS.matcher(userAgent.toLowerCase()).matches();
    }

    public static boolean isIphone(String userAgent) {
        return userAgent != null && userAgent.contains("iPhone");
    }

    public static boolean isIpad(String userAgent) {
        return userAgent != null && userAgent.contains("iPad");
    }

    public static boolean isIpod(String userAgent) {
        return userAgent != null && userAgent.contains("iPod");
    }

    public static boolean isIos(String userAgent) {
        return isIphone(userAgent) || isIpad(userAgent) || isIpod(userAgent);
    }

    public static boolean isSafari(String userAgent) {
        return userAgent != null && userAgent.contains("Safari") && !userAgent.contains("Chrome");
    }

    public static boolean isIosSafari(String userAgent) {
        return isIos(userAgent) && isSafari(userAgent);
    }

    /**
     * Called at the beginning of the request; called after #readCookies is called.
     * Allows this class to do common operations for all pages.
     */
    public void updateFromParams(HttpServletRequest request,
                                 HttpServletResponse response,
                                 SessionContext context) {
        updateHostnameCobrandFromParams(request, context);
        // Determine if this is a crawler
        String userAgent = request.getHeader("User-Agent");
        context.setCrawler(SessionContextUtil.isKnownCrawler(userAgent));

        context.setIphone(SessionContextUtil.isIphone(userAgent));
        context.setIpad(SessionContextUtil.isIpad(userAgent));
        context.setIpod(SessionContextUtil.isIpod(userAgent));
        context.setIos(SessionContextUtil.isIos(userAgent));
        context.setIosSafari(SessionContextUtil.isIosSafari(userAgent));

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


    public void setOmnitureSubCookieGenerator(CookieGenerator cookieGenerator) {
        _omnitureSubCookieGenerator = cookieGenerator;
    }

    public CookieGenerator getOmnitureSubCookieGenerator() {
        return _omnitureSubCookieGenerator;
    }

    public void setStateCookieGenerator(CookieGenerator stateCookieGenerator) {
        _stateCookieGenerator = stateCookieGenerator;
    }

    public void setMemberCookieGenerator(CookieGenerator memberCookieGenerator) {
        _memberCookieGenerator = memberCookieGenerator;
    }

    public void setNewMemberCookieGenerator(CookieGenerator newMemberCookieGenerator) {
        _newMemberCookieGenerator = newMemberCookieGenerator;
    }

    public CookieGenerator getNewMemberCookieGenerator() {
        return _newMemberCookieGenerator;
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

    public CookieGenerator getCityIdCookieGenerator() {
        return _cityIdCookieGenerator;
    }

    public void setCityIdCookieGenerator(CookieGenerator cityIdCookieGenerator) {
        _cityIdCookieGenerator = cityIdCookieGenerator;
    }

    public CookieGenerator getSitePrefCookieGenerator() {
        return _sitePrefCookieGenerator;
    }

    public void setSitePrefCookieGenerator(CookieGenerator sitePrefCookieGenerator) {
        _sitePrefCookieGenerator = sitePrefCookieGenerator;
    }

    public CookieGenerator getKindercareLeadGenCookieGenerator() {
        return _kindercareLeadGenCookieGenerator;
    }

    public void setKindercareLeadGenCookieGenerator(CookieGenerator kindercareLeadGenCookieGenerator) {
        _kindercareLeadGenCookieGenerator = kindercareLeadGenCookieGenerator;
    }

    public CookieGenerator getCare2PromoCookieGenerator() {
        return _care2PromoCookieGenerator;
    }

    public void setCare2PromoCookieGenerator(CookieGenerator care2OverlayCookieGenerator) {
        _care2PromoCookieGenerator = care2OverlayCookieGenerator;
    }

    public CookieGenerator getSearchResultsCookieGenerator() {
        return _searchResultsCookieGenerator;
    }

    public void setSearchResultsCookieGenerator(CookieGenerator searchResultsCookieGenerator) {
        _searchResultsCookieGenerator = searchResultsCookieGenerator;
    }

    /**
     * Grab the original request URI before tomcat resets it to the JSP that is forwarded to
     *
     * @param request
     * @param response
     * @param context
     */
    public void updateStateFromRequestURI(HttpServletRequest request, HttpServletResponse response,
                                          SessionContext context) {
        State state = null;
        if (request.getRequestURI().toLowerCase().startsWith("/district-of-columbia")) {
            state = State.DC;
        } else {
            Matcher matcher = getLongStateUriPattern().matcher(request.getRequestURI());
            boolean matchFound = matcher.find();

            if (matchFound) {
                state = _stateManager.getStateByLongName(matcher.group(1).replaceAll("-", " "));
            }
        }

        if (state != null) {
            final State currState = context.getState();
            updateStateHelper(context, request, response, currState, state);
        }
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

            updateStateHelper(context, httpServletRequest, httpServletResponse, currState, state);
        }
    }

    public void updateState(SessionContext context,
                                   HttpServletRequest httpServletRequest,
                                   HttpServletResponse httpServletResponse,
                                   State newState) {
        updateStateHelper(context, httpServletRequest, httpServletResponse, null, newState);
    }

    private void updateStateHelper(SessionContext context,
                                   HttpServletRequest httpServletRequest,
                                   HttpServletResponse httpServletResponse,
                                   State currState, State newState) {
        if (currState == null && newState == null) {
            _log.debug("No existing state in session and bogus, non-empty state through url param.");
        }

        if (newState != null) {
            context.setState(newState);
            //_stateCookieGenerator.setCookieDomain();
            if (UrlUtil.isDeveloperWorkstation(httpServletRequest.getServerName())) {
                // don't set domain for developer workstations
                // so they can still access the cookie!!
                _stateCookieGenerator.setCookieDomain(null);
            } else {
                _stateCookieGenerator.setCookieDomain(".greatschools.org");
            }

            _stateCookieGenerator.addCookie(httpServletResponse, newState.getAbbreviation());
            _log.debug("switching user's state: " + newState);
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
        updateFromRequestURI(request, response, context);
        updateFromRequestAttributes(request, context);
        updateFromParams(request, response, context);
        saveCookies(response, context);
        // TODO: GS-8867 Added 11/03/2009, can be removed in ~6 months
        // Anyone who is signed in but doesn't have the new member cookie, create it now
        if (PageHelper.isCommunityCookieSet(request)
                && !CookieUtil.hasCookie(request, _newMemberCookieGenerator.getCookieName())) {
            setUserIsMember(request, response);
        }
        // END GS-8867
        return context;
    }

    public void saveCookies(HttpServletResponse response, SessionContext context) {
        // Stash away member's information so we don't look it up every time.
        if (context.getMemberId() != null) {
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

    public void changeUser(HttpServletRequest request, HttpServletResponse response, User user) {
        if (user != null) {
            if (!UrlUtil.isDeveloperWorkstation(request.getServerName())) {
                _memberIdCookieGenerator.setCookieDomain(".greatschools.org");
            }
            _memberIdCookieGenerator.addCookie(response, user.getId().toString());
        } else {
            _log.error("Tried to set member id for a null user");
        }

    }

    public void changeAuthorization(HttpServletRequest request, HttpServletResponse response, User user, String hash) {
        changeAuthorization(request, response, user, hash, false);
    }

    public void setUserIsMember(HttpServletRequest request, HttpServletResponse response) {
        _newMemberCookieGenerator.addCookie(response, "y");
        if (!UrlUtil.isDeveloperWorkstation(request.getServerName())) {
            _newMemberCookieGenerator.setCookieDomain(".greatschools.org");
        }
    }

    public void changeAuthorization(HttpServletRequest request, HttpServletResponse response, User user, String hash, boolean rememberMe) {
        if (user != null) {
            setUserIsMember(request, response);
            ClientSideSessionCache cache = new ClientSideSessionCache(user);
            cache.setUserHash(hash);
            if (user.getUserProfile() != null) {
                cache.setScreenName(user.getUserProfile().getScreenName());
            }
            _sessionCacheCookieGenerator.addCookie(response, cache.getCookieRepresentation());
            if (StringUtils.isEmpty(_communityCookieGenerator.getCookieName())) {
                _communityCookieGenerator.setCookieName("community_" + getServerName(request));
                if (!UrlUtil.isDeveloperWorkstation(request.getServerName())) {
                    // don't set domain for developer workstations
                    // so they can still access the cookie!!
                    _communityCookieGenerator.setCookieDomain(".greatschools.org");
                }
            }
            if (rememberMe) {
                _communityCookieGenerator.setCookieMaxAge(COMMUNITY_COOKIE_MAX_AGE);
            } else {
                _communityCookieGenerator.setCookieMaxAge(-1);
            }
            _communityCookieGenerator.addCookie(response,  StringUtils.replace(hash ,"=","~"));
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
        SessionContext sessionContext = getSessionContext(request);
        if (sessionContext == null) {
            _log.warn("Unable to determine server name because of null SessionContext. Defaulting to www");
            return "www";
        }
        if (StringUtils.isEmpty(sessionContext.getHostName())) {
            sessionContext.getSessionContextUtil().updateHostnameCobrandFromParams(request, sessionContext);
        }
        PageHelper pageHelper = new PageHelper(sessionContext, request);
        if (pageHelper.isStagingServer()) {
            return "staging";
        } else if (pageHelper.isDevEnvironment()) {
            return "dev";
        }
        return "www";
    }

    public String getCommunityHost(HttpServletRequest request) {
        String serverName = request.getServerName();
        if (UrlUtil.isStagingServer(serverName)) {
            return COMMUNITY_STAGING_HOSTNAME;
        } else if (UrlUtil.isDevEnvironment(serverName)) {
            return COMMUNITY_DEV_HOSTNAME;
        } else if (UrlUtil.isPreReleaseServer(serverName)) {
            return COMMUNITY_PRERELEASE_HOSTNAME;
        }
        return COMMUNITY_LIVE_HOSTNAME;
    }

    public void changeCity(SessionContext context, HttpServletRequest req, HttpServletResponse res, Integer cityId) {
        context.setCityId(cityId);
        if (!UrlUtil.isDeveloperWorkstation(req.getServerName())) {
            _cityIdCookieGenerator.setCookieDomain(".greatschools.org");
        }
        _cityIdCookieGenerator.addCookie(res, cityId.toString());
    }

    public void changeCity(SessionContext context, HttpServletRequest req, HttpServletResponse res, City city) {
        changeCity(context, req, res, city.getId());
    }

    public void clearUserCookies(HttpServletResponse response) {
        _memberIdCookieGenerator.removeCookie(response);
        _sessionCacheCookieGenerator.removeCookie(response);
        _memberCookieGenerator.removeCookie(response);
        // Before a user logs into community, the name of the community cookie is blank. Thus, we
        // do this check in order to avoid a NPE.
        if (!StringUtils.isBlank(_communityCookieGenerator.getCookieName())) {
            _communityCookieGenerator.removeCookie(response);
        }
    }

    public Pattern getLongStateUriPattern() {
        if (LONG_STATE_URI_PATTERN == null) {
            StringBuffer longStatePattern = new StringBuffer("/(");
            List<State> states = _stateManager.getListByAbbreviations();
            for (int i = 0; i < states.size(); i++) {
                if (i > 0) longStatePattern.append("|");
                longStatePattern.append(states.get(i).getLongName().replaceAll(" ", "-").toLowerCase());
            }
            longStatePattern.append(")");
            LONG_STATE_URI_PATTERN = Pattern.compile(longStatePattern.toString(), Pattern.CASE_INSENSITIVE);
        }
        return LONG_STATE_URI_PATTERN;
    }
}