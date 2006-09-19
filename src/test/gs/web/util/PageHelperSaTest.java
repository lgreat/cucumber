/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: PageHelperSaTest.java,v 1.16 2006/09/19 23:31:08 dlee Exp $
 */

package gs.web.util;

import gs.data.community.User;
import gs.web.GsMockHttpServletRequest;
import gs.web.util.context.ISessionContext;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.Cookie;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class PageHelperSaTest extends TestCase {
    private MockHttpServletResponse _response;
    private GsMockHttpServletRequest _request;

    public void testMainSite() {
        ISessionContext sessionContext = new MockSessionContext();

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
        ISessionContext sessionContext = new MockSessionContext();
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


    public void testJavascriptAndCssInclude() {
        ISessionContext sessionContext = new MockSessionContext();
        PageHelper pageHelper = new PageHelper(sessionContext, new GsMockHttpServletRequest());

        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);

        assertEquals("", pageHelper.getHeadElements());

        PageHelper.addJavascriptSource(_request, "/res/js/something.js");
        assertEquals("<script type=\"text/javascript\" src=\"/res/js/something.js\"></script>", pageHelper.getHeadElements());

        PageHelper.addJavascriptSource(_request, "/res/js/somethingElse.js");
        assertEquals("<script type=\"text/javascript\" src=\"/res/js/something.js\">" +
                "</script><script type=\"text/javascript\" src=\"/res/js/somethingElse.js\"></script>",
                pageHelper.getHeadElements());

        PageHelper.addExternalCss(_request, "/res/css/special.css");
        assertEquals("<script type=\"text/javascript\" src=\"/res/js/something.js\">" +
                "</script><script type=\"text/javascript\" src=\"/res/js/somethingElse.js\"></script>" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"/res/css/special.css\"></link>",
                pageHelper.getHeadElements());
    }

    public void testHideFooter() {

        ISessionContext sessionContext = new MockSessionContext();

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

        ISessionContext sessionContext = new MockSessionContext();

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

        sessionFacade.setHostName("localhost");
        pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());
        assertTrue(pageHelper.isDevEnvironment());

        sessionFacade.setHostName("127.0.0.1");
        pageHelper = new PageHelper(sessionFacade, new GsMockHttpServletRequest());
        assertTrue(pageHelper.isDevEnvironment());
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

    public void testFindsBetaPage() {
        PageHelper helper = new PageHelper(new MockSessionContext(), _request);
        assertFalse(helper.isBetaPage());

        _request.setRequestURI("/community/beta/signup");
        helper = new PageHelper(new MockSessionContext(), _request);
        assertTrue(helper.isBetaPage());
    }

    public void testAdServerControlledFooterAd() {
        ISessionContext sessionContext = new MockSessionContext();
        PageHelper pageHelper = new PageHelper(sessionContext, new GsMockHttpServletRequest());

        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);
        assertEquals(false, pageHelper.isAdServerControlledFooterAd());

        PageHelper.setAdServerControlledFooterAd(_request, true);
        assertEquals(true, pageHelper.isAdServerControlledFooterAd());
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
    }

    protected void setUp() throws Exception {
        super.setUp();

        _response = new MockHttpServletResponse();
        _request = new GsMockHttpServletRequest();

        SessionContextUtil sessionContextUtil = new SessionContextUtil();
        final CookieGenerator memberIdCookieGenerator = new CookieGenerator();
        memberIdCookieGenerator.setCookieName("MEMID");
        sessionContextUtil.setMemberIdCookieGenerator(memberIdCookieGenerator);

        SessionContext sessionContext = new SessionContext();
        sessionContext.setSessionContextUtil(sessionContextUtil);
        _request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, sessionContext);

    }
}
