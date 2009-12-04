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
        assertEquals("Unexpected redirect URL", "http://schwablearningforumarchive.greatschools.org/thread/200.html", _response.getHeader("Location"));
    }

    public void testShouldRedirectToLDMicrositeIfNoID() throws Exception {
        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Expected permanent redirect so crawlers will follow link", HttpServletResponse.SC_MOVED_PERMANENTLY, _response.getStatus());
        assertEquals("Unexpected default redirect URL", "http://www.greatschools.org/content/specialNeeds.page?fromSchwab=1", _response.getHeader("Location"));
    }

    public void testShouldRedirectToLDMicrositeOnNonNumericId() throws Exception {
        _request.addParameter("thread", "blah");
        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Expected permanent redirect so crawlers will follow link", HttpServletResponse.SC_MOVED_PERMANENTLY, _response.getStatus());
        assertEquals("Unexpected default redirect URL", "http://www.greatschools.org/content/specialNeeds.page?fromSchwab=1", _response.getHeader("Location"));
    }

    public void testShouldRedirectToCorrectEnvironment() throws Exception {
        _sessionContext.setHostName("www.schwablearning.org");
        _request.addParameter("thread", "200");
        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Expected permanent redirect so crawlers will follow link", HttpServletResponse.SC_MOVED_PERMANENTLY, _response.getStatus());
        assertEquals("Unexpected redirect URL", "http://schwablearningforumarchive.greatschools.org/thread/200.html", _response.getHeader("Location"));

        _sessionContext.setHostName("schwablearning.greatschools.org");
        modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Unexpected redirect URL", "http://schwablearningforumarchive.greatschools.org/thread/200.html", _response.getHeader("Location"));

        _sessionContext.setHostName("schwablearning.staging.greatschools.org");
        modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Unexpected redirect URL", "http://schwablearningforumarchive.staging.greatschools.org/thread/200.html", _response.getHeader("Location"));

        _sessionContext.setHostName("schwablearning.dev.greatschools.org");
        modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Unexpected redirect URL", "http://schwablearningforumarchive.dev.greatschools.org/thread/200.html", _response.getHeader("Location"));
    }

    public void testShouldRedirectErrorsToCorrectEnvironment() throws Exception {
        _sessionContext.setHostName("schwablearning.dev.greatschools.org");
        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Unexpected redirect URL schwablearning.dev.greatschools.org",
                "http://dev.greatschools.org/content/specialNeeds.page?fromSchwab=1", _response.getHeader("Location"));

        _sessionContext.setHostName("schwablearning.staging.greatschools.org");
        modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Unexpected redirect URL for schwablearning.staging.greatschools.org",
                "http://staging.greatschools.org/content/specialNeeds.page?fromSchwab=1", _response.getHeader("Location"));

        _sessionContext.setHostName("schwablearning.greatschools.org");
        modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Unexpected redirect URL for schwablearning.greatschools.org",
                "http://www.greatschools.org/content/specialNeeds.page?fromSchwab=1", _response.getHeader("Location"));

        _sessionContext.setHostName("www.schwablearning.org");
        modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Unexpected redirect URL for www.schwablearning.org",
                "http://www.greatschools.org/content/specialNeeds.page?fromSchwab=1", _response.getHeader("Location"));

    }


}
