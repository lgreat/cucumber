package gs.web.status;

import gs.web.BaseControllerTestCase;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpSession;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class LoginControllerTest extends BaseControllerTestCase {

    private LoginController _controller;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = (LoginController) getApplicationContext().getBean(LoginController.BEAN_ID);
    }

    public void testOnSubmit() throws Exception {
        Identity command = new Identity();
        command.setUsername("foo");
        command.setPassword("bar");
        ModelAndView mAndV = _controller.onSubmit(getRequest(), getResponse(), command, null);
        RedirectView view = (RedirectView)mAndV.getView();
        assertEquals ("/status/searchmanager.page", view.getUrl());

        HttpSession session = getRequest().getSession();
        Identity ident = (Identity)session.getAttribute("identity");
        assertEquals (ident.getUsername(), "foo");
        assertEquals (ident.getPassword(), "bar");
    }
}
