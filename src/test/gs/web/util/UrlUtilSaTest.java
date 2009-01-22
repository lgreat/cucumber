/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: UrlUtilSaTest.java,v 1.57 2009/01/22 19:05:37 aroy Exp $
 */

package gs.web.util;

import gs.data.state.State;
import gs.web.GsMockHttpServletRequest;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
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

    public void testBuildUrl() {
        SessionContext sessionFacade = new SessionContext();
        SessionContextUtil sessionUtil = new SessionContextUtil();
        sessionFacade.setState(State.CA);


        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, sessionFacade);
        request.setMethod("http");
        request.setServerName("localhost");
        request.setServerPort(8080);
        request.setRequestURI("/search/search.page");

        //test that $_STATE is replaced with lowercased state abbrevs.
        assertEquals("http://dev.greatschools.net/cgi-bin/path/CA/1234", _urlUtil.buildUrl("/cgi-bin/path/$STATE/1234", request));
        assertEquals("http://dev.greatschools.net/cgi-bin/path/ca/1234", _urlUtil.buildUrl("/cgi-bin/path/$LCSTATE/1234", request));

        assertEquals("http://maps.google.com/maps?file=api", _urlUtil.buildUrl("http://maps.google.com/maps?file=api", request));
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
        assertEquals(ctxPath + "/res/css/global.css", _urlUtil.buildUrl("/res/css/global.css", request));
        assertEquals(ctxPath + "/res/js/s_code.js", _urlUtil.buildUrl("/res/js/s_code.js", request));
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
        assertEquals("/res/js/s_code.js", _urlUtil.buildUrl("/res/js/s_code.js", request));
        // This should be a secure request because otherwise it sets off internet explorer
        assertEquals("/modperl/promos/image//769/CA//3", _urlUtil.buildUrl("/modperl/promos/image//769/CA//3", request));

        // Test secure URL but coming from a cobrand
        request.setParameter("host", "sfgate.greatschools.net");
        sessionUtil.updateFromParams(request, response, sessionFacade);
        assertEquals("/thankyou.page", _urlUtil.buildUrl("/thankyou.page", request));
        assertEquals("http://sfgate.greatschools.net/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/CA", request));
        assertEquals("http://sfgate.greatschools.net/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/$STATE", request));
        assertEquals("/modperl/promos/image//769/CA//3", _urlUtil.buildUrl("/modperl/promos/image//769/CA//3", request));

        //test fully qualified has spaces removed
        assertEquals("http://maps.google.com?address+,City,+CA", _urlUtil.buildUrl("http://maps.google.com?address   ,City, CA", request));
    }


    public void testCobrandDetection() {
        assertNull(_urlUtil.cobrandFromUrl("www.greatschools.net"));
        assertNull(_urlUtil.cobrandFromUrl("dev.greatschools.net"));
        assertNull(_urlUtil.cobrandFromUrl("secure.greatschools.net"));
        assertNull(_urlUtil.cobrandFromUrl("maddy.greatschools.net"));
        assertNull(_urlUtil.cobrandFromUrl("clone.greatschools.net"));
        assertNull(_urlUtil.cobrandFromUrl("localhost"));
        assertNull(_urlUtil.cobrandFromUrl("maddy"));
        assertEquals("bob", _urlUtil.cobrandFromUrl("bob.greatschools.net"));
        assertEquals("az-central", _urlUtil.cobrandFromUrl("az-central.greatschools.net"));
        assertEquals("azcentral", _urlUtil.cobrandFromUrl("azcentral.dev.greatschools.net"));
        assertEquals("yahoo-ed", _urlUtil.cobrandFromUrl("yahoo-ed.greatschools.net"));
        assertEquals("babycenter", _urlUtil.cobrandFromUrl("greatschools.babycenter.com"));
        assertEquals("parentcenter", _urlUtil.cobrandFromUrl("greatschools.parentcenter.com"));

        assertEquals("yahoo", _urlUtil.cobrandFromUrl("yahoo.dev.greatschools.net"));
        assertEquals("yahooed", _urlUtil.cobrandFromUrl("yahooed.dev.greatschools.net"));
        assertEquals("azcentral", _urlUtil.cobrandFromUrl("azcentral.dev.greatschools.net/"));

        assertEquals("yahoo", _urlUtil.cobrandFromUrl("yahoo.staging.greatschools.net"));
        assertEquals("yahooed", _urlUtil.cobrandFromUrl("yahooed.staging.greatschools.net"));
        assertEquals("azcentral", _urlUtil.cobrandFromUrl("azcentral.staging.greatschools.net/"));

        assertEquals("charterschoolratings", _urlUtil.cobrandFromUrl("charterschoolratings.dev.greatschools.net/"));

        assertNull(_urlUtil.cobrandFromUrl("127.0.0.1"));
        assertNull(_urlUtil.cobrandFromUrl("aroy.office.greatschools.net"));
        assertNull(_urlUtil.cobrandFromUrl("aroy.dev.greatschools.net"));
        assertNull(_urlUtil.cobrandFromUrl("192.168.1.100"));
        assertNull(_urlUtil.cobrandFromUrl("172.21.1.142"));

        assertNull("editorial.dev should not be treated as a cobrand", _urlUtil.cobrandFromUrl("editorial.dev.greatschools.net"));

        // This doesn't work, but it's not a realistic case at this time.
        //assertNull(_urlUtil.cobrandFromUrl("greatschools.net"));
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
        assertFalse(UrlUtil.isDeveloperWorkstation("www.greatschools.net"));
        assertFalse(UrlUtil.isDeveloperWorkstation("maddy.greatschools.net"));
        assertFalse(UrlUtil.isDeveloperWorkstation("clone.greatschools.net"));
        assertFalse(UrlUtil.isDeveloperWorkstation("dev.greatschools.net"));
        assertFalse(UrlUtil.isDeveloperWorkstation("staging.greatschools.net"));
        assertFalse(UrlUtil.isDeveloperWorkstation("aroy.dev.greatschools.net"));
        assertTrue(UrlUtil.isDeveloperWorkstation("aroy.office.greatschools.net"));
        assertTrue(UrlUtil.isDeveloperWorkstation("somenewdeveloper.office.greatschools.net"));
        assertTrue(UrlUtil.isDeveloperWorkstation("macbook.greatschools.net"));
        assertTrue(UrlUtil.isDeveloperWorkstation("macbook"));
        assertFalse("Potential cobrand shouldn't be considered developer workstation",
                UrlUtil.isDeveloperWorkstation("office.greatschools.net"));
        assertTrue(UrlUtil.isDeveloperWorkstation("aroy.office"));
        assertTrue(UrlUtil.isDeveloperWorkstation("localhost"));
        assertTrue(UrlUtil.isDeveloperWorkstation("127.0.0.1"));
        assertTrue(UrlUtil.isDeveloperWorkstation("172.21.1.142"));
        assertTrue(UrlUtil.isDeveloperWorkstation("172.21.1.142:8080"));
        assertFalse("cpickslay.office should not be dev workstation", UrlUtil.isDeveloperWorkstation("cpickslay.office.greatschools.net"));
    }

    public void testIsDevEnvironment() {
        assertFalse(UrlUtil.isDevEnvironment("www.greatschools.net"));
        assertFalse(UrlUtil.isDevEnvironment("cobrand.greatschools.net"));
        assertFalse(UrlUtil.isDevEnvironment("maddy.greatschools.net"));
        assertFalse(UrlUtil.isDevEnvironment("yahoo.greatschools.net"));
        assertFalse(UrlUtil.isDevEnvironment("charterschoolratings.org"));

        assertTrue(UrlUtil.isDevEnvironment("dev.greatschools.net"));
        assertFalse(UrlUtil.isDevEnvironment("devwirefakecobrand.greatschools.net"));
        assertTrue(UrlUtil.isDevEnvironment("dev"));
        assertTrue(UrlUtil.isDevEnvironment("cobrand.dev.greatschools.net"));
        assertTrue(UrlUtil.isDevEnvironment("charterschoolratings.dev.greatschools.net"));
        assertTrue(UrlUtil.isDevEnvironment("staging.greatschools.net"));
        assertTrue(UrlUtil.isDevEnvironment("aroy.dev.greatschools.net"));
        assertTrue(UrlUtil.isDevEnvironment("aroy.office.greatschools.net"));
        assertTrue(UrlUtil.isDevEnvironment("aroy.office"));
        assertTrue(UrlUtil.isDevEnvironment("somedev.office.greatschools.net"));
        assertFalse(UrlUtil.isDevEnvironment("office.greatschools.net"));
        assertTrue(UrlUtil.isDevEnvironment("macbook.greatschools.net:8080"));
        assertTrue(UrlUtil.isDevEnvironment("macbook:8080"));
        assertTrue(UrlUtil.isDevEnvironment("clone.greatschools.net"));
        assertTrue(UrlUtil.isDevEnvironment("azcentral.clone.greatschools.net"));
        assertTrue(UrlUtil.isDevEnvironment("localhost"));
        assertTrue(UrlUtil.isDevEnvironment("127.0.0.1"));
        assertTrue(UrlUtil.isDevEnvironment("172.21.1.142"));
        assertTrue(UrlUtil.isDevEnvironment("172.21.1.142:8080"));
    }

    public void testIsStagingServer() {
        assertTrue(UrlUtil.isStagingServer("staging.greatschools.net"));
        assertTrue(UrlUtil.isStagingServer("clone.greatschools.net"));
        assertFalse(UrlUtil.isStagingServer("dev.greatschools.net"));
    }

    public void testIsPrereleaseServer() {
        assertTrue(UrlUtil.isPreReleaseServer("rithmatic.greatschools.net"));
        assertTrue(UrlUtil.isPreReleaseServer("sfgate.rithmatic.greatschools.net"));
        assertFalse(UrlUtil.isPreReleaseServer("www.greatschools.net"));
        assertFalse(UrlUtil.isPreReleaseServer("staging.greatschools.net"));
    }

    public void testBuildHref() {
        assertEquals("/search/search.page", _urlUtil.buildHref(null, "/search/search.page", false, "http://localhost:8080/search/search.page"));
        assertEquals("http://dev.greatschools.net/modperl/bycity/CA", _urlUtil.buildHref(null, "/modperl/bycity/CA", false, "http://localhost:8080/search/search.page"));
        assertEquals("/modperl/bycity/CA", _urlUtil.buildHref(null, "/modperl/bycity/CA", false, "http://staging.greatschools.net/search/search.page"));
        assertEquals("/modperl/bycity/CA", _urlUtil.buildHref(null, "/modperl/bycity/CA", false, "http://www.greatschools.net/search/search.page"));
        assertEquals("http://www.greatschools.net/modperl/bycity/CA", _urlUtil.buildHref(null, "/modperl/bycity/CA", false, "https://www.greatschools.net/search/search.page"));
        assertEquals("/modperl/bycity/CA", _urlUtil.buildHref(null, "/modperl/bycity/CA", false, null));
        assertEquals("/something.page", _urlUtil.buildHref(null, "/something.page", false, null));
    }

    public void testIsAdminServer() {
        assertTrue("Expected true for admin URL", UrlUtil.isAdminServer("admin.greatschools.net"));
        assertTrue("Expected true for maddy URL", UrlUtil.isAdminServer("admin.greatschools.net"));
        assertFalse("Expected false for production URL", UrlUtil.isAdminServer("www.greatschools.net"));
    }

    public void testIsCommunityContentLink() {
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.greatschools.net/advice/write"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.greatschools.net/groups/create"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http%3A%2F%2Fcommunity.dev.greatschools.net/q-and-a?submit=true"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http%3A%2F%2Fcommunity.dev.greatschools.net/q-and-a/?submit=true"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.greatschools.net/groups/2771/join"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.greatschools.net/q-and-a/12345/blah-blah?comment=5100_106604"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.greatschools.net/members/watchlist/watch?type=5000&id=106495"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.greatschools.net/recommend-content?id=106495&type=5000"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.greatschools.net/report/email-moderator"));

        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://comgen1.greatschools.net:8000/advice/write"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://comgen1.greatschools.net:8000/groups/create"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://comgen1.greatschools.net:8000/groups/2771/join"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://comgen1.greatschools.net:8000/q-and-a/12345/blah-blah?comment=5100_106604"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://comgen1.greatschools.net:8000/members/watchlist/watch?type=5000&id=106495"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://comgen1.greatschools.net:8000/recommend-content?id=106495&type=5000"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://comgen1.greatschools.net:8000/report/email-moderator"));

        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.dev.greatschools.net/advice/write"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.dev.greatschools.net/groups/create"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.dev.greatschools.net/groups/similar"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.dev.greatschools.net/groups/2771/join"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.dev.greatschools.net/q-and-a/12345/blah-blah?comment=5100_106604"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.dev.greatschools.net/members/watchlist/watch?type=5000&id=106495"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.dev.greatschools.net/recommend-content?id=106495&type=5000"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.dev.greatschools.net/report/email-moderator"));

        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.staging.greatschools.net/advice/write"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.staging.greatschools.net/groups/create"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.staging.greatschools.net/groups/2771/join"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.staging.greatschools.net/q-and-a/12345/blah-blah?comment=5100_106604"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.staging.greatschools.net/members/watchlist/watch?type=5000&id=106495"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.staging.greatschools.net/recommend-content?id=106495&type=5000"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.staging.greatschools.net/report/email-moderator"));

        assertFalse("Expected false for GS URL",
                UrlUtil.isCommunityContentLink("http://www.greatschools.net/advice/write"));
        assertFalse("Expected false for GS cobrand URL",
                UrlUtil.isCommunityContentLink("http://sfgate.greatschools.net/advice/write"));
        assertFalse("Expected false for GS cobrand URL",
                UrlUtil.isCommunityContentLink("http://sfgate.dev.greatschools.net/advice/write"));

        assertFalse("Expected false for non-content-creation URL on community",
                UrlUtil.isCommunityContentLink("http://community.greatschools.net/advice"));
        assertFalse("Expected false for non-content-creation URL on community",
                UrlUtil.isCommunityContentLink("http://community.greatschools.net/"));
        assertFalse("Expected false for non-content-creation URL on community",
                UrlUtil.isCommunityContentLink("http://community.greatschools.net/q-and-a/"));

        assertTrue("Expected true for MSL forward link", UrlUtil.isCommunityContentLink("http://dev.greatschools.net/mySchoolList.page"));
        assertTrue("Expected true for MSL forward link", UrlUtil.isCommunityContentLink("/mySchoolList.page"));
    }

    public void testAddParameterWithoutPreexistingParameters(){

        String testUrl = "community.greatschools.net";
        String testParam = "param=9";
        String testResult = UrlUtil.addParameter(testUrl, testParam);
        String expectedResult = testUrl + "?" + testParam;
        assertEquals("Expect the parameter to be appended with a ? delimiter", expectedResult, testResult);


    }
    public void testAddParameterWithPreexistingParameters(){

        String testUrl = "community.greatschools.net?id=22";
        String testParam = "param=9";
        String testResult = UrlUtil.addParameter(testUrl, testParam);
        String expectedResult = testUrl + "&" + testParam;
        assertEquals("Expect the parameter to be appended with a & delimiter", expectedResult, testResult);
    }

    public void testBuildArticleUrl() {
        try {
            UrlUtil.buildArticleUrl(null);
            fail("Expected IllegalArgumentException with null argument.");
        } catch (IllegalArgumentException iae) {
            assertTrue(true);
        }

        assertEquals("Expected valid article URL", "/cgi-bin/showarticle/5", UrlUtil.buildArticleUrl(5));
    }
}
