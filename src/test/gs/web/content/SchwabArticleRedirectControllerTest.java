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
                "http://www.greatschools.net/cgi-bin/showarticle/2200", _response.getHeader("Location"));
    }

    public void testShouldRedirectToLDMicrositeIfNoID() throws Exception {
        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Expected permanent redirect so crawlers will follow link", HttpServletResponse.SC_MOVED_PERMANENTLY, _response.getStatus());
        assertEquals("Unexpected default redirect URL",
                "http://www.greatschools.net/content/specialNeeds.page", _response.getHeader("Location"));
    }

    public void testShouldRedirectToLDMicrositeOnNonNumericId() throws Exception {
        _request.addParameter("r", "blah");
        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Expected permanent redirect so crawlers will follow link", HttpServletResponse.SC_MOVED_PERMANENTLY, _response.getStatus());
        assertEquals("Unexpected default redirect URL",
                "http://www.greatschools.net/content/specialNeeds.page", _response.getHeader("Location"));
    }

    public void testShouldRedirectToLDMicrositeOnTooHighId() throws Exception {
        _request.addParameter("r", "1501");
        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Expected permanent redirect so crawlers will follow link", HttpServletResponse.SC_MOVED_PERMANENTLY, _response.getStatus());
        assertEquals("Unexpected redirect URL for ID > 1500",
                "http://www.greatschools.net/content/specialNeeds.page", _response.getHeader("Location"));
    }

    public void testStaticListShouldOverrideDefaultLogic() throws Exception {
        for (String key : SchwabArticleRedirectController.staticRedirects.keySet()) {
            _request.addParameter("r", key);
            ModelAndView modelAndView = _controller.handleRequest(_request, _response);
            assertEquals("Expected permanent redirect so crawlers will follow link", HttpServletResponse.SC_MOVED_PERMANENTLY, _response.getStatus());
            assertEquals("Unexpected redirect URL for key " + key,
                    "http://www.greatschools.net/cgi-bin/showarticle/" + SchwabArticleRedirectController.staticRedirects.get(key),
                    _response.getHeader("Location"));
            _request.removeAllParameters();
        }
    }

    public void testShouldRedirectToCorrectEnvironment() throws Exception {
        _request.addParameter("r", "200");
        _sessionContext.setHostName("schwablearning.dev.greatschools.net");
        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Unexpected redirect URL schwablearning.dev.greatschools.net",
                "http://dev.greatschools.net/cgi-bin/showarticle/2200", _response.getHeader("Location"));

        _sessionContext.setHostName("schwablearning.staging.greatschools.net");
        modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Unexpected redirect URL for schwablearning.staging.greatschools.net",
                "http://staging.greatschools.net/cgi-bin/showarticle/2200", _response.getHeader("Location"));

        _sessionContext.setHostName("schwablearning.greatschools.net");
        modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Unexpected redirect URL for schwablearning.greatschools.net",
                "http://www.greatschools.net/cgi-bin/showarticle/2200", _response.getHeader("Location"));

        _sessionContext.setHostName("www.schwablearning.org");
        modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Unexpected redirect URL for www.schwablearning.org",
                "http://www.greatschools.net/cgi-bin/showarticle/2200", _response.getHeader("Location"));

    }

}
