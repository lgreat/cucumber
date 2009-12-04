package gs.web.util;

import gs.web.BaseControllerTestCase;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.org>
 */
public class IsValidEmailControllerTest extends BaseControllerTestCase {

    private IsValidEmailController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (IsValidEmailController)getApplicationContext().
                getBean(IsValidEmailController.BEAN_ID);
    }

    public void testHandleRequest() throws Exception {
        MockHttpServletResponse response = getResponse();
        getRequest().setParameter("email", "fan s d@greatschools.org");
        _controller.handleRequest(getRequest(), response);
        assertEquals("content type should be \"text/plain\"", "text/plain",
                response.getContentType());
        assertEquals("false", response.getContentAsString());

        response = new MockHttpServletResponse();
        getRequest().setParameter("email", "valid@greatschools.org");
        _controller.handleRequest(getRequest(), response);
        assertEquals("true", response.getContentAsString());        
    }
}
