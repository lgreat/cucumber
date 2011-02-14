/*
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: UrlUtilSaTest.java,v 1.74 2011/02/14 16:11:57 aroy Exp $
 */

package gs.web.util;

import gs.data.state.State;
import gs.data.util.CdnUtil;
import gs.web.GsMockHttpServletRequest;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Map;

/**
 * Tests UrlUtil
 *
 * @author <a href="mailto:apeterson@greatschools.org">Andrew J. Peterson</a>
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
        assertEquals("http://dev.greatschools.org/cgi-bin/path/CA/1234", _urlUtil.buildUrl("/cgi-bin/path/$STATE/1234", request));
        assertEquals("http://dev.greatschools.org/cgi-bin/path/ca/1234", _urlUtil.buildUrl("/cgi-bin/path/$LCSTATE/1234", request));

        assertEquals("http://maps.google.com/maps?file=api", _urlUtil.buildUrl("http://maps.google.com/maps?file=api", request));
        assertEquals("/search/search.page", _urlUtil.buildUrl("/search/search.page", request));
        assertEquals("http://dev.greatschools.org/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/CA", request));
        assertEquals("http://dev.greatschools.org/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/$STATE", request));

        // Test perl resources
        assertEquals("http://dev.greatschools.org/js/global.js", _urlUtil.buildUrl("/js/global.js", request));
        assertEquals("http://dev.greatschools.org/css/gs.css", _urlUtil.buildUrl("/css/gs.css", request));

        request.setServerName("staging.greatschools.org");
        assertEquals("/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/CA", request));
        assertEquals("/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/$STATE", request));

        request.setServerName("www.greatschools.org");
        assertEquals("/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/CA", request));
        assertEquals("/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/$STATE", request));
        request.setAttribute("STATE", "PA"); // String not allowed-- must be a state object.
        assertEquals("/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/$STATE", request));
        request.setAttribute("STATE", State.PA);
        assertEquals("/modperl/bycity/PA", _urlUtil.buildUrl("/modperl/bycity/$STATE", request));
        assertEquals("/content/allArticles.page?state=PA", _urlUtil.buildUrl("/content/allArticles.page?state=$STATE", request));
        request.removeAttribute("STATE");

        // Test the babycenter cobrand since it's not a greatschools.org domain
        request.setServerName("greatschools.babycenter.com");
        assertEquals("/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/CA", request));
        assertEquals("/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/$STATE", request));
        assertEquals("/search/search.page", _urlUtil.buildUrl("/search/search.page", request));

        // Test the standard cobrand such as sfgate
        request.setServerName("sfgate.greatschools.org");
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
        request.setServerName("secure.greatschools.org");
        request.setRequestURI("/subscribe.page");
        request.setMethod("https");
        request.setScheme("https");
        sessionUtil.updateFromParams(request, response, sessionFacade);
        assertEquals("/subscribe.page", _urlUtil.buildUrl("/subscribe.page", request));
        assertEquals("http://www.greatschools.org/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/CA", request));
        assertEquals("http://www.greatschools.org/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/$STATE", request));
        assertEquals("/res/js/s_code.js", _urlUtil.buildUrl("/res/js/s_code.js", request));
        // This should be a secure request because otherwise it sets off internet explorer
        assertEquals("/modperl/promos/image//769/CA//3", _urlUtil.buildUrl("/modperl/promos/image//769/CA//3", request));

        // Test secure URL but coming from a cobrand
        request.setParameter("host", "sfgate.greatschools.org");
        sessionUtil.updateFromParams(request, response, sessionFacade);
        assertEquals("/thankyou.page", _urlUtil.buildUrl("/thankyou.page", request));
        assertEquals("http://sfgate.greatschools.org/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/CA", request));
        assertEquals("http://sfgate.greatschools.org/modperl/bycity/CA", _urlUtil.buildUrl("/modperl/bycity/$STATE", request));
        assertEquals("/modperl/promos/image//769/CA//3", _urlUtil.buildUrl("/modperl/promos/image//769/CA//3", request));

        //test fully qualified has spaces removed
        assertEquals("http://maps.google.com?address+,City,+CA", _urlUtil.buildUrl("http://maps.google.com?address   ,City, CA", request));
    }


    public void testCobrandDetection() {
        assertNull(_urlUtil.cobrandFromUrl("www.greatschools.org"));
        assertNull(_urlUtil.cobrandFromUrl("dev.greatschools.org"));
        assertNull(_urlUtil.cobrandFromUrl("secure.greatschools.org"));
        assertNull(_urlUtil.cobrandFromUrl("maddy.greatschools.org"));
        assertNull(_urlUtil.cobrandFromUrl("clone.greatschools.org"));
        assertNull(_urlUtil.cobrandFromUrl("localhost"));
        assertNull(_urlUtil.cobrandFromUrl("maddy"));
        assertNull(_urlUtil.cobrandFromUrl("admin.greatschools.org"));
        assertNull(_urlUtil.cobrandFromUrl("www.maddy.greatschools.org"));
        assertNull(_urlUtil.cobrandFromUrl("qa.greatschools.org"));
        assertEquals("qantas", _urlUtil.cobrandFromUrl("qantas.greatschools.org"));
        assertEquals("bob", _urlUtil.cobrandFromUrl("bob.greatschools.org"));
        assertEquals("az-central", _urlUtil.cobrandFromUrl("az-central.greatschools.org"));
        assertEquals("azcentral", _urlUtil.cobrandFromUrl("azcentral.dev.greatschools.org"));
        assertEquals("yahoo-ed", _urlUtil.cobrandFromUrl("yahoo-ed.greatschools.org"));
        assertEquals("babycenter", _urlUtil.cobrandFromUrl("greatschools.babycenter.com"));
        assertEquals("parentcenter", _urlUtil.cobrandFromUrl("greatschools.parentcenter.com"));

        assertEquals("yahoo", _urlUtil.cobrandFromUrl("yahoo.dev.greatschools.org"));
        assertEquals("yahooed", _urlUtil.cobrandFromUrl("yahooed.dev.greatschools.org"));
        assertEquals("azcentral", _urlUtil.cobrandFromUrl("azcentral.dev.greatschools.org/"));

        assertEquals("yahoo", _urlUtil.cobrandFromUrl("yahoo.staging.greatschools.org"));
        assertEquals("yahooed", _urlUtil.cobrandFromUrl("yahooed.staging.greatschools.org"));
        assertEquals("azcentral", _urlUtil.cobrandFromUrl("azcentral.staging.greatschools.org/"));

        assertEquals("fresno", _urlUtil.cobrandFromUrl("fresno.schools.net"));
        assertEquals("fresno", _urlUtil.cobrandFromUrl("www.fresno.schools.net"));

        assertEquals("charterschoolratings", _urlUtil.cobrandFromUrl("charterschoolratings.dev.greatschools.org/"));

        assertNull(_urlUtil.cobrandFromUrl("127.0.0.1"));
        assertNull(_urlUtil.cobrandFromUrl("aroy.office.greatschools.org"));
        assertNull(_urlUtil.cobrandFromUrl("aroy.dev.greatschools.org"));
        assertNull(_urlUtil.cobrandFromUrl("192.168.1.100"));
        assertNull(_urlUtil.cobrandFromUrl("172.21.1.142"));

        assertNull("Resource servers are not cobrands", _urlUtil.cobrandFromUrl("res1.greatschools.org"));
        assertNull("Resource servers are not cobrands", _urlUtil.cobrandFromUrl("res2.greatschools.org"));
        assertNull("Resource servers are not cobrands", _urlUtil.cobrandFromUrl("res3.greatschools.org"));
        assertNull("Resource servers are not cobrands", _urlUtil.cobrandFromUrl("res4.greatschools.org"));
        assertNull("Resource servers are not cobrands", _urlUtil.cobrandFromUrl("res5.greatschools.org"));
        assertNull("Resource servers are not cobrands", _urlUtil.cobrandFromUrl("res6.greatschools.org"));

        assertNull("App servers are not cobrands", _urlUtil.cobrandFromUrl("app1a.greatschools.org"));
        assertNull("App servers are not cobrands", _urlUtil.cobrandFromUrl("app1b.greatschools.org"));
        assertNull("App servers are not cobrands", _urlUtil.cobrandFromUrl("app1c.greatschools.org"));
        assertNull("App servers are not cobrands", _urlUtil.cobrandFromUrl("app1d.greatschools.org"));
        assertNull("App servers are not cobrands", _urlUtil.cobrandFromUrl("app2a.greatschools.org"));
        assertNull("App servers are not cobrands", _urlUtil.cobrandFromUrl("app2b.greatschools.org"));
        assertNull("App servers are not cobrands", _urlUtil.cobrandFromUrl("app2c.greatschools.org"));
        assertNull("App servers are not cobrands", _urlUtil.cobrandFromUrl("app2d.greatschools.org"));

        assertNull("editorial.dev should not be treated as a cobrand", _urlUtil.cobrandFromUrl("editorial.dev.greatschools.org"));

        // This doesn't work, but it's not a realistic case at this time.
        //assertNull(_urlUtil.cobrandFromUrl("greatschools.org"));
    }

    public void testBuildPerlHostname() {

        assertEquals("www.greatschools.org", _urlUtil.buildPerlHostName("www.greatschools.org", null));

        assertEquals("dev.greatschools.org", _urlUtil.buildPerlHostName("localhost", null));
        assertEquals("bob.dev.greatschools.org", _urlUtil.buildPerlHostName("localhost", "bob"));

        assertEquals("yahoo-ed.greatschools.org", _urlUtil.buildPerlHostName("www.greatschools.org", "yahoo-ed"));
        assertEquals("yahoo-ed.staging.greatschools.org", _urlUtil.buildPerlHostName("staging.greatschools.org", "yahoo-ed"));
        assertEquals("yahoo-ed.dev.greatschools.org", _urlUtil.buildPerlHostName("dev.greatschools.org", "yahoo-ed"));
        assertEquals("sfgate.dev.greatschools.org", _urlUtil.buildPerlHostName("dev.greatschools.org", "sfgate"));
        assertEquals("azcentral.www.greatschools.org", _urlUtil.buildPerlHostName("azcentral.www.greatschools.org", "azcentral"));
    }


    public void testSmellsLikePerlSite() {
        assertTrue(_urlUtil.smellsLikePerl("/modperl/bycity/CA"));
        assertTrue(_urlUtil.smellsLikePerl("/modperl/distlist/WA"));
        assertTrue(_urlUtil.smellsLikePerl("http://dev.greatschools.org/modperl/distlist/WA"));
        assertTrue(_urlUtil.smellsLikePerl("http://www.greatschools.org/modperl/distlist/WA"));
        assertTrue(_urlUtil.smellsLikePerl("http://dev.greatschools.org/modperl/browse_school/wa/34/"));
        assertTrue(_urlUtil.smellsLikePerl("http://www.greatschools.org/modperl/browse_school/wa/34/"));
        assertTrue(_urlUtil.smellsLikePerl("http://az-central.greatschools.org/modperl/browse_school/wa/34/"));
        assertTrue(_urlUtil.smellsLikePerl("/modperl/browse_school/wa/34/"));
        assertTrue(_urlUtil.smellsLikePerl("http://dev.greatschools.org/images/GS_logo.gif"));
        assertTrue(_urlUtil.smellsLikePerl("/images/GS_logo.gif"));
        assertTrue(_urlUtil.smellsLikePerl("/cgi-bin/site/signin.cgi/CA/"));

        assertFalse(_urlUtil.smellsLikePerl("/res/css/global.css"));
        assertFalse(_urlUtil.smellsLikePerl("/search/search.page"));
        assertFalse(_urlUtil.smellsLikePerl("http://localhost:8080/search/search.page"));
        assertFalse(_urlUtil.smellsLikePerl("http://www.greatschools.org/search/search.page?q=smell&x=0&y=0&state=wa"));
        assertFalse(_urlUtil.smellsLikePerl("http://localhost:8080/search/search.page?q=smell&x=0&y=0&state=wa"));
        assertFalse(_urlUtil.smellsLikePerl("http://localhost:8080/search/search.page?q=smell&x=0&y=0&state=wa"));
        assertFalse(_urlUtil.smellsLikePerl("http://localhost:8080/search/search.page?q=smell&x=0&y=0&state=wa"));

    }


    public void testIsDeveloperWorkstation() {
        assertFalse(UrlUtil.isDeveloperWorkstation("www.greatschools.org"));
        assertFalse(UrlUtil.isDeveloperWorkstation("maddy.greatschools.org"));
        assertFalse(UrlUtil.isDeveloperWorkstation("clone.greatschools.org"));
        assertFalse(UrlUtil.isDeveloperWorkstation("dev.greatschools.org"));
        assertFalse(UrlUtil.isDeveloperWorkstation("staging.greatschools.org"));
        assertFalse(UrlUtil.isDeveloperWorkstation("aroy.dev.greatschools.org"));
        assertTrue(UrlUtil.isDeveloperWorkstation("aroy.office.greatschools.org"));
        assertTrue(UrlUtil.isDeveloperWorkstation("somenewdeveloper.office.greatschools.org"));
        assertTrue(UrlUtil.isDeveloperWorkstation("macbook.greatschools.org"));
        assertTrue(UrlUtil.isDeveloperWorkstation("macbook"));
        assertFalse("Potential cobrand shouldn't be considered developer workstation",
                UrlUtil.isDeveloperWorkstation("office.greatschools.org"));
        assertTrue(UrlUtil.isDeveloperWorkstation("aroy.office"));
        assertTrue(UrlUtil.isDeveloperWorkstation("localhost"));
        assertTrue(UrlUtil.isDeveloperWorkstation("127.0.0.1"));
        assertTrue(UrlUtil.isDeveloperWorkstation("172.21.1.142"));
        assertTrue(UrlUtil.isDeveloperWorkstation("172.21.1.142:8080"));
        assertFalse("cpickslay.office should not be dev workstation", UrlUtil.isDeveloperWorkstation("cpickslay.office.greatschools.org"));
    }

    public void testIsDevEnvironment() {
        assertFalse(UrlUtil.isDevEnvironment("www.greatschools.org"));
        assertFalse(UrlUtil.isDevEnvironment("cobrand.greatschools.org"));
        assertFalse(UrlUtil.isDevEnvironment("maddy.greatschools.org"));
        assertFalse(UrlUtil.isDevEnvironment("yahoo.greatschools.org"));
        assertFalse(UrlUtil.isDevEnvironment("charterschoolratings.org"));

        assertTrue(UrlUtil.isDevEnvironment("dev.greatschools.org"));
        assertFalse(UrlUtil.isDevEnvironment("devwirefakecobrand.greatschools.org"));
        assertTrue(UrlUtil.isDevEnvironment("dev"));
        assertTrue(UrlUtil.isDevEnvironment("cobrand.dev.greatschools.org"));
        assertTrue(UrlUtil.isDevEnvironment("charterschoolratings.dev.greatschools.org"));
        assertTrue(UrlUtil.isDevEnvironment("staging.greatschools.org"));
        assertTrue(UrlUtil.isDevEnvironment("aroy.dev.greatschools.org"));
        assertTrue(UrlUtil.isDevEnvironment("aroy.office.greatschools.org"));
        assertTrue(UrlUtil.isDevEnvironment("aroy.office"));
        assertTrue(UrlUtil.isDevEnvironment("somedev.office.greatschools.org"));
        assertFalse(UrlUtil.isDevEnvironment("office.greatschools.org"));
        assertTrue(UrlUtil.isDevEnvironment("macbook.greatschools.org:8080"));
        assertTrue(UrlUtil.isDevEnvironment("macbook:8080"));
        assertTrue(UrlUtil.isDevEnvironment("clone.greatschools.org"));
        assertTrue(UrlUtil.isDevEnvironment("azcentral.clone.greatschools.org"));
        assertTrue(UrlUtil.isDevEnvironment("localhost"));
        assertTrue(UrlUtil.isDevEnvironment("127.0.0.1"));
        assertTrue(UrlUtil.isDevEnvironment("172.21.1.142"));
        assertTrue(UrlUtil.isDevEnvironment("172.21.1.142:8080"));

        assertTrue(UrlUtil.isDevEnvironment("gs-cms.carbonfive.com"));
        assertTrue(UrlUtil.isDevEnvironment("gs-preview.carbonfive.com"));
        assertTrue(UrlUtil.isDevEnvironment("gs-staging.carbonfive.com"));
        assertTrue(UrlUtil.isDevEnvironment("gs-live.carbonfive.com"));
    }

    public void testIsStagingServer() {
        assertTrue(UrlUtil.isStagingServer("staging.greatschools.org"));
        assertTrue(UrlUtil.isStagingServer("clone.greatschools.org"));
        assertFalse(UrlUtil.isStagingServer("dev.greatschools.org"));
    }

    public void testIsPrereleaseServer() {
        assertTrue(UrlUtil.isPreReleaseServer("rithmatic.greatschools.org"));
        assertTrue(UrlUtil.isPreReleaseServer("sfgate.rithmatic.greatschools.org"));
        assertFalse(UrlUtil.isPreReleaseServer("www.greatschools.org"));
        assertFalse(UrlUtil.isPreReleaseServer("staging.greatschools.org"));
    }

    public void testBuildHref() {
        assertEquals("/search/search.page", _urlUtil.buildHref(null, "/search/search.page", false, "http://localhost:8080/search/search.page", false));
        assertEquals("http://dev.greatschools.org/modperl/bycity/CA", _urlUtil.buildHref(null, "/modperl/bycity/CA", false, "http://localhost:8080/search/search.page", false));
        assertEquals("/modperl/bycity/CA", _urlUtil.buildHref(null, "/modperl/bycity/CA", false, "http://staging.greatschools.org/search/search.page", false));
        assertEquals("/modperl/bycity/CA", _urlUtil.buildHref(null, "/modperl/bycity/CA", false, "http://www.greatschools.org/search/search.page", false));
        assertEquals("http://www.greatschools.org/modperl/bycity/CA", _urlUtil.buildHref(null, "/modperl/bycity/CA", false, "https://www.greatschools.org/search/search.page", false));
        assertEquals("/modperl/bycity/CA", _urlUtil.buildHref(null, "/modperl/bycity/CA", false, null, false));
        assertEquals("/something.page", _urlUtil.buildHref(null, "/something.page", false, null, false));

        assertEquals("/something.page", _urlUtil.buildHref(null, "/something.page", false, "http://www.greatschools.org/search/search.page", true));
        System.setProperty(CdnUtil.CDN_PREFIX, "http://fake.cdn.akamai.com");
        assertEquals("http://fake.cdn.akamai.com/something.page", _urlUtil.buildHref(null, "/something.page", false, "http://www.greatschools.org/search/search.page", true));
        assertEquals("/something.page", _urlUtil.buildHref(null, "/something.page", false, "http://localhost:8080/search/search.page", true));
        assertEquals("/something.page", _urlUtil.buildHref(null, "/something.page", false, "http://localhost:8080/search/search.page", false));
    }

    public void testIsAdminServer() {
        assertTrue("Expected true for admin URL", UrlUtil.isAdminServer("admin.greatschools.org"));
        assertTrue("Expected true for maddy URL", UrlUtil.isAdminServer("admin.greatschools.org"));
        assertFalse("Expected false for production URL", UrlUtil.isAdminServer("www.greatschools.org"));
    }

    public void testIsQAServer() {
        assertTrue("Expected true for qa URL", UrlUtil.isQAServer("cmsqa1.greatschools.org"));
        assertTrue("Expected true for qa URL", UrlUtil.isQAServer("cmsqa2.greatschools.org"));
        assertTrue("Expected true for qa URL", UrlUtil.isQAServer("qa.greatschools.org"));
        assertFalse("Expected false for production URL", UrlUtil.isQAServer("www.greatschools.org"));
    }

    public void testIsCommunityContentLink() {
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.greatschools.org/advice/write"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.greatschools.org/groups/create"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http%3A%2F%2Fcommunity.dev.greatschools.org/q-and-a?submit=true"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http%3A%2F%2Fcommunity.dev.greatschools.org/q-and-a/?submit=true"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.greatschools.org/groups/2771/join"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.greatschools.org/q-and-a/12345/blah-blah?comment=5100_106604"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.greatschools.org/members/watchlist/watch?type=5000&id=106495"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.greatschools.org/recommend-content?id=106495&type=5000"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.greatschools.org/report/email-moderator"));

        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://comgen1.greatschools.org:8000/advice/write"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://comgen1.greatschools.org:8000/groups/create"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://comgen1.greatschools.org:8000/groups/2771/join"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://comgen1.greatschools.org:8000/q-and-a/12345/blah-blah?comment=5100_106604"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://comgen1.greatschools.org:8000/members/watchlist/watch?type=5000&id=106495"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://comgen1.greatschools.org:8000/recommend-content?id=106495&type=5000"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://comgen1.greatschools.org:8000/report/email-moderator"));

        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.dev.greatschools.org/advice/write"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.dev.greatschools.org/groups/create"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.dev.greatschools.org/groups/similar"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.dev.greatschools.org/groups/2771/join"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.dev.greatschools.org/q-and-a/12345/blah-blah?comment=5100_106604"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.dev.greatschools.org/members/watchlist/watch?type=5000&id=106495"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.dev.greatschools.org/recommend-content?id=106495&type=5000"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.dev.greatschools.org/report/email-moderator"));

        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.staging.greatschools.org/advice/write"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.staging.greatschools.org/groups/create"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.staging.greatschools.org/groups/2771/join"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.staging.greatschools.org/q-and-a/12345/blah-blah?comment=5100_106604"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.staging.greatschools.org/members/watchlist/watch?type=5000&id=106495"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.staging.greatschools.org/recommend-content?id=106495&type=5000"));
        assertTrue("Expected true for content-creation URL",
                UrlUtil.isCommunityContentLink("http://community.staging.greatschools.org/report/email-moderator"));

        assertFalse("Expected false for GS URL",
                UrlUtil.isCommunityContentLink("http://www.greatschools.org/advice/write"));
        assertFalse("Expected false for GS cobrand URL",
                UrlUtil.isCommunityContentLink("http://sfgate.greatschools.org/advice/write"));
        assertFalse("Expected false for GS cobrand URL",
                UrlUtil.isCommunityContentLink("http://sfgate.dev.greatschools.org/advice/write"));

        assertFalse("Expected false for non-content-creation URL on community",
                UrlUtil.isCommunityContentLink("http://community.greatschools.org/advice"));
        assertFalse("Expected false for non-content-creation URL on community",
                UrlUtil.isCommunityContentLink("http://community.greatschools.org/"));
        assertFalse("Expected false for non-content-creation URL on community",
                UrlUtil.isCommunityContentLink("http://community.greatschools.org/q-and-a/"));

        assertTrue("Expected true for community discussion", UrlUtil.isCommunityContentLink("http://localhost/aroy-local-board-2/community.gs?content=21"));
        assertTrue("Expected true for community discussion", UrlUtil.isCommunityContentLink("/aroy-local-board-2/community.gs?content=21"));
        assertTrue("Expected true for community discussion", UrlUtil.isCommunityContentLink("http://localhost/aroy-local-board-2/community/discussion.gs?content=22"));
        assertTrue("Expected true for community discussion", UrlUtil.isCommunityContentLink("/aroy-local-board-2/community/discussion.gs?content=22"));
    }

    public void testAddParameterWithoutPreexistingParameters(){

        String testUrl = "community.greatschools.org";
        String testParam = "param=9";
        String testResult = UrlUtil.addParameter(testUrl, testParam);
        String expectedResult = testUrl + "?" + testParam;
        assertEquals("Expect the parameter to be appended with a ? delimiter", expectedResult, testResult);


    }
    public void testAddParameterWithPreexistingParameters(){

        String testUrl = "community.greatschools.org?id=22";
        String testParam = "param=9";
        String testResult = UrlUtil.addParameter(testUrl, testParam);
        String expectedResult = testUrl + "&" + testParam;
        assertEquals("Expect the parameter to be appended with a & delimiter", expectedResult, testResult);
    }

    public void testAddParameterWithAnchor(){
        String testParam = "foo=bar";
        
        assertEquals("Expect the parameter to be appended before the anchor name",
                     "/path?foo=bar#anchor", UrlUtil.addParameter("/path#anchor", testParam));

        assertEquals("Expect the parameter to be appended before the anchor name but after other parameters",
                     "/path?a=b&foo=bar#anchor", UrlUtil.addParameter("/path?a=b#anchor", testParam));
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

    public void testGetParamsFromQueryString() {
        Map<String, String> params;

        params = UrlUtil.getParamsFromQueryString(null);
        assertNotNull(params);
        assertEquals(0, params.size());

        params = UrlUtil.getParamsFromQueryString("");
        assertNotNull(params);
        assertEquals(0, params.size());

        params = UrlUtil.getParamsFromQueryString("foo=bar");
        assertNotNull(params);
        assertEquals(1, params.size());
        assertEquals("bar", params.get("foo"));

        params = UrlUtil.getParamsFromQueryString("a=b&c=d&e=f");
        assertNotNull(params);
        assertEquals(3, params.size());
        assertEquals("b", params.get("a"));
        assertEquals("d", params.get("c"));
        assertEquals("f", params.get("e"));

        params = UrlUtil.getParamsFromQueryString("state=CA&city=San+Francisco");
        assertNotNull(params);
        assertEquals(2, params.size());
        assertEquals("CA", params.get("state"));
        assertEquals("Expect URL decoding to occur", "San Francisco", params.get("city"));
    }

    public void testGetApiHostname() {
        assertEquals("api.dev.greatschools.org", UrlUtil.getApiHostname("localhost"));
        assertEquals("api.dev.greatschools.org", UrlUtil.getApiHostname("dev.greatschools.org"));
        assertEquals("api.dev.greatschools.org", UrlUtil.getApiHostname("sfgate.dev.greatschools.org"));
        assertEquals("api.staging.greatschools.org", UrlUtil.getApiHostname("staging.greatschools.org"));
        assertEquals("api.staging.greatschools.org", UrlUtil.getApiHostname("sfgate.staging.greatschools.org"));
        assertEquals("api.clone.greatschools.org", UrlUtil.getApiHostname("clone.greatschools.org"));
        assertEquals("api.clone.greatschools.org", UrlUtil.getApiHostname("sfgate.clone.greatschools.org"));
        assertEquals("api.greatschools.org", UrlUtil.getApiHostname("www.greatschools.org"));
        assertEquals("api.greatschools.org", UrlUtil.getApiHostname("sfgate.greatschools.org"));
    }
}
