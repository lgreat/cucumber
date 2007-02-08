package gs.web.school;

import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.util.context.SessionContext;
import gs.web.util.MockSessionContext;
import gs.data.school.School;
import gs.data.state.State;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.mock.web.MockHttpServletResponse;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.Cookie;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SchoolOverviewControllerTest extends BaseControllerTestCase {

    private SchoolOverviewController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (SchoolOverviewController)getApplicationContext().getBean(SchoolOverviewController.BEAN_ID);
    }

    public void testHandleRequest() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setAttribute("state", State.CA);
        request.setParameter("id", "1");
        request.setMethod("GET");
        ModelAndView mAndV = _controller.handleRequest(request, getResponse());
        School school = (School)mAndV.getModel().get("school");
        assertEquals("Alameda High School", school.getName());
        assertEquals(new Integer(2), mAndV.getModel().get("reviewCount"));
        assertTrue(StringUtils.isNotBlank((String)mAndV.getModel().get("reviewText")));
    }

    /*
     * When cobrand is number1expert and cookies set up right, this should behave as above.
     */
    public void testLeadGenPass() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setAttribute("state", State.CA);
        request.setParameter("id", "1");
        request.setMethod("GET");
        MockSessionContext sessionContext = new MockSessionContext();
        sessionContext.setCobrand("number1expert");
        sessionContext.setState(State.CA);
        request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, sessionContext);

        Cookie[] cookies = new Cookie[] {
                new Cookie("AGENTID", "1234"),
                new Cookie("BIREG1234", "1")
        };
        request.setCookies(cookies);

        MockHttpServletResponse response = getResponse();
        assertNull(response.getRedirectedUrl());
        ModelAndView mAndV = _controller.handleRequest(request, response);
        School school = (School)mAndV.getModel().get("school");
        assertEquals("Alameda High School", school.getName());
        assertEquals(new Integer(2), mAndV.getModel().get("reviewCount"));
        assertTrue(StringUtils.isNotBlank((String)mAndV.getModel().get("reviewText")));
        assertNull(response.getRedirectedUrl());
    }

    /*
     * When cobrand is number1expert and cookies are set up wrong, this should send a redirect
     */
    public void testLeadGenFail() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setAttribute("state", State.CA);
        request.setParameter("id", "1");
        request.setMethod("GET");
        MockSessionContext sessionContext = new MockSessionContext();
        sessionContext.setCobrand("number1expert");
        sessionContext.setState(State.CA);
        request.setAttribute(SessionContext.REQUEST_ATTRIBUTE_NAME, sessionContext);

        Cookie[] cookies = new Cookie[] {
                new Cookie("AGENTID", "1234"),
                new Cookie("BIREG1234", "0")
        };
        request.setCookies(cookies);

        MockHttpServletResponse response = getResponse();
        assertNull(response.getRedirectedUrl());
        _controller.handleRequest(request, response);
        assertNotNull(response.getRedirectedUrl());
    }
}
