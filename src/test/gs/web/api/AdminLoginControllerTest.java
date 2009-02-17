package gs.web.api;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ModelMap;

import javax.servlet.http.Cookie;

/**
 * @author chriskimm@greatschools.net
 */
public class AdminLoginControllerTest {

    private AdminLoginController _controller;

    @Before
    public void setup() {
        _controller = new AdminLoginController();
    }

    @Test
    public void testLogout() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        String view = _controller.getPage(null, "logout", response, new ModelMap());
        assertEquals(AdminLoginController.MAIN_VIEW, view);
        Cookie c = response.getCookie(AdminLoginController.API_ADMIN_COOKIE_NAME);
        assertEquals(0, c.getMaxAge());
    }
}
