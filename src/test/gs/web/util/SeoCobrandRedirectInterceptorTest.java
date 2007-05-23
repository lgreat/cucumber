package gs.web.util;

import gs.web.BaseControllerTestCase;

/**
 * @author thuss
 */
public class SeoCobrandRedirectInterceptorTest extends BaseControllerTestCase {

    private SeoCobrandRedirectInterceptor _interceptor;

    protected void setUp() throws Exception {
        super.setUp();
        _interceptor = new SeoCobrandRedirectInterceptor();
        getRequest().setRequestURI("/content/allArticles.page");
        getRequest().setMethod("GET");
    }

    /**
     * There should never be a redirect for a normal user
     *
     * @throws Exception
     */
    public void testNoRedirect() throws Exception {
        assertTrue(_interceptor.preHandle(getRequest(), getResponse(), null));
        assertNull(getResponse().getRedirectedUrl());

        // Verify that even on a cobrand there is no redirect for a normal user
        _sessionContext.setCobrand("sfgate");
        assertTrue(_interceptor.preHandle(getRequest(), getResponse(), null));
        assertNull(getResponse().getRedirectedUrl());
    }

    /**
     * We 301 redirect all crawlers from our cobrands to our main website
     *
     * @throws Exception
     */
    public void testRedirectExpected() throws Exception {
        _sessionContext.setCrawler(true);
        _sessionContext.setCobrand("sfgate");
        getRequest().setQueryString("foo=1&bar=2");
        assertFalse(_interceptor.preHandle(getRequest(), getResponse(), null));
        assertEquals(301, getResponse().getStatus());
        assertEquals("http://www.greatschools.net/content/allArticles.page?foo=1&bar=2", getResponse().getRedirectedUrl());
    }

    /**
     * As part of our negotiations with Yahoo we agreed to let them crawl their own cobrand
     *
     * @throws Exception
     */
    public void testYahooSpecialCase() throws Exception {
        _sessionContext.setCrawler(true);
        _sessionContext.setCobrand("yahooed");
        getRequest().addHeader("User-Agent", "Mozilla/5.0 (compatible; Yahoo! Slurp; http://help.yahoo.com/help/us/ysearch/slurp)");
        assertTrue(_interceptor.preHandle(getRequest(), getResponse(), null));
        assertNull(getResponse().getRedirectedUrl());
    }

    /**
     * Since we use Apache to rewrite the SPP overview URL we check to make sure the redirect Java is issuing is correct
     *
     * For example http://sfgate.greatschools.net/modperl/browse_school/ca/13933 gets rewritten by Apache to
     * http://sfgate.greatschools.net/school/overview.page?state=ca&id=13933
     * but the redirect should go to:
     * http://www.greatschools.net/modperl/browse_school/ca/13933
     *
     * @throws Exception
     */
    public void testSppOverviewRedirectExpected() throws Exception {
        _sessionContext.setCrawler(true);
        _sessionContext.setCobrand("sfgate");
        getRequest().setRequestURI("/school/overview.page");
        getRequest().addParameter("state", "ca");
        getRequest().addParameter("id", "13933");
        assertFalse(_interceptor.preHandle(getRequest(), getResponse(), null));
        assertEquals(301, getResponse().getStatus());
        assertEquals("http://www.greatschools.net/modperl/browse_school/ca/13933", getResponse().getRedirectedUrl());
    }

    /**
     * Since we use apache to rewrite the R&C URL we check to make sure the redirect Java is issuing is correct
     *
     * @throws Exception
     */
    public void testResearchCompareRedirectExpected() throws Exception {
        _sessionContext.setCrawler(true);
        _sessionContext.setCobrand("sfgate");
        getRequest().setRequestURI("/school/research.page");
        getRequest().addParameter("state", "CA");
        assertFalse(_interceptor.preHandle(getRequest(), getResponse(), null));
        assertEquals(301, getResponse().getStatus());
        assertEquals("http://www.greatschools.net/modperl/go/CA", getResponse().getRedirectedUrl());
    }

}
