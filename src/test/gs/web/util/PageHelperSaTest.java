/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: PageHelperSaTest.java,v 1.37 2007/09/10 17:36:52 cpickslay Exp $
 */

package gs.web.util;

import gs.data.community.User;
import gs.data.state.State;
import gs.web.GsMockHttpServletRequest;
import gs.web.ads.AdPosition;
import gs.web.community.ClientSideSessionCache;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import junit.framework.TestCase;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.Cookie;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class PageHelperSaTest extends TestCase {
    private MockHttpServletResponse _response;
    private GsMockHttpServletRequest _request;
    private SessionContext _sessionContext;

    public void testMainSite() {
        SessionContext sessionContext = new MockSessionContext();

        PageHelper pageHelper = new PageHelper(sessionContext, new GsMockHttpServletRequest());

        assertTrue(pageHelper.isShowingBannerAd());
        assertTrue(pageHelper.isShowingLogo());
        assertTrue(pageHelper.isShowingUserInfo());
        assertTrue(pageHelper.isShowingFooter());
        assertTrue(pageHelper.isLogoLinked());
        assertFalse(pageHelper.isAdFree());
    }

    public void testFramed() {
        MockSessionContext sessionFacade = new MockSessionContext();
        sessionFacade.setCobrand("framed");
        assertFramedOptions(sessionFacade);
    }

    public void testNumber1expert() {
        MockSessionContext sessionFacade = new MockSessionContext();
        sessionFacade.setCobrand("number1expert");
        assertFramedOptions(sessionFacade);
    }

    public void testHomegain() {
        MockSessionContext sessionFacade = new MockSessionContext();
        sessionFacade.setCobrand("homegain");
        assertFramedOptions(sessionFacade);
    }

    private void assertFramedOptions(MockSessionContext sessionFacade) {
        PageHelper pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());

        assertFalse(pageHelper.isShowingBannerAd());
        assertFalse(pageHelper.isShowingLogo());
        assertFalse(pageHelper.isShowingUserInfo());
        assertFalse(pageHelper.isLogoLinked());
        assertFalse(pageHelper.isShowingFooter());
        assertTrue(pageHelper.isAdFree());
        assertTrue(pageHelper.isFramed());
    }

    public void testCSR() {
        MockSessionContext sessionFacade = new MockSessionContext();
        sessionFacade.setCobrand("charterschoolratings");
        PageHelper pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());

        assertTrue(pageHelper.isShowingBannerAd());
        assertTrue(pageHelper.isShowingFooter());
        assertFalse(pageHelper.isAdFree());
        assertFalse(pageHelper.isFramed());
    }

    public void testSfgate() {
        MockSessionContext sessionFacade = new MockSessionContext();
        sessionFacade.setCobrand("sfgate");

        PageHelper pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());

        assertFalse(pageHelper.isShowingBannerAd());
        assertTrue(pageHelper.isShowingLogo());
        assertTrue(pageHelper.isShowingUserInfo());
        assertFalse(pageHelper.isLogoLinked());
        assertTrue(pageHelper.isShowingFooter());
        assertFalse(pageHelper.isAdFree());
        assertFalse(pageHelper.isFramed());
    }

    public void testYahoo() {
        MockSessionContext sessionFacade = new MockSessionContext();
        sessionFacade.setCobrand("yahoo");

        PageHelper pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());

        assertFalse(pageHelper.isShowingBannerAd());
        assertTrue(pageHelper.isShowingLogo() || pageHelper.isShowingUserInfo());
        assertFalse(pageHelper.isLogoLinked());
        assertFalse(pageHelper.isShowingFooter());
        assertFalse(pageHelper.isAdFree());
        assertFalse(pageHelper.isFramed());
    }

    public void testFamily() {
        MockSessionContext sessionFacade = new MockSessionContext();
        sessionFacade.setCobrand("family");

        PageHelper pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());

        assertFalse(pageHelper.isShowingBannerAd());
        assertTrue(pageHelper.isShowingLogo() || pageHelper.isShowingUserInfo());
        assertFalse(pageHelper.isLogoLinked());
        assertTrue(pageHelper.isShowingFooter());
        assertFalse(pageHelper.isAdFree());
        assertFalse(pageHelper.isFramed());
        assertFalse(pageHelper.isShowingFooterAd());
    }

    public void testAzCentral() {
        MockSessionContext sessionFacade = new MockSessionContext();
        sessionFacade.setCobrand("azcentral");

        PageHelper pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());

        assertFalse(pageHelper.isShowingBannerAd());
        assertTrue(pageHelper.isShowingLogo() || pageHelper.isShowingUserInfo());
        assertFalse(pageHelper.isLogoLinked());
        assertTrue(pageHelper.isShowingFooter());
        assertFalse(pageHelper.isAdFree());
        assertFalse(pageHelper.isFramed());
    }


    public void testOnload() {
        SessionContext sessionContext = new MockSessionContext();
        PageHelper pageHelper = new PageHelper(sessionContext, new GsMockHttpServletRequest());

        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);

        assertTrue(pageHelper.isShowingBannerAd());
        assertTrue(pageHelper.isShowingLogo() || pageHelper.isShowingUserInfo());
        assertTrue(pageHelper.isShowingFooter());
        assertTrue(pageHelper.isLogoLinked());

        assertEquals("", pageHelper.getOnload());

        PageHelper.addOnLoadHandler(_request, "window.alert('Hi')");
        assertEquals("window.alert('Hi')", pageHelper.getOnload());
        PageHelper.addOnLoadHandler(_request, "window.alert('World')");
        assertEquals("window.alert('Hi');window.alert('World')", pageHelper.getOnload());

        try {
            PageHelper.addOnLoadHandler(_request, "don't allow \"quotes\"");
            fail("quotes were allowed to be inserted");
        } catch (IllegalArgumentException e) {
            // good, I didn't write code to handle that yet.
        }

        //add javascript that already exists in the onload
        PageHelper.addOnLoadHandler(_request, "window.alert('Hi')");
        PageHelper.addOnLoadHandler(_request, "window.alert('World')");
        PageHelper.addOnLoadHandler(_request, "window.alert('Hi')");
        PageHelper.addOnLoadHandler(_request, "window.alert('World')");
        assertEquals("window.alert('Hi');window.alert('World')", pageHelper.getOnload());
    }

    public void testOnunload() {
        SessionContext sessionContext = new MockSessionContext();
        PageHelper pageHelper = new PageHelper(sessionContext, new GsMockHttpServletRequest());

        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);

        assertEquals("", pageHelper.getOnunload());

        PageHelper.addOnunloadHandler(_request, "window.alert('Hi')");
        assertEquals("window.alert('Hi')", pageHelper.getOnunload());
        PageHelper.addOnunloadHandler(_request, "window.alert('World')");
        assertEquals("window.alert('Hi');window.alert('World')", pageHelper.getOnunload());

        try {
            PageHelper.addOnunloadHandler(_request, "don't allow \"quotes\"");
            fail("quotes were allowed to be inserted");
        } catch (IllegalArgumentException e) {
            // good, I didn't write code to handle that yet.
        }

        //add javascript that already exists in the onunload
        PageHelper.addOnunloadHandler(_request, "window.alert('Hi')");
        PageHelper.addOnunloadHandler(_request, "window.alert('World')");
        PageHelper.addOnunloadHandler(_request, "window.alert('Hi')");
        PageHelper.addOnunloadHandler(_request, "window.alert('World')");
        assertEquals("window.alert('Hi');window.alert('World')", pageHelper.getOnunload());
    }

    public void testJavascriptAndCssInclude() {
        SessionContext sessionContext = new MockSessionContext();
        PageHelper pageHelper = new PageHelper(sessionContext, new GsMockHttpServletRequest());
        Properties versionProperties = new Properties();
        versionProperties.setProperty("gsweb.version","8.3");
        pageHelper.setVersionProperties(versionProperties);

        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);

        assertEquals("", pageHelper.getHeadElements());

        PageHelper.addJavascriptSource(_request, "/res/js/something.js");
        assertEquals("<script type=\"text/javascript\" src=\"/res/js/something.js?v=8.3\"></script>", pageHelper.getHeadElements());

        //add a duplicate, should not get multiple
        PageHelper.addJavascriptSource(_request, "/res/js/something.js");
        assertEquals("<script type=\"text/javascript\" src=\"/res/js/something.js?v=8.3\"></script>", pageHelper.getHeadElements());


        PageHelper.addJavascriptSource(_request, "/res/js/somethingElse.js");
        assertEquals("<script type=\"text/javascript\" src=\"/res/js/something.js?v=8.3\">" +
                "</script><script type=\"text/javascript\" src=\"/res/js/somethingElse.js?v=8.3\"></script>",
                pageHelper.getHeadElements());

        PageHelper.addExternalCss(_request, "/res/css/special.css");
        assertEquals("<script type=\"text/javascript\" src=\"/res/js/something.js?v=8.3\">" +
                "</script><script type=\"text/javascript\" src=\"/res/js/somethingElse.js?v=8.3\"></script>" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"/res/css/special.css?v=8.3\"></link>",
                pageHelper.getHeadElements());

        //add a duplicate, should not get multiple
        PageHelper.addExternalCss(_request, "/res/css/special.css");
        assertEquals("<script type=\"text/javascript\" src=\"/res/js/something.js?v=8.3\">" +
                "</script><script type=\"text/javascript\" src=\"/res/js/somethingElse.js?v=8.3\"></script>" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"/res/css/special.css?v=8.3\"></link>",
                pageHelper.getHeadElements());

        //add a print css
        PageHelper.addExternalCss(_request, "/res/pRint-sifr.css");
        assertEquals("<script type=\"text/javascript\" src=\"/res/js/something.js?v=8.3\">" +
                "</script><script type=\"text/javascript\" src=\"/res/js/somethingElse.js?v=8.3\"></script>" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"/res/css/special.css?v=8.3\"></link>" +
                "<link rel=\"stylesheet\" type=\"text/css\" media=\"print\" href=\"/res/pRint-sifr.css?v=8.3\"></link>",
                pageHelper.getHeadElements());

        //add a screen css
        PageHelper.addExternalCss(_request, "/res/screen-sifr.css");
        assertEquals("<script type=\"text/javascript\" src=\"/res/js/something.js?v=8.3\">" +
                "</script><script type=\"text/javascript\" src=\"/res/js/somethingElse.js?v=8.3\"></script>" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"/res/css/special.css?v=8.3\"></link>" +
                "<link rel=\"stylesheet\" type=\"text/css\" media=\"print\" href=\"/res/pRint-sifr.css?v=8.3\"></link>" +
                "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"/res/screen-sifr.css?v=8.3\"></link>",
                pageHelper.getHeadElements());



        PageHelper.addExternalCss(_request, "");

    }

    public void testHideFooter() {

        SessionContext sessionContext = new MockSessionContext();

        PageHelper pageHelper = new PageHelper(sessionContext, new GsMockHttpServletRequest());

        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);

        PageHelper.hideFooter(_request);

        assertTrue(pageHelper.isShowingBannerAd());
        assertTrue(pageHelper.isShowingLogo() || pageHelper.isShowingUserInfo());
        assertFalse(pageHelper.isShowingFooter());
        assertTrue(pageHelper.isLogoLinked());
        assertFalse(pageHelper.isAdFree());
    }

    public void testHideHeader() {

        SessionContext sessionContext = new MockSessionContext();

        PageHelper pageHelper = new PageHelper(sessionContext, new GsMockHttpServletRequest());

        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);

        PageHelper.hideHeader(_request);

        assertTrue(pageHelper.isShowingBannerAd());
        assertFalse(pageHelper.isShowingLogo() || pageHelper.isShowingUserInfo());
        assertTrue(pageHelper.isShowingFooter());
        assertTrue(pageHelper.isLogoLinked());
        assertFalse(pageHelper.isAdFree());
    }


    public void testIsDevEnvironment() {

        MockSessionContext sessionFacade = new MockSessionContext();

        sessionFacade.setHostName("www.greatschools.net");
        PageHelper pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());
        assertFalse(pageHelper.isDevEnvironment());

        sessionFacade.setHostName("cobrand.greatschools.net");
        pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());
        assertFalse(pageHelper.isDevEnvironment());

        sessionFacade.setHostName("yahoo.greatschools.net");
        pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());
        assertFalse(pageHelper.isDevEnvironment());

        sessionFacade.setHostName("charterschoolratings.org");
        pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());
        assertFalse(pageHelper.isDevEnvironment());

        sessionFacade.setHostName("dev.greatschools.net");
        pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());
        assertTrue(pageHelper.isDevEnvironment());

        sessionFacade.setHostName("cobrand.dev.greatschools.net");
        pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());
        assertTrue(pageHelper.isDevEnvironment());

        sessionFacade.setHostName("charterschoolratings.dev.greatschools.net");
        pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());
        assertTrue(pageHelper.isDevEnvironment());

        sessionFacade.setHostName("staging.greatschools.net");
        pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());
        assertTrue(pageHelper.isDevEnvironment());

        sessionFacade.setHostName("apeterson.dev.greatschools.net");
        pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());
        assertTrue(pageHelper.isDevEnvironment());

        sessionFacade.setHostName("apeterson.office.greatschools.net");
        pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());
        assertTrue(pageHelper.isDevEnvironment());

        sessionFacade.setHostName("aroy.dev.greatschools.net");
        pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());
        assertTrue(pageHelper.isDevEnvironment());

        sessionFacade.setHostName("aroy.office.greatschools.net");
        pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());
        assertTrue(pageHelper.isDevEnvironment());

        sessionFacade.setHostName("localhost");
        pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());
        assertTrue(pageHelper.isDevEnvironment());

        sessionFacade.setHostName("127.0.0.1");
        pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());
        assertTrue(pageHelper.isDevEnvironment());

        sessionFacade.setHostName("staging.greatschools.net");
        pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());
        assertTrue(pageHelper.isStagingServer());

        sessionFacade.setHostName("dev.greatschools.net");
        pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());
        assertFalse(pageHelper.isStagingServer());

    }

    public void testAdvertising() {
        // Test for the main website
        MockSessionContext sessionFacade = new MockSessionContext();
        sessionFacade.setHostName("www.greatschools.net");
        PageHelper pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());
        assertFalse(pageHelper.isAdFree());
        // In the case of an ad server outage we turn advertising off
        sessionFacade.setAdvertisingOnline(false);
        pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());
        assertTrue(pageHelper.isAdFree());

        // Test for a cobrand that shows ads
        sessionFacade = new MockSessionContext();
        sessionFacade.setHostName("sfgate.greatschools.net");
        sessionFacade.setCobrand("sfgate");
        pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());
        assertFalse(pageHelper.isAdFree());
        // In the case of an ad server outage we turn advertising off
        sessionFacade.setAdvertisingOnline(false);
        pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());
        assertTrue(pageHelper.isAdFree());

        // Test for an ad free cobrand
        sessionFacade = new MockSessionContext();
        sessionFacade.setHostName("framed.greatschools.net");
        sessionFacade.setCobrand("framed");
        pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());
        assertTrue(pageHelper.isAdFree());
        // Turning advertising off should have no effect
        sessionFacade.setAdvertisingOnline(false);
        pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());
        assertTrue(pageHelper.isAdFree());
    }

    public void testSetMemberCookie() {
        User user = new User();
        String memberId = "100";
        user.setId(Integer.valueOf(memberId));
        PageHelper.setMemberCookie(_request, _response, user);

        Cookie cookie = _response.getCookie(SessionContextUtil.MEMBER_ID_COOKIE);
        assertNotNull(cookie);
        assertEquals(memberId, cookie.getValue());
        assertEquals(CookieGenerator.DEFAULT_COOKIE_MAX_AGE, cookie.getMaxAge());
        assertEquals(CookieGenerator.DEFAULT_COOKIE_PATH, cookie.getPath());
    }

    public void testSetMemberAuthorized() throws NoSuchAlgorithmException {
        User user = new User();
        user.setId(123);
        user.setEmail("testSetMemberCookie@greatschools.net");
        String hash = "rgTkMapq+oP5MTpxH3lEUQ==123";
        PageHelper.setMemberAuthorized(_request, _response, user);
        Cookie cookie = _response.getCookie("SESSION_CACHE");
        assertNotNull(cookie);
        ClientSideSessionCache sessionCache = ClientSideSessionCache.createClientSideSessionCache(cookie.getValue());
        assertEquals(hash, sessionCache.getUserHash());
        assertNotNull("Community cookie is not set!", _response.getCookie("community_dev"));
    }

    public void testIsMemberAuthorized() throws NoSuchAlgorithmException {
        assertFalse(PageHelper.isMemberAuthorized(_request));
        User user = new User();

        user.setId(123);
        user.setEmail("testSetMemberCookie@greatschools.net");
        PageHelper.setMemberAuthorized(_request, _response, user);

        // this step normally occurs automatically, but for this test must be done programmatically
        SessionContext sessionContext = SessionContextUtil.getSessionContext(_request);
        sessionContext.setUser(user);
        sessionContext.setHostName("dev.greatschools.net");

        _request.setCookies(new Cookie[] {_response.getCookie("community_dev")});
        assertTrue(PageHelper.isMemberAuthorized(_request));
    }

    public void testIsCommunityCookieSet() {
        SessionContext sessionContext = SessionContextUtil.getSessionContext(_request);
        sessionContext.setHostName("dev.greatschools.net");
        assertFalse(PageHelper.isCommunityCookieSet(_request));

        _request.setCookies(new Cookie[] {new Cookie("community_dev", "blahblah23")});
        assertTrue(PageHelper.isCommunityCookieSet(_request));        
    }

    public void testFindsBetaPage() {
        PageHelper helper = new PageHelper(new MockSessionContext(), _request);
        assertFalse(helper.isBetaPage());

        _request.setRequestURI("/community/beta/signup");
        helper = new PageHelper(new MockSessionContext(), _request);
        assertTrue(helper.isBetaPage());
    }

    public void testAdServedByCobrand() {
        MockSessionContext sessionContext = new MockSessionContext();
        PageHelper pageHelper = new PageHelper(sessionContext, new GsMockHttpServletRequest());

        assertEquals(false, pageHelper.isAdServedByCobrand());

        sessionContext.setCobrand("yahoo");
        pageHelper = new PageHelper(sessionContext, new GsMockHttpServletRequest());
        assertEquals(true, pageHelper.isAdServedByCobrand());

        sessionContext.setCobrand("yahooed");
        pageHelper = new PageHelper(sessionContext, new GsMockHttpServletRequest());
        assertEquals(true, pageHelper.isAdServedByCobrand());

        sessionContext.setCobrand("family");
        pageHelper = new PageHelper(sessionContext, new GsMockHttpServletRequest());
        assertEquals(true, pageHelper.isAdServedByCobrand());

        sessionContext.setCobrand("encarta");
        pageHelper = new PageHelper(sessionContext, new GsMockHttpServletRequest());
        assertEquals(true, pageHelper.isAdServedByCobrand());

        sessionContext.setCobrand("arkansasonline");
        pageHelper = new PageHelper(sessionContext, new GsMockHttpServletRequest());
        assertEquals(true, pageHelper.isAdServedByCobrand());
    }

    public void testSetPathwayCookie() {
        MockControl mockSessionContextUtil = MockClassControl.createControl(SessionContextUtil.class);

        SessionContextUtil sessionContextUtil = (SessionContextUtil) mockSessionContextUtil.getMock();
        _sessionContext.setSessionContextUtil(sessionContextUtil);

        sessionContextUtil.changePathway(_sessionContext, _response, (String) PageHelper.pageIds.get("SEASONAL"));
        mockSessionContextUtil.replay();

        PageHelper pageHelper = new PageHelper(_sessionContext, _request);
        pageHelper.setPathway(_request, _response, "SEASONAL");

        mockSessionContextUtil.verify();
        mockSessionContextUtil.reset();
    }

    public void testAdSlot() {
        MockSessionContext sessionContext = new MockSessionContext();
        PageHelper pageHelper = new PageHelper(sessionContext, new GsMockHttpServletRequest());
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);

        pageHelper = (PageHelper) _request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        assertEquals(0, pageHelper.getAdPositions().size());

        pageHelper.addAdPosition(AdPosition.X_11);
        assertEquals(1, pageHelper.getAdPositions().size());

        pageHelper.addAdPosition(AdPosition.X_20);
        assertEquals(2, pageHelper.getAdPositions().size());

        assertTrue(pageHelper.getAdPositions().contains(AdPosition.X_11));
        assertTrue(pageHelper.getAdPositions().contains(AdPosition.X_20));
    }

    public void testOasAdKeywords() {
        MockSessionContext sessionContext = new MockSessionContext();
        PageHelper pageHelper = new PageHelper(sessionContext, new GsMockHttpServletRequest());
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);
        assertEquals("no keywords set", "", pageHelper.getOASKeywords());

        sessionContext.setState(State.GA);
        pageHelper = new PageHelper(sessionContext, new GsMockHttpServletRequest());
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);
        assertEquals("state keyword set", "state=ga", pageHelper.getOASKeywords());

        pageHelper.addAdKeyword("hello","there");
        assertEquals("state keyword set", "hello=there&state=ga", pageHelper.getOASKeywords());

        pageHelper.addAdKeyword("county","Palm Beach");
        assertEquals("state keyword set", "county=Palm+Beach&hello=there&state=ga", pageHelper.getOASKeywords());
    }

    public void testAdKeywords() {
        MockSessionContext sessionContext = new MockSessionContext();
        PageHelper pageHelper = new PageHelper(sessionContext, new GsMockHttpServletRequest());
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);
        assertEquals(0, pageHelper.getAdKeywords().size());

        pageHelper.addAdKeyword("hello","there");
        pageHelper.addAdKeyword("type","private");
        pageHelper.addAdKeyword("type","private");
        assertEquals(2, pageHelper.getAdKeywords().size());

        assertTrue(pageHelper.getAdKeywords().containsKey("hello"));
        assertTrue(pageHelper.getAdKeywords().containsKey("type"));

        assertEquals("there", pageHelper.getAdKeywords().get("hello"));
        assertEquals("private", pageHelper.getAdKeywords().get("type"));
    }

    public void testIsAdminServer() {
        MockSessionContext sessionFacade = new MockSessionContext();

        sessionFacade.setHostName("admin.greatschools.net");
        PageHelper pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());
        assertTrue("Expected true for admin.greatschools.net", pageHelper.isAdminServer());

        sessionFacade.setHostName("maddy.greatschools.net");
        pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());
        assertTrue("Expected true for maddy.greatschools.net", pageHelper.isAdminServer());

        sessionFacade.setHostName("www.greatschools.net");
        pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());
        assertFalse("Expected false for www.greatschools.net", pageHelper.isAdminServer());
    }

    protected void setUp() throws Exception {
        super.setUp();

        _response = new MockHttpServletResponse();
        _request = new GsMockHttpServletRequest();

        SessionContextUtil sessionContextUtil = new SessionContextUtil();
        final CookieGenerator memberIdCookieGenerator = new CookieGenerator();
        memberIdCookieGenerator.setCookieName("MEMID");
        sessionContextUtil.setMemberIdCookieGenerator(memberIdCookieGenerator);

        final CookieGenerator sessionCacheCookieGenerator = new CookieGenerator();
        sessionCacheCookieGenerator.setCookieName("SESSION_CACHE");
        sessionContextUtil.setSessionCacheCookieGenerator(sessionCacheCookieGenerator);

        final CookieGenerator communityCookieGenerator = new CookieGenerator();
        communityCookieGenerator.setCookieName("community_dev");
        sessionContextUtil.setCommunityCookieGenerator(communityCookieGenerator);

        _sessionContext = new MockSessionContext();
        _sessionContext.setSessionContextUtil(sessionContextUtil);
        _request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, _sessionContext);

    }
}
