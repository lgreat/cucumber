/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: PageHelperSaTest.java,v 1.8 2006/05/23 23:14:16 dlee Exp $
 */

package gs.web.util;

import gs.data.state.State;
import gs.web.GsMockHttpServletRequest;
import gs.web.ISessionFacade;
import junit.framework.TestCase;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class PageHelperSaTest extends TestCase {
    public void testMainSite() {
        ISessionFacade sessionFacade = new MockSessionFacade();

        PageHelper pageHelper = new PageHelper(sessionFacade);

        assertTrue(pageHelper.isShowingBannerAd());
        assertTrue(pageHelper.isShowingLogo());
        assertTrue(pageHelper.isShowingUserInfo());
        assertTrue(pageHelper.isShowingFooter());
        assertTrue(pageHelper.isLogoLinked());
        assertFalse(pageHelper.isAdFree());
    }

    public void testFramed() {
        MockSessionFacade sessionFacade = new MockSessionFacade();
        sessionFacade.setCobrand("framed");

        PageHelper pageHelper = new PageHelper(sessionFacade);

        assertFalse(pageHelper.isShowingBannerAd());
        assertFalse(pageHelper.isShowingLogo());
        assertFalse(pageHelper.isShowingUserInfo());
        assertFalse(pageHelper.isLogoLinked());
        assertFalse(pageHelper.isShowingFooter());
        assertTrue(pageHelper.isAdFree());
    }

    public void testCSR() {
        MockSessionFacade sessionFacade = new MockSessionFacade();
        sessionFacade.setCobrand("charterschoolratings");
        PageHelper pageHelper = new PageHelper(sessionFacade);

        assertTrue(pageHelper.isShowingBannerAd());
        assertTrue(pageHelper.isShowingFooter());
        assertFalse(pageHelper.isAdFree());
        assertFalse(pageHelper.isFramed());
    }

    public void testNumber1expert() {
        MockSessionFacade sessionFacade = new MockSessionFacade();
        sessionFacade.setCobrand("number1expert");

        PageHelper pageHelper = new PageHelper(sessionFacade);

        assertFalse(pageHelper.isShowingBannerAd());
        assertFalse(pageHelper.isShowingLogo());
        assertFalse(pageHelper.isShowingUserInfo());
        assertFalse(pageHelper.isLogoLinked());
        assertFalse(pageHelper.isShowingFooter());
        assertTrue(pageHelper.isAdFree());
        assertTrue(pageHelper.isFramed());
    }

    public void testSfgate() {
        MockSessionFacade sessionFacade = new MockSessionFacade();
        sessionFacade.setCobrand("sfgate");

        PageHelper pageHelper = new PageHelper(sessionFacade);

        assertFalse(pageHelper.isShowingBannerAd());
        assertTrue(pageHelper.isShowingLogo());
        assertTrue(pageHelper.isShowingUserInfo());
        assertFalse(pageHelper.isLogoLinked());
        assertTrue(pageHelper.isShowingFooter());
        assertFalse(pageHelper.isAdFree());
        assertFalse(pageHelper.isFramed());
    }

    public void testYahoo() {
        MockSessionFacade sessionFacade = new MockSessionFacade();
        sessionFacade.setCobrand("yahoo");

        PageHelper pageHelper = new PageHelper(sessionFacade);

        assertFalse(pageHelper.isShowingBannerAd());
        assertTrue(pageHelper.isShowingLogo() || pageHelper.isShowingUserInfo());
        assertFalse(pageHelper.isLogoLinked());
        assertFalse(pageHelper.isShowingFooter());
        assertFalse(pageHelper.isAdFree());
        assertFalse(pageHelper.isFramed());
    }

    public void testFamily() {
        MockSessionFacade sessionFacade = new MockSessionFacade();
        sessionFacade.setCobrand("family");

        PageHelper pageHelper = new PageHelper(sessionFacade);

        assertFalse(pageHelper.isShowingBannerAd());
        assertTrue(pageHelper.isShowingLogo() || pageHelper.isShowingUserInfo());
        assertFalse(pageHelper.isLogoLinked());
        assertTrue(pageHelper.isShowingFooter());
        assertFalse(pageHelper.isAdFree());
        assertFalse(pageHelper.isFramed());
        assertFalse(pageHelper.isShowingFooterAd());
    }

    public void testAzCentral() {
        MockSessionFacade sessionFacade = new MockSessionFacade();
        sessionFacade.setCobrand("azcentral");

        PageHelper pageHelper = new PageHelper(sessionFacade);

        assertFalse(pageHelper.isShowingBannerAd());
        assertTrue(pageHelper.isShowingLogo() || pageHelper.isShowingUserInfo());
        assertFalse(pageHelper.isLogoLinked());
        assertTrue(pageHelper.isShowingFooter());
        assertFalse(pageHelper.isAdFree());
        assertFalse(pageHelper.isFramed());
    }


    public void testOnload() {
        ISessionFacade sessionFacade = new MockSessionFacade();
        PageHelper pageHelper = new PageHelper(sessionFacade);

        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);

        assertTrue(pageHelper.isShowingBannerAd());
        assertTrue(pageHelper.isShowingLogo() || pageHelper.isShowingUserInfo());
        assertTrue(pageHelper.isShowingFooter());
        assertTrue(pageHelper.isLogoLinked());

        assertEquals("", pageHelper.getOnload());

        PageHelper.addOnLoadHandler(request, "window.alert('Hi')");
        assertEquals("window.alert('Hi')", pageHelper.getOnload());
        PageHelper.addOnLoadHandler(request, "window.alert('World')");
        assertEquals("window.alert('Hi');window.alert('World')", pageHelper.getOnload());

        try {
            PageHelper.addOnLoadHandler(request, "don't allow \"quotes\"");
            fail("quotes were allowed to be inserted");
        } catch (IllegalArgumentException e) {
            // good, I didn't write code to handle that yet.
        }

    }



    public void testJavascriptAndCssInclude() {
        ISessionFacade sessionFacade = new MockSessionFacade();
        PageHelper pageHelper = new PageHelper(sessionFacade);

        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);

        assertEquals("", pageHelper.getHeadElements());

        PageHelper.addJavascriptSource(request, "/res/js/something.js");
        assertEquals("<script type=\"text/javascript\" src=\"/res/js/something.js\"></script>", pageHelper.getHeadElements());

        PageHelper.addJavascriptSource(request, "/res/js/somethingElse.js");
        assertEquals("<script type=\"text/javascript\" src=\"/res/js/something.js\">"+
                "</script><script type=\"text/javascript\" src=\"/res/js/somethingElse.js\"></script>",
                pageHelper.getHeadElements());

        PageHelper.addExternalCss(request, "/res/css/special.css");
        assertEquals("<script type=\"text/javascript\" src=\"/res/js/something.js\">" +
                "</script><script type=\"text/javascript\" src=\"/res/js/somethingElse.js\"></script>" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"/res/css/special.css\"></link>",
                pageHelper.getHeadElements());
    }

    public void testHideFooter() {

        ISessionFacade sessionFacade = new MockSessionFacade();

        PageHelper pageHelper = new PageHelper(sessionFacade);

        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);

        PageHelper.hideFooter(request);

        assertTrue(pageHelper.isShowingBannerAd());
        assertTrue(pageHelper.isShowingLogo() || pageHelper.isShowingUserInfo());
        assertFalse(pageHelper.isShowingFooter());
        assertTrue(pageHelper.isLogoLinked());
        assertFalse(pageHelper.isAdFree());
    }

    public void testHideHeader() {

        ISessionFacade sessionFacade = new MockSessionFacade();

        PageHelper pageHelper = new PageHelper(sessionFacade);

        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);

        PageHelper.hideHeader(request);

        assertTrue(pageHelper.isShowingBannerAd());
        assertFalse(pageHelper.isShowingLogo() || pageHelper.isShowingUserInfo());
        assertTrue(pageHelper.isShowingFooter());
        assertTrue(pageHelper.isLogoLinked());
        assertFalse(pageHelper.isAdFree());
    }

    public void testSubscriptionState() {

        MockSessionFacade sessionFacade = new MockSessionFacade();
        sessionFacade.setState(State.CT);
        PageHelper pageHelper = new PageHelper(sessionFacade);

        assertTrue(pageHelper.isShowingBannerAd());
        assertTrue(pageHelper.isShowingLogo() || pageHelper.isShowingUserInfo());
        assertTrue(pageHelper.isShowingFooter());
        assertTrue(pageHelper.isLogoLinked());
        assertFalse(pageHelper.isSignInAvailable());

        sessionFacade.setState(State.TX);
        pageHelper = new PageHelper(sessionFacade);

        assertTrue(pageHelper.isShowingBannerAd());
        assertTrue(pageHelper.isShowingLogo() || pageHelper.isShowingUserInfo());
        assertTrue(pageHelper.isShowingFooter());
        assertTrue(pageHelper.isLogoLinked());
        assertTrue(pageHelper.isSignInAvailable());
        assertFalse(pageHelper.isAdFree());
    }

    public void testIsDevEnvironment() {

        MockSessionFacade sessionFacade = new MockSessionFacade();

        sessionFacade.setHostName("www.greatschools.net");
        PageHelper pageHelper = new PageHelper(sessionFacade);
        assertFalse(pageHelper.isDevEnvironment());

        sessionFacade.setHostName("cobrand.greatschools.net");
        pageHelper = new PageHelper(sessionFacade);
        assertFalse(pageHelper.isDevEnvironment());

        sessionFacade.setHostName("yahoo.greatschools.net");
        pageHelper = new PageHelper(sessionFacade);
        assertFalse(pageHelper.isDevEnvironment());

        sessionFacade.setHostName("charterschoolratings.org");
        pageHelper = new PageHelper(sessionFacade);
        assertFalse(pageHelper.isDevEnvironment());

        sessionFacade.setHostName("dev.greatschools.net");
        pageHelper = new PageHelper(sessionFacade);
        assertTrue(pageHelper.isDevEnvironment());

        sessionFacade.setHostName("cobrand.dev.greatschools.net");
        pageHelper = new PageHelper(sessionFacade);
        assertTrue(pageHelper.isDevEnvironment());

        sessionFacade.setHostName("charterschoolratings.dev.greatschools.net");
        pageHelper = new PageHelper(sessionFacade);
        assertTrue(pageHelper.isDevEnvironment());

        sessionFacade.setHostName("staging.greatschools.net");
        pageHelper = new PageHelper(sessionFacade);
        assertTrue(pageHelper.isDevEnvironment());

        sessionFacade.setHostName("apeterson.dev.greatschools.net");
        pageHelper = new PageHelper(sessionFacade);
        assertTrue(pageHelper.isDevEnvironment());

        sessionFacade.setHostName("apeterson.office.greatschools.net");
        pageHelper = new PageHelper(sessionFacade);
        assertTrue(pageHelper.isDevEnvironment());

        sessionFacade.setHostName("localhost");
        pageHelper = new PageHelper(sessionFacade);
        assertTrue(pageHelper.isDevEnvironment());

        sessionFacade.setHostName("127.0.0.1");
        pageHelper = new PageHelper(sessionFacade);
        assertTrue(pageHelper.isDevEnvironment());
    }

    public void testAdvertising() {
        // Test for the main website
        MockSessionFacade sessionFacade = new MockSessionFacade();
        sessionFacade.setHostName("www.greatschools.net");
        PageHelper pageHelper = new PageHelper(sessionFacade);
        assertFalse(pageHelper.isAdFree());
        // In the case of an ad server outage we turn advertising off
        sessionFacade.setAdvertisingOnline(false);
        pageHelper = new PageHelper(sessionFacade);
        assertTrue(pageHelper.isAdFree());

        // Test for a cobrand that shows ads
        sessionFacade = new MockSessionFacade();
        sessionFacade.setHostName("sfgate.greatschools.net");
        sessionFacade.setCobrand("sfgate");
        pageHelper = new PageHelper(sessionFacade);
        assertFalse(pageHelper.isAdFree());
        // In the case of an ad server outage we turn advertising off
        sessionFacade.setAdvertisingOnline(false);
        pageHelper = new PageHelper(sessionFacade);
        assertTrue(pageHelper.isAdFree());

        // Test for an ad free cobrand
        sessionFacade = new MockSessionFacade();
        sessionFacade.setHostName("framed.greatschools.net");
        sessionFacade.setCobrand("framed");
        pageHelper = new PageHelper(sessionFacade);
        assertTrue(pageHelper.isAdFree());
        // Turning advertising off should have no effect
        sessionFacade.setAdvertisingOnline(false);
        pageHelper = new PageHelper(sessionFacade);
        assertTrue(pageHelper.isAdFree());
    }

}
