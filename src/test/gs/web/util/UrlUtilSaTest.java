/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: UrlUtilSaTest.java,v 1.3 2005/10/25 21:49:52 thuss Exp $
 */

package gs.web.util;

import gs.data.state.State;
import gs.web.SessionContext;
import gs.web.SessionFacade;
import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Tests UrlUtil
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class UrlUtilSaTest extends TestCase {
    public void testUrl() {
        SessionContext sessionFacade = new SessionContext();
        sessionFacade.setState(State.CA);

        UrlUtil urlUtil = new UrlUtil();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(SessionFacade.REQUEST_ATTRIBUTE_NAME, sessionFacade);
        request.setMethod("http");
        request.setServerName("localhost");
        request.setServerPort(8080);
        request.setRequestURI("/search/search.page");

        assertEquals("/search/search.page", urlUtil.buildUrl("/search/search.page", request));
        assertEquals("http://dev.greatschools.net/modperl/bycity/CA", urlUtil.buildUrl("/modperl/bycity/CA", request));
        assertEquals("http://dev.greatschools.net/modperl/bycity/CA", urlUtil.buildUrl("/modperl/bycity/$STATE", request));

        request.setServerName("staging.greatschools.net");
        assertEquals("/modperl/bycity/CA", urlUtil.buildUrl("/modperl/bycity/CA", request));
        assertEquals("/modperl/bycity/CA", urlUtil.buildUrl("/modperl/bycity/$STATE", request));

        request.setServerName("www.greatschools.net");
        assertEquals("/modperl/bycity/CA", urlUtil.buildUrl("/modperl/bycity/CA", request));
        assertEquals("/modperl/bycity/CA", urlUtil.buildUrl("/modperl/bycity/$STATE", request));
        request.setAttribute("STATE", "PA"); // String not allowed-- must be a state object.
        assertEquals("/modperl/bycity/CA", urlUtil.buildUrl("/modperl/bycity/$STATE", request));
        request.setAttribute("STATE", State.PA);
        assertEquals("/modperl/bycity/PA", urlUtil.buildUrl("/modperl/bycity/$STATE", request));
        request.removeAttribute("STATE");

        // Test the babycenter cobrand since it's not a greatschools.net domain
        request.setServerName("greatschools.babycenter.com");
        assertEquals("/modperl/bycity/CA", urlUtil.buildUrl("/modperl/bycity/CA", request));
        assertEquals("/modperl/bycity/CA", urlUtil.buildUrl("/modperl/bycity/$STATE", request));
        assertEquals("/search/search.page", urlUtil.buildUrl("/search/search.page", request));

        // Test the standard cobrand such as sfgate
        request.setServerName("sfgate.greatschools.net");
        assertEquals("/modperl/bycity/CA", urlUtil.buildUrl("/modperl/bycity/CA", request));
        assertEquals("/modperl/bycity/CA", urlUtil.buildUrl("/modperl/bycity/$STATE", request));
        assertEquals("/search/search.page", urlUtil.buildUrl("/search/search.page", request));

        // Test having the page deployed under say gs-web
        String ctxPath = "/gs-web";
        request.setContextPath(ctxPath);
        assertEquals(ctxPath + "/search/search.page", urlUtil.buildUrl("/search/search.page", request));
        // But Perl pages should stay unmodified by the context path
        assertEquals("/modperl/bycity/CA", urlUtil.buildUrl("/modperl/bycity/$STATE", request));
        request.setContextPath("/");

        request.setServerName("www.greatschools.net");
        request.setMethod("https");
        request.setScheme("https");
        assertEquals("http://www.greatschools.net/modperl/bycity/CA", urlUtil.buildUrl("/modperl/bycity/CA", request));
        assertEquals("http://www.greatschools.net/modperl/bycity/CA", urlUtil.buildUrl("/modperl/bycity/$STATE", request));

    }
}
