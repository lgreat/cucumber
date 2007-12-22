package gs.web.test;

import gs.web.BaseControllerTestCase;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author chriskimm@greatschools.net
 */
public class TestTestLandingController extends BaseControllerTestCase {

    private TestLandingController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (TestLandingController)getApplicationContext().getBean(TestLandingController.BEAN_ID);
    }

    public void testRequestForm() throws Exception {
        getRequest().setMethod("GET");
        getRequest().setParameter("state", "FL");
        getRequest().setParameter("tid", "1");
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());
        assertEquals("/test/landing", mAndV.getViewName());
        assertEquals("Here is some FL FCAT text.", mAndV.getModel().get("info"));
        assertEquals("link text 1 : http://www.google.com\nlink text 2 : http://www.greatschools.net", mAndV.getModel().get("links"));
    }
}
