package gs.web.search;

import gs.web.BaseControllerTestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author thuss
 */
public class RawSearchControllerTest extends BaseControllerTestCase {

    RawSearchController _controller;

    ApplicationContext _ctx;

    public void setUp() throws Exception {
        _ctx = getApplicationContext();
        _controller = (RawSearchController) _ctx.getBean(RawSearchController.BEAN_ID);
        super.setUp();
    }

    public void testHandleRequestInternal() throws Exception {
        MockHttpServletRequest request = getRequest();
        request.addParameter("q", "lincoln");
        request.addParameter("analyzer", "gs");
        ModelAndView mv = _controller.handleRequestInternal(request, getResponse());
        assertNotNull(mv);
        assertEquals("4", mv.getModel().get("total"));
    }
}
