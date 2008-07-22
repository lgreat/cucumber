package gs.web.community;

import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.PageHelper;
import gs.data.community.User;
import org.springframework.web.servlet.ModelAndView;

public class LogoutControllerTest extends BaseControllerTestCase {

    LogoutController _controller;
    public void setUp() throws Exception {
        super.setUp();
        _controller = new LogoutController();
    }

    public void testLogout_default() throws Exception {
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        assertEquals("Expected default redirect view", "redirect:/", mAndV.getViewName());
    }

    public void testLogout_user_logged_in() throws Exception {
        User user = new User();
        user.setEmail("flippy@flimflam.com");
        user.setId(4321);
        PageHelper.setMemberCookie(getRequest(), getResponse(), user);
        assertNotNull(getResponse().getCookie("MEMID"));

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        assertEquals("Expected default redirect view", "redirect:/", mAndV.getViewName());

//        assertEquals("Max age should = 0", 0, getResponse().getCookie("MEMID").getMaxAge());
    }
}
