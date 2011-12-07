package gs.web.request;

import gs.web.GsMockHttpServletRequest;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContext;
import net.sourceforge.wurfl.core.WURFLManager;
import org.junit.Test;
import org.junit.Before;

import javax.servlet.http.HttpServletRequest;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.createMock;

public class RequestInfoTest {

    private WURFLManager _wurflManager;

    @Before
    public void setUp() {
        _wurflManager = createMock(WURFLManager.class);
    }
    
    public HttpServletRequest getHttpServletRequestForHostname(String hostname) {
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        SessionContext sessionContext = new SessionContext();
        request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, sessionContext);
        request.setServerName(hostname);
        return request;
    }

    @Test
    public void testHostnameInfo() {
        RequestInfo requestInfo;

        requestInfo = new RequestInfo(getHttpServletRequestForHostname("www.greatschools.org"));

        assertEquals("www.greatschools.org", requestInfo.getHostname());
        assertEquals(UrlUtil.isDevEnvironment("www.greatschools.org"), requestInfo.isDevEnvironment());
        assertEquals(UrlUtil.cobrandFromUrl("www.greatschools.org") != null, requestInfo.isCobranded());
        assertFalse(requestInfo.isOnPkSubdomain());

        requestInfo = new RequestInfo(getHttpServletRequestForHostname("pk.greatschools.org"));

        assertEquals("pk.greatschools.org", requestInfo.getHostname());
        assertEquals(UrlUtil.isDevEnvironment("pk.greatschools.org"), requestInfo.isDevEnvironment());
        assertEquals(UrlUtil.cobrandFromUrl("pk.greatschools.org") != null, requestInfo.isCobranded());
        assertTrue(requestInfo.isOnPkSubdomain());

        requestInfo = new RequestInfo(getHttpServletRequestForHostname("cobrand.greatschools.org"));

        assertEquals("cobrand.greatschools.org", requestInfo.getHostname());
        assertEquals(UrlUtil.isDevEnvironment("cobrand.greatschools.org"), requestInfo.isDevEnvironment());
        assertEquals(UrlUtil.cobrandFromUrl("cobrand.greatschools.org") != null, requestInfo.isCobranded());
        assertFalse(requestInfo.isOnPkSubdomain());
    }

    @Test
    public void testGetBaseHostname() {
        RequestInfo hostnameInfo;

        hostnameInfo = new RequestInfo(getHttpServletRequestForHostname("www.greatschools.org"));
        assertEquals("www.greatschools.org", hostnameInfo.getBaseHostname());
        
        hostnameInfo = new RequestInfo(getHttpServletRequestForHostname("pk.greatschools.org"));
        assertEquals("www.greatschools.org", hostnameInfo.getBaseHostname());

        hostnameInfo = new RequestInfo(getHttpServletRequestForHostname("profile.dev"));
        assertEquals("profile.dev", hostnameInfo.getBaseHostname());

        hostnameInfo = new RequestInfo(getHttpServletRequestForHostname("pk.dev.greatschools.org"));
        assertEquals("dev.greatschools.org", hostnameInfo.getBaseHostname());

        hostnameInfo = new RequestInfo(getHttpServletRequestForHostname("dev.greatschools.org"));
        assertEquals("dev.greatschools.org", hostnameInfo.getBaseHostname());

        hostnameInfo = new RequestInfo(getHttpServletRequestForHostname("greatschools.babycenter.com"));
        assertEquals("greatschools.babycenter.com", hostnameInfo.getBaseHostname());

        hostnameInfo = new RequestInfo(getHttpServletRequestForHostname("localhost"));
        assertEquals("localhost", hostnameInfo.getBaseHostname());
    }

    @Test
    public void testGetHostnameForTargetSubdomain() {
        RequestInfo hostnameInfo;

        hostnameInfo = new RequestInfo(getHttpServletRequestForHostname("www.greatschools.org"));
        assertEquals("pk.greatschools.org", hostnameInfo.getHostnameForTargetSubdomain(Subdomain.PK));

        hostnameInfo = new RequestInfo(getHttpServletRequestForHostname("pk.greatschools.org"));
        assertEquals("pk.greatschools.org", hostnameInfo.getHostnameForTargetSubdomain(Subdomain.PK));

        hostnameInfo = new RequestInfo(getHttpServletRequestForHostname("profile.dev"));
        assertEquals("pk.profile.dev", hostnameInfo.getHostnameForTargetSubdomain(Subdomain.PK));

        hostnameInfo = new RequestInfo(getHttpServletRequestForHostname("pk.dev.greatschools.org"));
        assertEquals("pk.dev.greatschools.org", hostnameInfo.getHostnameForTargetSubdomain(Subdomain.PK));

        hostnameInfo = new RequestInfo(getHttpServletRequestForHostname("dev.greatschools.org"));
        assertEquals("pk.dev.greatschools.org", hostnameInfo.getHostnameForTargetSubdomain(Subdomain.PK));

        hostnameInfo = new RequestInfo(getHttpServletRequestForHostname("greatschools.babycenter.com"));
        assertEquals("greatschools.babycenter.com", hostnameInfo.getHostnameForTargetSubdomain(Subdomain.PK));

        hostnameInfo = new RequestInfo(getHttpServletRequestForHostname("localhost"));
        assertEquals("localhost", hostnameInfo.getHostnameForTargetSubdomain(Subdomain.PK));

        // www.localhost.com will be detected as developer workstation and so pk subdomain won't be applied.
        hostnameInfo = new RequestInfo(getHttpServletRequestForHostname("www.localhost.com"));
        assertEquals("www.localhost.com", hostnameInfo.getHostnameForTargetSubdomain(Subdomain.PK));

        hostnameInfo = new RequestInfo(getHttpServletRequestForHostname("dev.greatschools.org"));
        assertEquals("dev.greatschools.org", hostnameInfo.getHostnameForTargetSubdomain(null));

        hostnameInfo = new RequestInfo(getHttpServletRequestForHostname("www.greatschools.org"));
        assertEquals("www.greatschools.org", hostnameInfo.getHostnameForTargetSubdomain(null));

        hostnameInfo = new RequestInfo(getHttpServletRequestForHostname("greatschools.org"));
        assertEquals("greatschools.org", hostnameInfo.getHostnameForTargetSubdomain(null));

        hostnameInfo = new RequestInfo(getHttpServletRequestForHostname("localhost"));
        assertEquals("localhost", hostnameInfo.getHostnameForTargetSubdomain(null));

    }

    @Test
    public void testIsProductionHostname() {
        RequestInfo hostnameInfo;

        hostnameInfo = new RequestInfo(getHttpServletRequestForHostname("www.greatschools.org"));
        assertTrue(hostnameInfo.isProductionHostname());

        hostnameInfo = new RequestInfo(getHttpServletRequestForHostname("pk.greatschools.org"));
        assertTrue(hostnameInfo.isProductionHostname());

        hostnameInfo = new RequestInfo(getHttpServletRequestForHostname("profile.dev"));
        assertFalse(hostnameInfo.isProductionHostname());

        hostnameInfo = new RequestInfo(getHttpServletRequestForHostname("pk.dev.greatschools.org"));
        assertFalse(hostnameInfo.isProductionHostname());

        hostnameInfo = new RequestInfo(getHttpServletRequestForHostname("dev.greatschools.org"));
        assertFalse(hostnameInfo.isProductionHostname());

        hostnameInfo = new RequestInfo(getHttpServletRequestForHostname("greatschools.babycenter.com"));
        assertFalse(hostnameInfo.isProductionHostname());

        hostnameInfo = new RequestInfo(getHttpServletRequestForHostname("localhost"));
        assertFalse(hostnameInfo.isProductionHostname());

        hostnameInfo = new RequestInfo(getHttpServletRequestForHostname("www.localhost.com"));
        assertFalse(hostnameInfo.isProductionHostname());

        hostnameInfo = new RequestInfo(getHttpServletRequestForHostname("blah.greatschools.org"));
        assertFalse(hostnameInfo.isProductionHostname());
    }
}
