/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: PageHelperSaTest.java,v 1.4 2006/01/19 00:25:14 apeterson Exp $
 */

package gs.web.util;

import gs.web.ISessionFacade;
import gs.web.MockHttpServletRequest;
import gs.data.state.State;
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
    }


    public void testOnload() {
        ISessionFacade sessionFacade = new MockSessionFacade();
        PageHelper pageHelper = new PageHelper(sessionFacade);

        MockHttpServletRequest request = new MockHttpServletRequest();
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

    public void testHideFooter() {

        ISessionFacade sessionFacade = new MockSessionFacade();

        PageHelper pageHelper = new PageHelper(sessionFacade);

        MockHttpServletRequest request = new MockHttpServletRequest();
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

        MockHttpServletRequest request = new MockHttpServletRequest();
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


}
