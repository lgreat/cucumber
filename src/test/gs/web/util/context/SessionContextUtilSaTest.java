package gs.web.util.context;

import gs.data.community.User;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.web.BaseTestCase;
import gs.web.GsMockHttpServletRequest;
import static org.easymock.EasyMock.*;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.Cookie;

/**
 * Provides testing for the SessionContextUtil class. Note this class was created for revision
 * 1.14 of SessionContextUtil and is not comprehensive ... I'm only testing certain methods that I've
 * modified / created in revision 1.14.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 * @TODO Expand test cases to all methods in SessionContextUtil
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
        stateGen.setCookieName("STATE3");
        _sessionContextUtil.setStateCookieGenerator(stateGen);
        _sessionContextUtil.setTempMsgCookieGenerator(new CookieGenerator());
        CookieGenerator _cityIdCookieGenerator = new CookieGenerator();
        _cityIdCookieGenerator.setCookieName("CITYID");
        _sessionContextUtil.setCityIdCookieGenerator(_cityIdCookieGenerator);
        CookieGenerator _newMemberCookieGenerator = new CookieGenerator();
        _newMemberCookieGenerator.setCookieName("isMember");
        _sessionContextUtil.setNewMemberCookieGenerator(_newMemberCookieGenerator);
        _sessionContextUtil.setStateManager(new StateManager());
    }

    private void setServerName(String serverName) {
        _request.setServerName(serverName);
        _sessionContextUtil.updateFromParams(_request, _response, _sessionContext);
    }

    private static GsMockHttpServletRequest getRequestWithUserAgent(String userAgent) {
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setServerName("www.greatschools.org");
        request.addHeader("User-Agent", userAgent);
        return request;
    }

    public void testIsKnownCrawler() {
        assertFalse(SessionContextUtil.isKnownCrawler(null));
        assertFalse(SessionContextUtil.isKnownCrawler("Mozilla/4.0 (compatible; MSIE 4.01; Windows 95)"));
        assertTrue(SessionContextUtil.isKnownCrawler("Mozilla/5.0 (compatible; Googlebot/2.1; http://www.google.com/bot.html)"));
        assertTrue(SessionContextUtil.isKnownCrawler("Mozilla/5.0 (Twiceler-0.9 http://www.cuil.com/twiceler/robot.html)"));
        assertTrue(SessionContextUtil.isKnownCrawler("Mozilla/5.0 (compatible; Yahoo! Slurp; http://help.yahoo.com/help/us/ysearch/slurp)"));
    }

    public void testUpdateFromParamsDetectsCrawler() {
        _sessionContextUtil.updateFromParams(_request, _response, _sessionContext);
        assertFalse(_sessionContext.isCrawler());

        _sessionContextUtil.updateFromParams(getRequestWithUserAgent("Mozilla/5.0 (compatible; Yahoo! Slurp; http://help.yahoo.com/help/us/ysearch/slurp)"), _response, _sessionContext);
        assertTrue(_sessionContext.isCrawler());
    }

    public void testGetServerName() {
        // dev environment
        setServerName("dev.greatschools.org");
        assertEquals("dev", SessionContextUtil.getServerName(_request));

        setServerName("aroy.office.greatschools.org");
        assertEquals("dev", SessionContextUtil.getServerName(_request));

        setServerName("localhost:8080");
        assertEquals("dev", SessionContextUtil.getServerName(_request));

        // staging environment
        setServerName("staging.greatschools.org");
        assertEquals("staging", SessionContextUtil.getServerName(_request));

        setServerName("sfgate.staging.greatschools.org");
        assertEquals("staging", SessionContextUtil.getServerName(_request));

        // live environment
        setServerName("www.greatschools.org");
        assertEquals("www", SessionContextUtil.getServerName(_request));

        setServerName("sfgate.greatschools.org");
        assertEquals("www", SessionContextUtil.getServerName(_request));
    }

    public void testChangeAuthorizationRememberMe() {
        User user = new User();
        user.setId(1);
        user.setEmail("email@example.com");
        String hash = "hash";
        setServerName("dev.greatschools.org");
        _sessionContextUtil.changeAuthorization(_request, _response, user, hash, true);

        Cookie cookie = _response.getCookie("community_dev");
        assertNotNull("Cookie should exist under name community_dev", cookie);
        assertEquals(".greatschools.org", cookie.getDomain());
        assertEquals(SessionContextUtil.COMMUNITY_COOKIE_MAX_AGE, cookie.getMaxAge());
        assertNotNull("New member cookie should be set on login/register", _response.getCookie("isMember"));
    }

    public void testChangeAuthorizationForgetMe() {
        User user = new User();
        user.setId(1);
        user.setEmail("email@example.com");
        String hash = "hash";
        setServerName("dev.greatschools.org");
        _sessionContextUtil.changeAuthorization(_request, _response, user, hash, false);

        Cookie cookie = _response.getCookie("community_dev");
        assertNotNull("Cookie should exist under name community_dev", cookie);
        assertEquals(".greatschools.org", cookie.getDomain());
        assertEquals(-1, cookie.getMaxAge());
        assertNotNull("New member cookie should be set on login/register", _response.getCookie("isMember"));
    }

    public void testChangeAuthorizationDomainStaging() {
        User user = new User();
        user.setId(1);
        user.setEmail("email@example.com");
        String hash = "hash";
        Cookie cookie;

        setServerName("staging.greatschools.org");
        _sessionContextUtil.changeAuthorization(_request, _response, user, hash, false);

        cookie = _response.getCookie("community_staging");
        assertNotNull("Cookie should exist under name community_staging", cookie);
        assertEquals(".greatschools.org", cookie.getDomain());
    }

    public void testChangeAuthorizationDomainWww() {
        User user = new User();
        user.setId(1);
        user.setEmail("email@example.com");
        String hash = "hash";
        Cookie cookie;

        setServerName("www.greatschools.org");
        _sessionContextUtil.changeAuthorization(_request, _response, user, hash, false);

        cookie = _response.getCookie("community_www");
        assertNotNull("Cookie should exist under name community_www", cookie);
        assertEquals(".greatschools.org", cookie.getDomain());
    }

    public void testChangeAuthorizationDomainLocalhost() {
        User user = new User();
        user.setId(1);
        user.setEmail("email@example.com");
        String hash = "hash";
        Cookie cookie;

        setServerName("localhost");
        _sessionContextUtil.changeAuthorization(_request, _response, user, hash, false);

        cookie = _response.getCookie("community_dev");
        assertNotNull("Cookie should exist under name community_dev", cookie);
        assertNull("Expect no cookie domain set for localhost", cookie.getDomain());
    }

    public void testStateCookie() {
        Cookie newStateCookie = new Cookie("STATE3", "CA");
        Cookie oldStateCookie1 = new Cookie("STATE", "AK");
        Cookie oldStateCookie2 = new Cookie("STATE2", "TX");

        assertNull(_sessionContext.getState());

        _request.setCookies(new Cookie[]{newStateCookie});
        _sessionContextUtil.readCookies(_request, _sessionContext);
        assertEquals("Value not read from STATE3 cookie", State.CA, _sessionContext.getState());

        _request.setCookies(new Cookie[]{oldStateCookie1});
        _sessionContextUtil.readCookies(_request, _sessionContext);
        assertEquals("Value did not fall back to old STATE cookie", State.AK, _sessionContext.getState());

        _request.setCookies(new Cookie[]{newStateCookie, oldStateCookie1});
        _sessionContextUtil.readCookies(_request, _sessionContext);
        assertEquals("Value should always default to new STATE3 cookie", State.CA, _sessionContext.getState());

        // make sure order of cookie doesn't matter
        _request.setCookies(new Cookie[]{oldStateCookie1, newStateCookie});
        _sessionContextUtil.readCookies(_request, _sessionContext);
        assertEquals("Value should always default to new STATE3 cookie", State.CA, _sessionContext.getState());

        _request.setCookies(new Cookie[]{oldStateCookie2});
        _sessionContextUtil.readCookies(_request, _sessionContext);
        assertEquals("Value did not fall back to old STATE cookie", State.TX, _sessionContext.getState());

        _request.setCookies(new Cookie[]{newStateCookie, oldStateCookie2});
        _sessionContextUtil.readCookies(_request, _sessionContext);
        assertEquals("Value should always default to new STATE3 cookie", State.CA, _sessionContext.getState());

        // make sure order of cookie doesn't matter
        _request.setCookies(new Cookie[]{oldStateCookie2, newStateCookie});
        _sessionContextUtil.readCookies(_request, _sessionContext);
        assertEquals("Value should always default to new STATE3 cookie", State.CA, _sessionContext.getState());
    }

    public void testGetCommunityHostForProduction() {
        _request.setServerName("www.greatschools.org");
        assertEquals("Unexpected community host", SessionContextUtil.COMMUNITY_LIVE_HOSTNAME, _sessionContextUtil.getCommunityHost(_request));
        _request.setServerName("sfgate.greatschools.org");
        assertEquals("Unexpected community host for sfgate cobrand domain", SessionContextUtil.COMMUNITY_LIVE_HOSTNAME, _sessionContextUtil.getCommunityHost(_request));
        _request.setServerName("somenewcobrand.greatschools.org");
        assertEquals("Unexpected community host for some other cobrand domain", SessionContextUtil.COMMUNITY_LIVE_HOSTNAME, _sessionContextUtil.getCommunityHost(_request));
    }

    public void testGetCommunityHostForStaging() {
        _request.setServerName("staging.greatschools.org");
        assertEquals("Unexpected staging community host", SessionContextUtil.COMMUNITY_STAGING_HOSTNAME, _sessionContextUtil.getCommunityHost(_request));
        _request.setServerName("sfgate.staging.greatschools.org");
        assertEquals("Unexpected community host for sfgate cobrand domain on staging", SessionContextUtil.COMMUNITY_STAGING_HOSTNAME, _sessionContextUtil.getCommunityHost(_request));
        _request.setServerName("somenewcobrand.staging.greatschools.org");
        assertEquals("Unexpected community host for some other cobrand domain on staging", SessionContextUtil.COMMUNITY_STAGING_HOSTNAME, _sessionContextUtil.getCommunityHost(_request));
    }

    public void testGetCommunityHostForPrerelease() {
        _request.setServerName("rithmatic.greatschools.org");
        assertEquals("Unexpected rithmatic community host", SessionContextUtil.COMMUNITY_PRERELEASE_HOSTNAME, _sessionContextUtil.getCommunityHost(_request));
        _request.setServerName("sfgate.rithmatic.greatschools.org");
        assertEquals("Unexpected community host for sfgate cobrand domain on rithmatic", SessionContextUtil.COMMUNITY_PRERELEASE_HOSTNAME, _sessionContextUtil.getCommunityHost(_request));
    }

    public void testGetCommunityHostForDev() {
        _request.setServerName("dev.greatschools.org");
        assertEquals("Unexpected dev community host", SessionContextUtil.COMMUNITY_DEV_HOSTNAME, _sessionContextUtil.getCommunityHost(_request));
        _request.setServerName("main.dev.greatschools.org");
        assertEquals("Unexpected dev community host", SessionContextUtil.COMMUNITY_DEV_HOSTNAME, _sessionContextUtil.getCommunityHost(_request));
        _request.setServerName("sfgate.dev.greatschools.org");
        assertEquals("Unexpected community host for sfgate cobrand domain on dev", SessionContextUtil.COMMUNITY_DEV_HOSTNAME, _sessionContextUtil.getCommunityHost(_request));
        _request.setServerName("somenewcobrand.dev.greatschools.org");
        assertEquals("Unexpected community host for some other cobrand domain on dev", SessionContextUtil.COMMUNITY_DEV_HOSTNAME, _sessionContextUtil.getCommunityHost(_request));
        _request.setServerName("aroy.office.greatschools.org");
        assertEquals("Unexpected community host for an office workstation", SessionContextUtil.COMMUNITY_DEV_HOSTNAME, _sessionContextUtil.getCommunityHost(_request));
    }

    public void testUpdateFromRequestURI() {
        ApplicationContext ac = createMock(ApplicationContext.class);
        expect(ac.getBean(SessionContext.BEAN_ID)).andReturn(_sessionContext);
        replay(ac);
        _sessionContextUtil.setApplicationContext(ac);
        _request.setRequestURI("/some/path");
        _sessionContextUtil.prepareSessionContext(_request, _response);

        verify(ac);
        assertEquals("Expected to find orginal URI in request", "/some/path", _sessionContext.getOriginalRequestURI());
        reset(ac);
    }

    public void testUpdateStateFromRequestURI() {
        // this only tests whether or not the session context state is properly set,
        // as updateStateHelper functionality should be tested separately
        _sessionContext.setState(State.OH);
        _request.setRequestURI("/california/san-francisco/schools/");
        _sessionContextUtil.updateStateFromRequestURI(_request, _response, _sessionContext);
        assertEquals("Expected state of CA", State.CA, _sessionContext.getState());

        _request.setRequestURI("/california/los-angeles/schools/");
        _sessionContextUtil.updateStateFromRequestURI(_request, _response, _sessionContext);
        assertEquals("Expected state of CA", State.CA, _sessionContext.getState());

        _request.setRequestURI("/new-york/ithaca/schools/");
        _sessionContextUtil.updateStateFromRequestURI(_request, _response, _sessionContext);
        assertEquals("Expected state of NY", State.NY, _sessionContext.getState());

        _request.setRequestURI("/washington-dc/washington/schools/");
        _sessionContextUtil.updateStateFromRequestURI(_request, _response, _sessionContext);
        assertEquals("Expected state of DC", State.DC, _sessionContext.getState());

        _request.setRequestURI("/Vermont/burlington/schools/");
        _sessionContextUtil.updateStateFromRequestURI(_request, _response, _sessionContext);
        assertEquals("Expected state of VT", State.VT, _sessionContext.getState());

        // Special case for GS-11672
        _request.setRequestURI("/district-of-columbia/washington/schools/");
        _sessionContextUtil.updateStateFromRequestURI(_request, _response, _sessionContext);
        assertEquals("Expected state of DC", State.DC, _sessionContext.getState());

        _request.setRequestURI("/top-high-schools/florida");
        _sessionContextUtil.updateStateFromRequestURI(_request, _response, _sessionContext);
        assertEquals("Expected state of FL", State.FL, _sessionContext.getState());

        _request.setRequestURI("/schools.page?district=717&state=CA");
        _sessionContextUtil.updateStateFromRequestURI(_request, _response, _sessionContext);
        assertEquals("Expected state of FL because the URI matched shouldn't match state=CA", State.FL, _sessionContext.getState());
    }

    public void testCityIdCookie() throws Exception {
        Cookie cityCookie = new Cookie("CITYID", "133917");
        Cookie newCityCookie = new Cookie("CITYID", "12345");

        assertNull(_sessionContext.getCityId());

        _request.setCookies(new Cookie[]{cityCookie});
        _sessionContextUtil.readCookies(_request, _sessionContext);
        assertEquals("Value not read from CITYID cookie", new Integer(133917), _sessionContext.getCityId());

        _request.setCookies(new Cookie[]{newCityCookie});
        _sessionContextUtil.readCookies(_request, _sessionContext);
        assertEquals("Correct value not read from CITYID cookie", new Integer(12345), _sessionContext.getCityId());
    }

    // http://blogs.sitepoint.com/identify-apple-iphone-ipod-ipad-visitors/
    // http://p2p.wrox.com/content/articles/identifying-iphone-safari-user-agent
    // http://www.useragentstring.com/
    // http://www.zytrax.com/tech/web/mobile_ids.html

    public void testIsIphone() {
        assertFalse(SessionContextUtil.isIphone(null));
        assertFalse(SessionContextUtil.isIphone(""));

        // Safari - iPhone
        assertTrue(SessionContextUtil.isIphone("Mozilla/5.0 (iPhone; U; CPU like Mac OS X; en) AppleWebKit/420+ (KHTML, like Gecko) Version/3.0 Mobile/1A543a Safari/419.3"));
        // Safari - iPad
        assertFalse(SessionContextUtil.isIphone("Mozilla/5.0 (iPad; U; CPU OS 3_2 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) version/4.0.4 Mobile/7B367 Safari/531.21.10"));
        // Safari - iPod
        assertFalse(SessionContextUtil.isIphone("Mozilla/5.0 (iPod; U; CPU like Mac OS X; en) AppleWebKit/420.1 (KHTML, like Gecko) Version/3.0 Mobile/3A101a Safari/419.3"));
    }

    public void testIsIpad() {
        assertFalse(SessionContextUtil.isIpad(null));
        assertFalse(SessionContextUtil.isIpad(""));

        // Safari - iPhone
        assertFalse(SessionContextUtil.isIpad("Mozilla/5.0 (iPhone; U; CPU like Mac OS X; en) AppleWebKit/420+ (KHTML, like Gecko) Version/3.0 Mobile/1A543a Safari/419.3"));
        // Safari - iPad
        assertTrue(SessionContextUtil.isIpad("Mozilla/5.0 (iPad; U; CPU OS 3_2 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) version/4.0.4 Mobile/7B367 Safari/531.21.10"));
        // Safari - iPod
        assertFalse(SessionContextUtil.isIpad("Mozilla/5.0 (iPod; U; CPU like Mac OS X; en) AppleWebKit/420.1 (KHTML, like Gecko) Version/3.0 Mobile/3A101a Safari/419.3"));
    }

    public void testIsIpod() {
        assertFalse(SessionContextUtil.isIpod(null));
        assertFalse(SessionContextUtil.isIpod(""));

        // Safari - iPhone
        assertFalse(SessionContextUtil.isIpod("Mozilla/5.0 (iPhone; U; CPU like Mac OS X; en) AppleWebKit/420+ (KHTML, like Gecko) Version/3.0 Mobile/1A543a Safari/419.3"));
        // Safari - iPad
        assertFalse(SessionContextUtil.isIpod("Mozilla/5.0 (iPad; U; CPU OS 3_2 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) version/4.0.4 Mobile/7B367 Safari/531.21.10"));
        // Safari - iPod
        assertTrue(SessionContextUtil.isIpod("Mozilla/5.0 (iPod; U; CPU like Mac OS X; en) AppleWebKit/420.1 (KHTML, like Gecko) Version/3.0 Mobile/3A101a Safari/419.3"));
    }

    public void testIsIos() {
        assertFalse(SessionContextUtil.isIos(null));
        assertFalse(SessionContextUtil.isIos(""));

        // Safari - iPhone
        assertTrue(SessionContextUtil.isIos("Mozilla/5.0 (iPhone; U; CPU like Mac OS X; en) AppleWebKit/420+ (KHTML, like Gecko) Version/3.0 Mobile/1A543a Safari/419.3"));
        // Safari - iPad
        assertTrue(SessionContextUtil.isIos("Mozilla/5.0 (iPad; U; CPU OS 3_2 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) version/4.0.4 Mobile/7B367 Safari/531.21.10"));
        // Safari - iPod
        assertTrue(SessionContextUtil.isIos("Mozilla/5.0 (iPod; U; CPU like Mac OS X; en) AppleWebKit/420.1 (KHTML, like Gecko) Version/3.0 Mobile/3A101a Safari/419.3"));

        // FF4 - Windows
        assertFalse(SessionContextUtil.isIos("Mozilla/5.0 (Windows; U; Windows NT 6.1; ru; rv:1.9.2.3) Gecko/20100401 Firefox/4.0 (.NET CLR 3.5.30729)"));
        // IE9 - WIndows
        assertFalse(SessionContextUtil.isIos("Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; Media Center PC 6.0; InfoPath.3; MS-RTC LM 8; Zune 4.7"));
        // Chrome - Ubuntu Linux
        assertFalse(SessionContextUtil.isIos("Mozilla/5.0 (X11; Linux i686) AppleWebKit/534.35 (KHTML, like Gecko) Ubuntu/10.10 Chromium/13.0.764.0 Chrome/13.0.764.0 Safari/534.35"));
        // Safari - Mac OS X 10.6.6
        assertFalse(SessionContextUtil.isIos("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_6; de-de) AppleWebKit/533.20.25 (KHTML, like Gecko) Version/5.0.4 Safari/533.20.27"));
    }

    public void testIsSafari() {
        assertFalse(SessionContextUtil.isSafari(null));
        assertFalse(SessionContextUtil.isSafari(""));

        // Safari - iPhone
        assertTrue(SessionContextUtil.isSafari("Mozilla/5.0 (iPhone; U; CPU like Mac OS X; en) AppleWebKit/420+ (KHTML, like Gecko) Version/3.0 Mobile/1A543a Safari/419.3"));
        // Safari - iPad
        assertTrue(SessionContextUtil.isSafari("Mozilla/5.0 (iPad; U; CPU OS 3_2 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) version/4.0.4 Mobile/7B367 Safari/531.21.10"));
        // Safari - iPod
        assertTrue(SessionContextUtil.isSafari("Mozilla/5.0 (iPod; U; CPU like Mac OS X; en) AppleWebKit/420.1 (KHTML, like Gecko) Version/3.0 Mobile/3A101a Safari/419.3"));

        // FF4 - Windows
        assertFalse(SessionContextUtil.isSafari("Mozilla/5.0 (Windows; U; Windows NT 6.1; ru; rv:1.9.2.3) Gecko/20100401 Firefox/4.0 (.NET CLR 3.5.30729)"));
        // IE9 - WIndows
        assertFalse(SessionContextUtil.isSafari("Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; Media Center PC 6.0; InfoPath.3; MS-RTC LM 8; Zune 4.7"));
        // Chrome - Ubuntu Linux
        assertFalse(SessionContextUtil.isSafari("Mozilla/5.0 (X11; Linux i686) AppleWebKit/534.35 (KHTML, like Gecko) Ubuntu/10.10 Chromium/13.0.764.0 Chrome/13.0.764.0 Safari/534.35"));
        // Safari - Mac OS X 10.6.6
        assertTrue(SessionContextUtil.isSafari("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_6; de-de) AppleWebKit/533.20.25 (KHTML, like Gecko) Version/5.0.4 Safari/533.20.27"));
    }

    public void testIsIosSafari() {
        assertFalse(SessionContextUtil.isIosSafari(null));
        assertFalse(SessionContextUtil.isIosSafari(""));

        // Safari - iPhone
        assertTrue(SessionContextUtil.isIosSafari("Mozilla/5.0 (iPhone; U; CPU like Mac OS X; en) AppleWebKit/420+ (KHTML, like Gecko) Version/3.0 Mobile/1A543a Safari/419.3"));
        // Safari - iPad
        assertTrue(SessionContextUtil.isIosSafari("Mozilla/5.0 (iPad; U; CPU OS 3_2 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) version/4.0.4 Mobile/7B367 Safari/531.21.10"));
        // Safari - iPod
        assertTrue(SessionContextUtil.isIosSafari("Mozilla/5.0 (iPod; U; CPU like Mac OS X; en) AppleWebKit/420.1 (KHTML, like Gecko) Version/3.0 Mobile/3A101a Safari/419.3"));

        // TODO - non-Safari iOS user agent strings -- very difficult to find!

        // FF4 - Windows
        assertFalse(SessionContextUtil.isIosSafari("Mozilla/5.0 (Windows; U; Windows NT 6.1; ru; rv:1.9.2.3) Gecko/20100401 Firefox/4.0 (.NET CLR 3.5.30729)"));
        // IE9 - WIndows
        assertFalse(SessionContextUtil.isIosSafari("Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; Media Center PC 6.0; InfoPath.3; MS-RTC LM 8; Zune 4.7"));
        // Chrome - Ubuntu Linux
        assertFalse(SessionContextUtil.isIosSafari("Mozilla/5.0 (X11; Linux i686) AppleWebKit/534.35 (KHTML, like Gecko) Ubuntu/10.10 Chromium/13.0.764.0 Chrome/13.0.764.0 Safari/534.35"));
        // Safari - Mac OS X 10.6.6
        assertFalse(SessionContextUtil.isIosSafari("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_6; de-de) AppleWebKit/533.20.25 (KHTML, like Gecko) Version/5.0.4 Safari/533.20.27"));
    }
}
