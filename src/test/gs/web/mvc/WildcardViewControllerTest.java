package gs.web.mvc;

import gs.web.BaseControllerTestCase;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by chriskimm@greatschools.net
 */
public class WildcardViewControllerTest extends BaseControllerTestCase {

    private WildcardViewController _controller;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        _controller = new WildcardViewController();
        _controller.setBasePath("foo/bar");
    }

    public void testRequest() throws Exception {
        getRequest().setMethod("GET");
        getRequest().setRequestURI("/foo/bar/tester");
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        assertEquals("foo/bar/tester", mAndV.getViewName());

        getRequest().setRequestURI("/foo/bar/tester.page");
        mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        assertEquals("foo/bar/tester", mAndV.getViewName());

        getRequest().setRequestURI("/foo/bar/blah/tester.page");
        mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        assertEquals("foo/bar/blah/tester", mAndV.getViewName());        
    }
}
