package gs.web.admin;

import gs.web.BaseControllerTestCase;
import org.springframework.web.servlet.ModelAndView;

public class LimitToDevControllerTest extends BaseControllerTestCase {
    private LimitToDevController _controller;
    private String _successViewName = "some view";
    private String _errorViewName = "error view";

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new LimitToDevController();
        _controller.setViewName(_successViewName);
        _controller.setErrorViewName(_errorViewName);
    }

    public void testRequestToDevSucceeds() throws Exception {
        _sessionContext.setHostName("dev.greatschools.net");
        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Unexpected view for dev", _successViewName, modelAndView.getViewName());
    }

    public void testRequestToProductionFails() throws Exception {
        _sessionContext.setHostName("www.greatschools.net");
        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Unexpected view for production", _errorViewName, modelAndView.getViewName());
    }
}
