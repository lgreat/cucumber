package gs.web.util.context;

import gs.data.community.User;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.BaseTestCase;
import gs.web.GsMockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.Cookie;

/**
 * Provides testing for the SessionContextUtil class. Note this class was created for revision
 * 1.14 of SessionContextUtil and is not comprehensive ... I'm only testing certain methods that I've
 * modified / created in revision 1.14.
 *
 * @TODO Expand test cases to all methods in SessionContextUtil
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class SessionContextUtilSaTest extends BaseTestCase {
    private GsMockHttpServletRequest _request;
    private MockHttpServletResponse _response;
    private SessionContextUtil _sessionContextUtil;
    private SessionContext _sessionContext;

    protected void setUp() throws Exception {
        super.setUp();
        _request = new GsMockHttpServletRequest();
        _response = new MockHttpServletResponse();

        _sessionContextUtil = new SessionContextUtil();

        _sessionContext = new SessionContext();
        _sessionContext.setSessionContextUtil(_sessionContextUtil);
        _request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, _sessionContext);

        CookieGenerator sccGen = new CookieGenerator();
        sccGen.setCookieName("SESSION_CACHE");
        _sessionContextUtil.setSessionCacheCookieGenerator(sccGen);
        _sessionContextUtil.setCommunityCookieGenerator(new CookieGenerator());
        _sessionContextUtil.setHasSearchedCookieGenerator(new CookieGenerator());
        CookieGenerator stateGen = new CookieGenerator();
        stateGen.setCookieName("STATE2");
        _sessionContextUtil.setStateCookieGenerator(stateGen);


        _sessionContextUtil.setStateManager(new StateManager());
    }

    private void setServerName(String serverName) {
        _request.setServerName(serverName);
        _sessionContextUtil.updateFromParams(_request, _response, _sessionContext);
    }

    public void testGetServerName() {
        // dev environment
        setServerName("dev.greatschools.net");
        assertEquals("dev", SessionContextUtil.getServerName(_request));

        setServerName("aroy.office.greatschools.net");
        assertEquals("dev", SessionContextUtil.getServerName(_request));

        setServerName("localhost:8080");
        assertEquals("dev", SessionContextUtil.getServerName(_request));

        // staging environment
        setServerName("staging.greatschools.net");
        assertEquals("staging", SessionContextUtil.getServerName(_request));

        setServerName("sfgate.staging.greatschools.net");
        assertEquals("staging", SessionContextUtil.getServerName(_request));

        // live environment
        setServerName("www.greatschools.net");
        assertEquals("www", SessionContextUtil.getServerName(_request));

        setServerName("sfgate.greatschools.net");
        assertEquals("www", SessionContextUtil.getServerName(_request));
    }

    public void testChangeAuthorizationRememberMe() {
        User user = new User();
        user.setId(1);
        user.setEmail("email@example.com");
        String hash = "hash";
        setServerName("dev.greatschools.net");
        _sessionContextUtil.changeAuthorization(_request, _response, user, hash, true);

        Cookie cookie = _response.getCookie("community_dev");
        assertNotNull("Cookie should exist under name community_dev", cookie);
        assertEquals(".greatschools.net", cookie.getDomain());
        assertEquals(SessionContextUtil.COMMUNITY_COOKIE_MAX_AGE, cookie.getMaxAge());
    }

    public void testChangeAuthorizationForgetMe() {
        User user = new User();
        user.setId(1);
        user.setEmail("email@example.com");
        String hash = "hash";
        setServerName("dev.greatschools.net");
        _sessionContextUtil.changeAuthorization(_request, _response, user, hash, false);

        Cookie cookie = _response.getCookie("community_dev");
        assertNotNull("Cookie should exist under name community_dev", cookie);
        assertEquals(".greatschools.net", cookie.getDomain());
        assertEquals(-1, cookie.getMaxAge());
    }

    public void testStateCookie() {
        Cookie newStateCookie = new Cookie("STATE2", "CA");
        Cookie oldStateCookie = new Cookie("STATE", "AK");

        assertNull(_sessionContext.getState());

        _request.setCookies(new Cookie[] {newStateCookie});
        _sessionContextUtil.readCookies(_request, _sessionContext);
        assertEquals("Value not read from STATE2 cookie", State.CA, _sessionContext.getState());

        _request.setCookies(new Cookie[] {oldStateCookie});
        _sessionContextUtil.readCookies(_request, _sessionContext);
        assertEquals("Value did not fall back to old STATE cookie", State.AK, _sessionContext.getState());

        _request.setCookies(new Cookie[] {newStateCookie, oldStateCookie});
        _sessionContextUtil.readCookies(_request, _sessionContext);
        assertEquals("Value should always default to new STATE2 cookie", State.CA, _sessionContext.getState());

        // make sure order of cookie doesn't matter
        _request.setCookies(new Cookie[] {oldStateCookie, newStateCookie});
        _sessionContextUtil.readCookies(_request, _sessionContext);
        assertEquals("Value should always default to new STATE2 cookie", State.CA, _sessionContext.getState());
    }

    public void testGetCommunityHostForProduction() {
        _request.setServerName("www.greatschools.net");
        assertEquals("Unexpected community host", SessionContextUtil.COMMUNITY_LIVE_HOSTNAME, _sessionContextUtil.getCommunityHost(_request));
        _request.setServerName("sfgate.greatschools.net");
        assertEquals("Unexpected community host for sfgate cobrand domain", SessionContextUtil.COMMUNITY_LIVE_HOSTNAME, _sessionContextUtil.getCommunityHost(_request));
        _request.setServerName("somenewcobrand.greatschools.net");
        assertEquals("Unexpected community host for some other cobrand domain", SessionContextUtil.COMMUNITY_LIVE_HOSTNAME, _sessionContextUtil.getCommunityHost(_request));
    }

    public void testGetCommunityHostForStaging() {
        _request.setServerName("staging.greatschools.net");
        assertEquals("Unexpected staging community host", SessionContextUtil.COMMUNITY_STAGING_HOSTNAME, _sessionContextUtil.getCommunityHost(_request));
        _request.setServerName("sfgate.staging.greatschools.net");
        assertEquals("Unexpected community host for sfgate cobrand domain on staging", SessionContextUtil.COMMUNITY_STAGING_HOSTNAME, _sessionContextUtil.getCommunityHost(_request));
        _request.setServerName("somenewcobrand.staging.greatschools.net");
        assertEquals("Unexpected community host for some other cobrand domain on staging", SessionContextUtil.COMMUNITY_STAGING_HOSTNAME, _sessionContextUtil.getCommunityHost(_request));
    }

    public void testGetCommunityHostForDev() {
        _request.setServerName("dev.greatschools.net");
        assertEquals("Unexpected dev community host", SessionContextUtil.COMMUNITY_DEV_HOSTNAME, _sessionContextUtil.getCommunityHost(_request));
        _request.setServerName("main.dev.greatschools.net");
        assertEquals("Unexpected dev community host", SessionContextUtil.COMMUNITY_DEV_HOSTNAME, _sessionContextUtil.getCommunityHost(_request));
        _request.setServerName("sfgate.dev.greatschools.net");
        assertEquals("Unexpected community host for sfgate cobrand domain on dev", SessionContextUtil.COMMUNITY_DEV_HOSTNAME, _sessionContextUtil.getCommunityHost(_request));
        _request.setServerName("somenewcobrand.dev.greatschools.net");
        assertEquals("Unexpected community host for some other cobrand domain on dev", SessionContextUtil.COMMUNITY_DEV_HOSTNAME, _sessionContextUtil.getCommunityHost(_request));
        _request.setServerName("cpickslay.office.greatschools.net");
        assertEquals("Unexpected community host for an office workstation", SessionContextUtil.COMMUNITY_DEV_HOSTNAME, _sessionContextUtil.getCommunityHost(_request));
    }
}
