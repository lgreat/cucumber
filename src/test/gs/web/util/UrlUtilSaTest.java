/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: UrlUtilSaTest.java,v 1.11 2005/12/16 23:15:37 dlee Exp $
 */

package gs.web.util;

import gs.data.state.State;
import gs.web.SessionContext;
import gs.web.SessionFacade;
import gs.web.SessionContextUtil;
import gs.web.MockHttpServletRequest;
import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests UrlUtil
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class UrlUtilSaTest extends TestCase {
    private UrlUtil _urlUtil;

    protected void setUp() throws Exception {
        super.setUp();

        _urlUtil = new UrlUtil();
    }

    public void testUrl() {
        SessionContext sessionFacade = new SessionContext();
        SessionContextUtil sessionUtil = new SessionContextUtil();
        sessionFacade.setState(State.CA);


        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setAttribute(SessionFacade.REQUEST_ATTRIBUTE_NAME, sessionFacade);
        request.setMethod("http");
        request.setServerName("localhost");
        request.setServerPort(8080);
        request.setRequestURI("/search/search.page");

        assertEquals("/search/search.page", _urlUtil.buildUrl("/search/search.page", request));
        assertEquals("http://dev.greatschools.net/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/CA", request));
        assertEquals("http://dev.greatschools.net/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/$STATE", request));

        request.setServerName("staging.greatschools.net");
        assertEquals("/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/CA", request));
        assertEquals("/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/$STATE", request));

        request.setServerName("www.greatschools.net");
        assertEquals("/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/CA", request));
        assertEquals("/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/$STATE", request));
        request.setAttribute("STATE", "PA"); // String not allowed-- must be a state object.
        assertEquals("/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/$STATE", request));
        request.setAttribute("STATE", State.PA);
        assertEquals("/modperl/bycity/PA", _urlUtil.buildUrl("/modperl/bycity/$STATE", request));
        assertEquals("/content/allArticles.page?state=PA", _urlUtil.buildUrl("/content/allArticles.page?state=$STATE", request));
        request.removeAttribute("STATE");

        // Test the babycenter cobrand since it's not a greatschools.net domain
        request.setServerName("greatschools.babycenter.com");
        assertEquals("/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/CA", request));
        assertEquals("/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/$STATE", request));
        assertEquals("/search/search.page", _urlUtil.buildUrl("/search/search.page", request));

        // Test the standard cobrand such as sfgate
        request.setServerName("sfgate.greatschools.net");
        assertEquals("/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/CA", request));
        assertEquals("/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/$STATE", request));
        assertEquals("/search/search.page", _urlUtil.buildUrl("/search/search.page", request));

        // Test having the page deployed under say gs-web
        String ctxPath = "/gs-web";
        request.setContextPath(ctxPath);
        assertEquals(ctxPath + "/search/search.page", _urlUtil.buildUrl("/search/search.page", request));
        // But Perl pages should stay unmodified by the context path
        assertEquals("/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/$STATE", request));
        assertEquals(ctxPath +"/res/css/global.css", _urlUtil.buildUrl("/res/css/global.css", request));
        assertEquals("/gs-web/content/allArticles.page?state=CA", _urlUtil.buildUrl("/content/allArticles.page?state=$STATE", request));
        request.setContextPath("/");

        // Test https server links
        request.setServerName("secure.greatschools.net");
        request.setRequestURI("/subscribe.page");
        request.setMethod("https");
        request.setScheme("https");
        sessionUtil.updateFromParams(request, response, sessionFacade);
        assertEquals("/subscribe.page", _urlUtil.buildUrl("/subscribe.page", request));
        assertEquals("http://www.greatschools.net/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/CA", request));
        assertEquals("http://www.greatschools.net/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/$STATE", request));

        // Test secure URL but coming from a cobrand
        request.setParameter("host", "sfgate.greatschools.net");
        sessionUtil.updateFromParams(request, response, sessionFacade);
        assertEquals("/thankyou.page", _urlUtil.buildUrl("/thankyou.page", request));
        assertEquals("http://sfgate.greatschools.net/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/CA", request));
        assertEquals("http://sfgate.greatschools.net/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/$STATE", request));
    }


    public void testCobrandDetection() {
        assertNull(_urlUtil.cobrandFromUrl("www.greatschools.net"));
        assertNull(_urlUtil.cobrandFromUrl("dev.greatschools.net"));
        assertNull(_urlUtil.cobrandFromUrl("secure.greatschools.net"));
        assertNull(_urlUtil.cobrandFromUrl("apeterson.dev.greatschools.net"));
        assertNull(_urlUtil.cobrandFromUrl("localhost"));
        assertNull(_urlUtil.cobrandFromUrl("maddy"));
        assertEquals("bob", _urlUtil.cobrandFromUrl("bob.greatschools.net"));
        assertEquals("az-central", _urlUtil.cobrandFromUrl("az-central.greatschools.net"));
        assertEquals("azcentral", _urlUtil.cobrandFromUrl("azcentral.dev.greatschools.net"));
        assertEquals("yahoo-ed", _urlUtil.cobrandFromUrl("yahoo-ed.greatschools.net"));
        assertEquals("babycenter", _urlUtil.cobrandFromUrl("greatschools.babycenter.com"));
        assertEquals("parentcenter", _urlUtil.cobrandFromUrl("greatschools.parentcenter.com"));

        // These don't work.
        // TODO Todd
//        assertNull(_urlUtil.cobrandFromUrl("greatschools.net"));
//        assertNull(_urlUtil.cobrandFromUrl("127.0.0.1"));
//        assertNull(_urlUtil.cobrandFromUrl("215.76.8.34"));
    }

    public void testBuildPerlHostname() {

        assertEquals("www.greatschools.net", _urlUtil.buildPerlHostName("www.greatschools.net", null));

        assertEquals("dev.greatschools.net", _urlUtil.buildPerlHostName("localhost", null));
        assertEquals("bob.dev.greatschools.net", _urlUtil.buildPerlHostName("localhost", "bob"));

        assertEquals("yahoo-ed.greatschools.net", _urlUtil.buildPerlHostName("www.greatschools.net", "yahoo-ed"));
        assertEquals("yahoo-ed.staging.greatschools.net", _urlUtil.buildPerlHostName("staging.greatschools.net", "yahoo-ed"));
        assertEquals("yahoo-ed.dev.greatschools.net", _urlUtil.buildPerlHostName("dev.greatschools.net", "yahoo-ed"));
        assertEquals("sfgate.dev.greatschools.net", _urlUtil.buildPerlHostName("dev.greatschools.net", "sfgate"));
        assertEquals("azcentral.www.greatschools.net", _urlUtil.buildPerlHostName("azcentral.www.greatschools.net", "azcentral"));
    }


    public void testSmellsLikePerlSite() {
        assertTrue(_urlUtil.smellsLikePerl("/modperl/bycity/CA"));
        assertTrue(_urlUtil.smellsLikePerl("/modperl/distlist/WA"));
        assertTrue(_urlUtil.smellsLikePerl("http://dev.greatschools.net/modperl/distlist/WA"));
        assertTrue(_urlUtil.smellsLikePerl("http://www.greatschools.net/modperl/distlist/WA"));
        assertTrue(_urlUtil.smellsLikePerl("http://dev.greatschools.net/modperl/browse_school/wa/34/"));
        assertTrue(_urlUtil.smellsLikePerl("http://www.greatschools.net/modperl/browse_school/wa/34/"));
        assertTrue(_urlUtil.smellsLikePerl("http://az-central.greatschools.net/modperl/browse_school/wa/34/"));
        assertTrue(_urlUtil.smellsLikePerl("/modperl/browse_school/wa/34/"));
        assertTrue(_urlUtil.smellsLikePerl("http://dev.greatschools.net/images/GS_logo.gif"));
        assertTrue(_urlUtil.smellsLikePerl("/images/GS_logo.gif"));
        assertTrue(_urlUtil.smellsLikePerl("/cgi-bin/site/signin.cgi/CA/"));

        assertFalse(_urlUtil.smellsLikePerl("/res/css/global.css"));
        assertFalse(_urlUtil.smellsLikePerl("/search/search.page"));
        assertFalse(_urlUtil.smellsLikePerl("http://localhost:8080/search/search.page"));
        assertFalse(_urlUtil.smellsLikePerl("http://www.greatschools.net/search/search.page?q=smell&x=0&y=0&state=wa"));
        assertFalse(_urlUtil.smellsLikePerl("http://localhost:8080/search/search.page?q=smell&x=0&y=0&state=wa"));
        assertFalse(_urlUtil.smellsLikePerl("http://localhost:8080/search/search.page?q=smell&x=0&y=0&state=wa"));
        assertFalse(_urlUtil.smellsLikePerl("http://localhost:8080/search/search.page?q=smell&x=0&y=0&state=wa"));

    }


    public void testIsDeveloperWorkstation() {
        assertFalse(_urlUtil.isDeveloperWorkstation("www.greatschools.net"));
        assertFalse(_urlUtil.isDeveloperWorkstation("dev.greatschools.net"));
        assertFalse(_urlUtil.isDeveloperWorkstation("staging.greatschools.net"));
        assertFalse(_urlUtil.isDeveloperWorkstation("apeterson.dev.greatschools.net"));
        assertTrue(_urlUtil.isDeveloperWorkstation("localhost"));
    }

    public void testBuildHref() {
        assertEquals("/search/search.page", _urlUtil.buildHref("/search/search.page", false, "http://localhost:8080/search/search.page", null));
        assertEquals("http://dev.greatschools.net/modperl/bycity/CA", _urlUtil.buildHref("/modperl/bycity/CA", false, "http://localhost:8080/search/search.page", null));
        assertEquals("/modperl/bycity/CA", _urlUtil.buildHref("/modperl/bycity/CA", false, "http://staging.greatschools.net/search/search.page", null));
        assertEquals("/modperl/bycity/CA", _urlUtil.buildHref("/modperl/bycity/CA", false, "http://www.greatschools.net/search/search.page", null));
        assertEquals("http://www.greatschools.net/modperl/bycity/CA", _urlUtil.buildHref("/modperl/bycity/CA", false, "https://www.greatschools.net/search/search.page", null));
        assertEquals("/modperl/bycity/CA", _urlUtil.buildHref("/modperl/bycity/CA", false, null, null));
        assertEquals("/something.page", _urlUtil.buildHref("/something.page", false, null, null));
    }


    public void testVpageToUrl() {
        assertEquals("/search/search.page", _urlUtil.vpageToUrl("/search/search.page"));
        assertEquals("/modperl/bycity/CA", _urlUtil.vpageToUrl("/modperl/bycity/CA"));
        assertEquals("/cgi-bin/site/january_parent_tips.cgi/$STATE", _urlUtil.vpageToUrl("vpage:content.seasonal"));
        assertEquals("/modperl/go/$STATE", _urlUtil.vpageToUrl("vpage:path1"));
        assertEquals("/path/mySchool.page?state=$STATE", _urlUtil.vpageToUrl("vpage:path2"));
    }
}
