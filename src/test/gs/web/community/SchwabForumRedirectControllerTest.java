package gs.web.community;

import gs.web.BaseControllerTestCase;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

public class SchwabForumRedirectControllerTest extends BaseControllerTestCase {
    private SchwabForumRedirectController _controller = new SchwabForumRedirectController();

    public void testShouldRedirectToStaticPage() throws Exception {
        _request.addParameter("thread", "200");
        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Expected permanent redirect so crawlers will follow link", HttpServletResponse.SC_MOVED_PERMANENTLY, _response.getStatus());
        assertEquals("Unexpected redirect URL", "http://schwabforumarchive.greatschools.net/archive/200.html", _response.getHeader("Location"));
    }

    public void testShouldRedirectToLDMicrositeIfNoID() throws Exception {
        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Expected permanent redirect so crawlers will follow link", HttpServletResponse.SC_MOVED_PERMANENTLY, _response.getStatus());
        assertEquals("Unexpected default redirect URL", "http://www.greatschools.net/content/specialNeeds.page", _response.getHeader("Location"));
    }

    public void testShouldRedirectToLDMicrositeOnNonNumericId() throws Exception {
        _request.addParameter("thread", "blah");
        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Expected permanent redirect so crawlers will follow link", HttpServletResponse.SC_MOVED_PERMANENTLY, _response.getStatus());
        assertEquals("Unexpected default redirect URL", "http://www.greatschools.net/content/specialNeeds.page", _response.getHeader("Location"));
    }

    public void testShouldRedirectErrorsToCorrectEnvironment() throws Exception {
        _sessionContext.setHostName("schwablearning.dev.greatschools.net");
        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Unexpected redirect URL schwablearning.dev.greatschools.net",
                "http://dev.greatschools.net/content/specialNeeds.page", _response.getHeader("Location"));

        _sessionContext.setHostName("schwablearning.staging.greatschools.net");
        modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Unexpected redirect URL for schwablearning.staging.greatschools.net",
                "http://staging.greatschools.net/content/specialNeeds.page", _response.getHeader("Location"));

        _sessionContext.setHostName("schwablearning.greatschools.net");
        modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Unexpected redirect URL for schwablearning.greatschools.net",
                "http://www.greatschools.net/content/specialNeeds.page", _response.getHeader("Location"));

        _sessionContext.setHostName("www.schwablearning.org");
        modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Unexpected redirect URL for www.schwablearning.org",
                "http://www.greatschools.net/content/specialNeeds.page", _response.getHeader("Location"));

    }


}
