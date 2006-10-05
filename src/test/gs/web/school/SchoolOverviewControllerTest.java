package gs.web.school;

import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.data.school.School;
import gs.data.state.State;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SchoolOverviewControllerTest extends BaseControllerTestCase {

    private SchoolOverviewController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (SchoolOverviewController)getApplicationContext().getBean(SchoolOverviewController.BEAN_ID);
    }

    public void testHandleRequestInternal() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setAttribute("state", State.CA);
        request.setParameter("id", "1");
        ModelAndView mAndV = _controller.handleRequestInternal(request, getResponse());
        School school = (School)mAndV.getModel().get("school");
        assertEquals("Alameda High School", school.getName());

        //Integer reviewCount = (Integer)mAndV.getModel().get("reviewCount");
    }
}
