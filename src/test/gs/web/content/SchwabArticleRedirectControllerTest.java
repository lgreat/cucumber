package gs.web.content;

import gs.web.BaseControllerTestCase;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

public class SchwabArticleRedirectControllerTest extends BaseControllerTestCase {
    private SchwabArticleRedirectController _controller = new SchwabArticleRedirectController();

    public void testShouldAddThreeThousandToArticleId() throws Exception {
        _request.addParameter("r", "200");
        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Expected permanent redirect so crawlers will follow link", HttpServletResponse.SC_MOVED_PERMANENTLY, _response.getStatus());
        assertEquals("Unexpected redirect URL", "/cgi-bin/showarticle/3200", _response.getHeader("Location"));
    }

    public void testShouldRedirectToLDMicrositeIfNoID() throws Exception {
        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Expected permanent redirect so crawlers will follow link", HttpServletResponse.SC_MOVED_PERMANENTLY, _response.getStatus());
        assertEquals("Unexpected default redirect URL", "/content/specialNeeds.page", _response.getHeader("Location"));
    }

    public void testShouldRedirectToLDMicrositeOnNonNumericId() throws Exception {
        _request.addParameter("r", "blah");
        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Expected permanent redirect so crawlers will follow link", HttpServletResponse.SC_MOVED_PERMANENTLY, _response.getStatus());
        assertEquals("Unexpected default redirect URL", "/content/specialNeeds.page", _response.getHeader("Location"));
    }

    public void testShouldRedirectToLDMicrositeOnTooHighId() throws Exception {
        _request.addParameter("r", "1501");
        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Expected permanent redirect so crawlers will follow link", HttpServletResponse.SC_MOVED_PERMANENTLY, _response.getStatus());
        assertEquals("Unexpected redirect URL for ID > 1500", "/content/specialNeeds.page", _response.getHeader("Location"));
    }

    public void testStaticListShouldOverrideDefaultLogic() throws Exception {
        for (String key : SchwabArticleRedirectController.staticRedirects.keySet()) {
            _request.addParameter("r", key);
            ModelAndView modelAndView = _controller.handleRequest(_request, _response);
            assertEquals("Expected permanent redirect so crawlers will follow link", HttpServletResponse.SC_MOVED_PERMANENTLY, _response.getStatus());
            assertEquals("Unexpected redirect URL for key " + key,
                    "/cgi-bin/showarticle/" + SchwabArticleRedirectController.staticRedirects.get(key), _response.getHeader("Location"));
            _request.removeAllParameters();
        }
    }
}
