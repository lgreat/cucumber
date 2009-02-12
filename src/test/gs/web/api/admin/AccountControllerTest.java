package gs.web.api.admin;

import gs.web.BaseControllerTestCase;
import gs.data.api.ApiAccount;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * Created by chriskimm@greatschools.net
 */
public class AccountControllerTest extends BaseControllerTestCase {

    private AccountController _controller;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        _controller = new AccountController();
        _controller.setCommandName("account");
        _controller.setCommandClass(ApiAccount.class);
        _controller.setFormView("form_view");
        _controller.setSuccessView("success_view");
    }

    public void testGetForm() throws Exception {
        getRequest().setMethod("GET");
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());
        System.out.println ("account: " + mAndV.getModel().get("account"));
//        List<String> messages = mAndV.getModel().get(AccountController.MESSAGES)
    }
}
