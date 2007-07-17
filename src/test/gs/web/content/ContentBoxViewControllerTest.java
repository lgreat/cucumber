package gs.web.content;

import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;

/**
 * Tests for the content boxes
 * <p/>
 * http://dev.greatschools.net/content/box/v1/WY/feature01.page
 * http://dev.greatschools.net/content/box/v1/WY/tipOfTheWeek.page
 */
public class ContentBoxViewControllerTest extends BaseControllerTestCase {
    private ContentBoxViewController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (ContentBoxViewController) getApplicationContext().getBean(ContentBoxViewController.BEAN_ID);
    }

    public void testHandleRequest() throws Exception {
        // Test a normal dev server request
        GsMockHttpServletRequest request = getRequest();
        request.setRequestURI("/content/box/v1/WY/feature01.page");
        request.setServerName("dev.greatschools.net");
        _controller.handleRequestInternal(request, getResponse());
        assertEquals("feature01", request.getAttribute(ContentBoxViewController.MODEL_FEATURE));
        assertEquals("WY", request.getAttribute(ContentBoxViewController.MODEL_STATE));
        assertEquals("od6", request.getAttribute(ContentBoxViewController.MODEL_WORKSHEET));

        // Test a production server request
        request.setServerName("www.greatschools.net");
        _controller.handleRequestInternal(request, getResponse());
        assertEquals("od7", request.getAttribute(ContentBoxViewController.MODEL_WORKSHEET));
    }
}
