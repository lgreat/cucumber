package gs.web.util.context;

import gs.web.GsMockHttpServletRequest;
import gs.web.BaseTestCase;
import gs.data.community.User;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.Cookie;

/**
 * Provides testing for the SessionContextUtil class. Note this class was created for revision
 * 1.14 of SessionContextUtil and is not comprehensive ... I'm only testing certain methods that I've
 * modified / created in revision 1.14.
 *
 * @TODO Expand test cases to all methods in SessionContextUtil
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class SessionContextUtilSaTest extends BaseTestCase {
    private GsMockHttpServletRequest _request;
    private MockHttpServletResponse _response;
    private SessionContextUtil _sessionContextUtil;
    private SessionContext _sessionContext;

    protected void setUp() throws Exception {
        super.setUp();
        _request = new GsMockHttpServletRequest();
        _response = new MockHttpServletResponse();

        _sessionContextUtil = new SessionContextUtil();

        _sessionContext = new SessionContext();
        _request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, _sessionContext);

        CookieGenerator sccGen = new CookieGenerator();
        sccGen.setCookieName("SESSION_CACHE");
        _sessionContextUtil.setSessionCacheCookieGenerator(sccGen);
        _sessionContextUtil.setCommunityCookieGenerator(new CookieGenerator());
    }

    private void setServerName(String serverName) {
        _request.setServerName(serverName);
        _sessionContextUtil.updateFromParams(_request, _response, _sessionContext);
    }

    public void testGetServerName() {
        // dev environment
        setServerName("dev.greatschools.net");
        assertEquals("dev", _sessionContextUtil.getServerName(_request));

        setServerName("aroy.office.greatschools.net");
        assertEquals("dev", _sessionContextUtil.getServerName(_request));

        setServerName("localhost:8080");
        assertEquals("dev", _sessionContextUtil.getServerName(_request));

        // staging environment
        setServerName("staging.greatschools.net");
        assertEquals("staging", _sessionContextUtil.getServerName(_request));

        setServerName("sfgate.staging.greatschools.net");
        assertEquals("staging", _sessionContextUtil.getServerName(_request));

        // live environment
        setServerName("www.greatschools.net");
        assertEquals("www", _sessionContextUtil.getServerName(_request));

        setServerName("sfgate.greatschools.net");
        assertEquals("www", _sessionContextUtil.getServerName(_request));
    }

    public void testChangeAuthorizationRememberMe() {
        User user = new User();
        user.setId(1);
        user.setEmail("email@example.com");
        String hash = "hash";
        setServerName("dev.greatschools.net");
        _sessionContextUtil.changeAuthorization(_request, _response, user, hash, true);

        Cookie cookie = _response.getCookie("community_dev");
        assertNotNull("Cookie should exist under name community_dev", cookie);
        assertEquals(".greatschools.net", cookie.getDomain());
        assertEquals(SessionContextUtil.COMMUNITY_COOKIE_MAX_AGE, cookie.getMaxAge());
    }

    public void testChangeAuthorizationForgetMe() {
        User user = new User();
        user.setId(1);
        user.setEmail("email@example.com");
        String hash = "hash";
        setServerName("dev.greatschools.net");
        _sessionContextUtil.changeAuthorization(_request, _response, user, hash, false);

        Cookie cookie = _response.getCookie("community_dev");
        assertNotNull("Cookie should exist under name community_dev", cookie);
        assertEquals(".greatschools.net", cookie.getDomain());
        assertEquals(-1, cookie.getMaxAge());
    }
}
