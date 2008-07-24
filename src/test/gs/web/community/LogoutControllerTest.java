package gs.web.community;

import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.PageHelper;
import gs.data.community.User;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

public class LogoutControllerTest extends BaseControllerTestCase {

    LogoutController _controller;
    public void setUp() throws Exception {
        super.setUp();
        _controller = new LogoutController();
    }

    public void testLogout_default() throws Exception {
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        RedirectView view = (RedirectView)mAndV.getView();
        assertEquals("Expected default redirect to '/'", LogoutController.DEFAULT_VIEW, view.getUrl());
    }

    public void testLogout_default_user_logged_in() throws Exception {
        User user = new User();
        user.setEmail("flippy@flimflam.com");
        user.setId(4321);
        PageHelper.setMemberCookie(getRequest(), getResponse(), user);
        assertNotNull(getResponse().getCookie("MEMID"));

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        RedirectView view = (RedirectView)mAndV.getView();
        assertEquals("Expected default redirect to '/'", LogoutController.DEFAULT_VIEW, view.getUrl());
    }

    public void testRedirectParameter() throws Exception {
        getRequest().setParameter(LogoutController.PARAM_REDIRECT, "/community/doSomething.page");
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        RedirectView view = (RedirectView)mAndV.getView();
        assertEquals("Expected default redirect to /community/doSomething.page",
                "/community/doSomething.page", view.getUrl());        
    }
}
