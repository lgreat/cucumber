package gs.web.request;

import gs.web.util.UrlUtil;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class HostnameInfoTest {

    @Test
    public void testHostnameInfo() {
        HostnameInfo hostnameInfo;

        hostnameInfo = new HostnameInfo("www.greatschools.org");

        assertEquals("www.greatschools.org", hostnameInfo.getHostname());
        assertEquals(UrlUtil.isDevEnvironment("www.greatschools.org"), hostnameInfo.isDevEnvironment());
        assertEquals(UrlUtil.cobrandFromUrl("www.greatschools.org") != null, hostnameInfo.isCobranded());
        assertFalse(hostnameInfo.isOnPkSubdomain());

        hostnameInfo = new HostnameInfo("pk.greatschools.org");

        assertEquals("pk.greatschools.org", hostnameInfo.getHostname());
        assertEquals(UrlUtil.isDevEnvironment("pk.greatschools.org"), hostnameInfo.isDevEnvironment());
        assertEquals(UrlUtil.cobrandFromUrl("pk.greatschools.org") != null, hostnameInfo.isCobranded());
        assertTrue(hostnameInfo.isOnPkSubdomain());

        hostnameInfo = new HostnameInfo("cobrand.greatschools.org");

        assertEquals("cobrand.greatschools.org", hostnameInfo.getHostname());
        assertEquals(UrlUtil.isDevEnvironment("cobrand.greatschools.org"), hostnameInfo.isDevEnvironment());
        assertEquals(UrlUtil.cobrandFromUrl("cobrand.greatschools.org") != null, hostnameInfo.isCobranded());
        assertFalse(hostnameInfo.isOnPkSubdomain());
    }

    @Test
    public void testGetBaseHostname() {
        HostnameInfo hostnameInfo;

        hostnameInfo = new HostnameInfo("www.greatschools.org");
        assertEquals("www.greatschools.org", hostnameInfo.getBaseHostname());
        
        hostnameInfo = new HostnameInfo("pk.greatschools.org");
        assertEquals("www.greatschools.org", hostnameInfo.getBaseHostname());

        hostnameInfo = new HostnameInfo("profile.dev");
        assertEquals("profile.dev", hostnameInfo.getBaseHostname());

        hostnameInfo = new HostnameInfo("pk.dev.greatschools.org");
        assertEquals("dev.greatschools.org", hostnameInfo.getBaseHostname());

        hostnameInfo = new HostnameInfo("dev.greatschools.org");
        assertEquals("dev.greatschools.org", hostnameInfo.getBaseHostname());

        hostnameInfo = new HostnameInfo("greatschools.babycenter.com");
        assertEquals("greatschools.babycenter.com", hostnameInfo.getBaseHostname());

        hostnameInfo = new HostnameInfo("localhost");
        assertEquals("localhost", hostnameInfo.getBaseHostname());
    }

    @Test
    public void testGetHostnameForPkSubdomain() {
        HostnameInfo hostnameInfo;

        hostnameInfo = new HostnameInfo("www.greatschools.org");
        assertEquals("pk.greatschools.org", hostnameInfo.getHostnameForPkSubdomain());

        hostnameInfo = new HostnameInfo("pk.greatschools.org");
        assertEquals("pk.greatschools.org", hostnameInfo.getHostnameForPkSubdomain());

        hostnameInfo = new HostnameInfo("profile.dev");
        assertEquals("pk.profile.dev", hostnameInfo.getHostnameForPkSubdomain());

        hostnameInfo = new HostnameInfo("pk.dev.greatschools.org");
        assertEquals("pk.dev.greatschools.org", hostnameInfo.getHostnameForPkSubdomain());

        hostnameInfo = new HostnameInfo("dev.greatschools.org");
        assertEquals("pk.dev.greatschools.org", hostnameInfo.getHostnameForPkSubdomain());

        hostnameInfo = new HostnameInfo("greatschools.babycenter.com");
        assertEquals("greatschools.babycenter.com", hostnameInfo.getHostnameForPkSubdomain());

        hostnameInfo = new HostnameInfo("localhost");
        assertEquals("localhost", hostnameInfo.getHostnameForPkSubdomain());

        hostnameInfo = new HostnameInfo("www.localhost.com");
        assertEquals("pk.localhost.com", hostnameInfo.getHostnameForPkSubdomain());
    }
}
