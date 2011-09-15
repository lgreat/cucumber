package gs.web.api.admin;

import gs.web.request.HostnameInfo;
import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;

/**
 * Created by chriskimm@greatschools.org
 */
public class AuthInterceptorTest {

    private AuthInterceptor _authInterceptor;

    @Before
    public void setup() {
        _authInterceptor = new AuthInterceptor();
    }

    @Test
    public void testPreHandle() throws Exception {
        HostnameInfo hostnameInfo = new HostnameInfo("");
        
        // no auth required
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(HostnameInfo.REQUEST_ATTRIBUTE_NAME, hostnameInfo);
        request.setMethod("get");
        request.setRequestURI("/api");
        HttpServletResponse response = new MockHttpServletResponse();
        assertTrue(_authInterceptor.preHandle(request, response, "nothing"));

        // auth required - no cookie
        request = new MockHttpServletRequest();
        request.setAttribute(HostnameInfo.REQUEST_ATTRIBUTE_NAME, hostnameInfo);
        request.setMethod("get");
        request.setRequestURI("/api/admin/foo");
        response = new MockHttpServletResponse();
        assertFalse(_authInterceptor.preHandle(request, response, "nothing"));

        // auth required - valid cookie
        request = new MockHttpServletRequest();
        request.setAttribute(HostnameInfo.REQUEST_ATTRIBUTE_NAME, hostnameInfo);
        request.setMethod("get");
        request.setRequestURI("/api/admin/foo");
        Cookie cookie = new Cookie(AuthInterceptor.API_ADMIN_COOKIE, "foo@bar.com");
        request.setCookies(new Cookie[]{cookie});
        response = new MockHttpServletResponse();
        assertTrue(_authInterceptor.preHandle(request, response, "nothing"));
    }
}
