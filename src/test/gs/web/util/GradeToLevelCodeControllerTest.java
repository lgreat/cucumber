package gs.web.util;

import gs.web.BaseControllerTestCase;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Dave Roy <mailto:droy@greatschools.net>
 */
public class GradeToLevelCodeControllerTest extends BaseControllerTestCase {

    private GradeToLevelCodeController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (GradeToLevelCodeController)getApplicationContext().
                getBean(GradeToLevelCodeController.BEAN_ID);
    }

    public void testHandleRequest() throws Exception {
        MockHttpServletResponse response = getResponse();

        // Test grade without name hint
        getRequest().setParameter("grades", "6");

        _controller.handleRequest(getRequest(), response);
        assertEquals("content type should be \"text/plain\"", "text/plain",
                response.getContentType());
        assertEquals("m", response.getContentAsString());

        // Test same grade but with a name hint
        response = new MockHttpServletResponse();
        getRequest().setParameter("grades", "6");
        getRequest().setParameter("name", "Foo Elementary");
        _controller.handleRequest(getRequest(), response);
        assertEquals("e", response.getContentAsString());

        // Test invalid grade returns emtpy
        response = new MockHttpServletResponse();
        getRequest().setParameter("grades", "6-8");  // Invalid input
        getRequest().setParameter("name", "Foo Elementary");
        _controller.handleRequest(getRequest(), response);
        assertEquals("", response.getContentAsString());
    }
}