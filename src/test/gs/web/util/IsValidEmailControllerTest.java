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
//        _controller = (IsValidEmailController)getApplicationContext().
//                getBean(IsValidEmailController.BEAN_ID);
        _controller = new IsValidEmailController();
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

    public void testMultipleEmails() throws Exception {
        MockHttpServletResponse response = getResponse();
        getRequest().setParameter("emails", "aroy@greatschools.org, foo");
        _controller.handleRequest(getRequest(), response);
        assertEquals("false", response.getContentAsString());

        response = new MockHttpServletResponse();
        getRequest().setParameter("emails", "aroy@greatschools.org, foo@greatschools.org");
        _controller.handleRequest(getRequest(), response);
        assertEquals("true", response.getContentAsString());
    }

    public void testMultipleEmailsWithDetails() throws Exception {
        MockHttpServletResponse response = getResponse();
        getRequest().setParameter("emails", "aroy@greatschools.org, foo");
        getRequest().setParameter("details", "1");
        _controller.handleRequest(getRequest(), response);
        assertEquals("foo", response.getContentAsString());

        response = new MockHttpServletResponse();
        getRequest().setParameter("emails", "not_good@example,aroy@greatschools.org,foo");
        getRequest().setParameter("details", "1");
        _controller.handleRequest(getRequest(), response);
        assertEquals("not_good@example, foo", response.getContentAsString());

        response = new MockHttpServletResponse();
        getRequest().setParameter("emails", "aroy@greatschools.org, foo@greatschools.org");
        _controller.handleRequest(getRequest(), response);
        assertEquals("", response.getContentAsString());
    }
}
