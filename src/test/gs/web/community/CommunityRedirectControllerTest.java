package gs.web.community;

import gs.web.BaseControllerTestCase;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

public class CommunityRedirectControllerTest extends BaseControllerTestCase {
    private CommunityRedirectController _controller;

    public void testShouldRedirectCommunityRequests() throws Exception {
        _request.setServerName("www.greatschools.net");
        _request.addParameter(CommunityRedirectController.PAGE_PARAM, "/advice/write/");
        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Expected permanent redirect so crawlers will follow link", HttpServletResponse.SC_MOVED_PERMANENTLY, _response.getStatus());
        assertEquals("Unexpected redirect URL", "http://community.greatschools.net/advice/write/", _response.getHeader("Location"));
        assertNull("No model and view should be returned", modelAndView);
    }

    public void testShouldRedirectStagingRequests() throws Exception {
        _request.setServerName("staging.greatschools.net");
        _request.addParameter(CommunityRedirectController.PAGE_PARAM, "/advice/read/");
        _controller.handleRequest(_request, _response);
        assertEquals("Unexpected staging redirect URL", "http://community.staging.greatschools.net/advice/read/", _response.getHeader("Location"));
    }

    public void testShouldRedirectDevRequests() throws Exception {
        _request.setServerName("main.dev.greatschools.net");
        _request.addParameter(CommunityRedirectController.PAGE_PARAM, "/members/");
        _controller.handleRequest(_request, _response);
        assertEquals("Unexpected dev redirect URL", "http://community.dev.greatschools.net/members/", _response.getHeader("Location"));
    }

    public void testShouldRedirectProductionCobrandRequests() throws Exception {
        _request.setServerName("sfgate.greatschools.net");
        _request.addParameter(CommunityRedirectController.PAGE_PARAM, "/blah/");
        _controller.handleRequest(_request, _response);
        assertEquals("Unexpected redirect URL", "http://community.greatschools.net/blah/", _response.getHeader("Location"));
    }

    public void testShouldRedirectStagingCobrandRequests() throws Exception {
        _request.setServerName("encarta.staging.greatschools.net");
        _request.addParameter(CommunityRedirectController.PAGE_PARAM, "/q-and-a/");
        _controller.handleRequest(_request, _response);
        assertEquals("Unexpected staging redirect URL", "http://community.staging.greatschools.net/q-and-a/", _response.getHeader("Location"));
    }

    public void testShouldRedirectDevCobrandRequests() throws Exception {
        _request.setServerName("yahooed.dev.greatschools.net");
        _request.addParameter(CommunityRedirectController.PAGE_PARAM, "/mygroups/");
        _controller.handleRequest(_request, _response);
        assertEquals("Unexpected dev redirect URL", "http://community.dev.greatschools.net/mygroups/", _response.getHeader("Location"));
    }

    public void testShouldPrependSlashIfNecessary() throws Exception {
        _request.setServerName("www.greatschools.net");
        _request.addParameter(CommunityRedirectController.PAGE_PARAM, "search");
        _controller.handleRequest(_request, _response);
        assertEquals("Unexpected redirect URL", "http://community.greatschools.net/search", _response.getHeader("Location"));
    }

    public void testShouldNotURLEncodeParameters() throws Exception {
        _request.setServerName("www.greatschools.net");
        _request.addParameter(CommunityRedirectController.PAGE_PARAM, "/search?query=homework&search_type=3&x=0&y=0");
        _controller.handleRequest(_request, _response);
        assertEquals("Unexpected redirect URL", "http://community.greatschools.net/search?query=homework&search_type=3&x=0&y=0", _response.getHeader("Location"));
    }

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new CommunityRedirectController();
    }
}
