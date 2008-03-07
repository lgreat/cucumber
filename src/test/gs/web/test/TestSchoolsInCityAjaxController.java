package gs.web.test;

import gs.web.BaseControllerTestCase;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class TestSchoolsInCityAjaxController extends BaseControllerTestCase {
    private SchoolsInCityAjaxController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (SchoolsInCityAjaxController)getApplicationContext().
                getBean(SchoolsInCityAjaxController.BEAN_ID);
    }

    public void testHandleRequest() throws Exception {
        getRequest().setParameter("state", "AK");
        getRequest().setParameter("city", "Anchorage");
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());
        assertNull(mAndV);
        String anchorageOutput = getResponse().getContentAsString();
        assertTrue (anchorageOutput.contains("<option value=\"116\">Abbott Loop Elementary School</option>"));

        getRequest().setParameter("city", "Fairbanks");
        _controller.handleRequest(getRequest(), getResponse());
        String fairbanksOutput = getResponse().getContentAsString();
        assertTrue (fairbanksOutput.contains("<option value=\"383\">West Valley High School</option>"));
    }

    public void testOutputSchoolSelect() throws Exception {
        try {
            _controller.outputSchoolSelect(getRequest(), getResponse().getWriter(), null);
            fail ("Method should not accept request without a 'state' attribute");
        } catch (Exception e) {
            assertTrue (true);
        }

        
    }
}
