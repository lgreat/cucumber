package gs.web.school;

import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class ResearchControllerTest extends BaseControllerTestCase {

    private ResearchController _controller;

    public void setUp () throws Exception {
        super.setUp();
        _controller = new ResearchController();
    }

    public void testHanderRequestInternal() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        _controller.handleRequestInternal(request, getResponse());
    }
}
