package gs.web.content;

import gs.web.BaseControllerTestCase;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

public class SchwabArticleRedirectControllerTest extends BaseControllerTestCase {
    private SchwabArticleRedirectController _controller = new SchwabArticleRedirectController();

    public void testShouldAddTwoThousandToArticleId() throws Exception {
        _request.addParameter("r", "200");
        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Expected permanent redirect so crawlers will follow link", HttpServletResponse.SC_MOVED_PERMANENTLY, _response.getStatus());
        assertEquals("Unexpected redirect URL",
                "http://www.greatschools.org/cgi-bin/showarticle/2200", _response.getHeader("Location"));
    }

    public void testShouldRedirectToLDMicrositeIfNoID() throws Exception {
        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Expected permanent redirect so crawlers will follow link", HttpServletResponse.SC_MOVED_PERMANENTLY, _response.getStatus());
        assertEquals("Unexpected default redirect URL",
                "http://www.greatschools.org/content/specialNeeds.page?fromSchwab=1", _response.getHeader("Location"));
    }

    public void testShouldRedirectToLDMicrositeOnNonNumericId() throws Exception {
        _request.addParameter("r", "blah");
        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Expected permanent redirect so crawlers will follow link", HttpServletResponse.SC_MOVED_PERMANENTLY, _response.getStatus());
        assertEquals("Unexpected default redirect URL",
                "http://www.greatschools.org/content/specialNeeds.page?fromSchwab=1", _response.getHeader("Location"));
    }

    public void testShouldRedirectToLDMicrositeOnTooHighId() throws Exception {
        _request.addParameter("r", "1501");
        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Expected permanent redirect so crawlers will follow link", HttpServletResponse.SC_MOVED_PERMANENTLY, _response.getStatus());
        assertEquals("Unexpected redirect URL for ID > 1500",
                "http://www.greatschools.org/content/specialNeeds.page?fromSchwab=1", _response.getHeader("Location"));
    }

    public void testStaticListShouldOverrideDefaultLogic() throws Exception {
        for (String key : SchwabArticleRedirectController.staticRedirects.keySet()) {
            _request.addParameter("r", key);
            ModelAndView modelAndView = _controller.handleRequest(_request, _response);
            assertEquals("Expected permanent redirect so crawlers will follow link", HttpServletResponse.SC_MOVED_PERMANENTLY, _response.getStatus());
            assertEquals("Unexpected redirect URL for key " + key,
                    "http://www.greatschools.org/cgi-bin/showarticle/" + SchwabArticleRedirectController.staticRedirects.get(key),
                    _response.getHeader("Location"));
            _request.removeAllParameters();
        }
    }

    public void testShouldRedirectToCorrectEnvironment() throws Exception {
        _request.addParameter("r", "200");
        _sessionContext.setHostName("schwablearning.dev.greatschools.org");
        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Unexpected redirect URL schwablearning.dev.greatschools.org",
                "http://dev.greatschools.org/cgi-bin/showarticle/2200", _response.getHeader("Location"));

        _sessionContext.setHostName("schwablearning.staging.greatschools.org");
        modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Unexpected redirect URL for schwablearning.staging.greatschools.org",
                "http://staging.greatschools.org/cgi-bin/showarticle/2200", _response.getHeader("Location"));

        _sessionContext.setHostName("schwablearning.greatschools.org");
        modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Unexpected redirect URL for schwablearning.greatschools.org",
                "http://www.greatschools.org/cgi-bin/showarticle/2200", _response.getHeader("Location"));

        _sessionContext.setHostName("www.schwablearning.org");
        modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Unexpected redirect URL for www.schwablearning.org",
                "http://www.greatschools.org/cgi-bin/showarticle/2200", _response.getHeader("Location"));

    }

}
