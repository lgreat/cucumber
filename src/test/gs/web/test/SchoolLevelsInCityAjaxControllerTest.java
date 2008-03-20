package gs.web.test;

import gs.web.BaseControllerTestCase;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author chriskimm@greatschools.net
 */
public class SchoolLevelsInCityAjaxControllerTest extends BaseControllerTestCase {

    private SchoolLevelsInCityAjaxController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (SchoolLevelsInCityAjaxController)getApplicationContext().
                getBean(SchoolLevelsInCityAjaxController.BEAN_ID);
    }

    public void testHandleRequestMultiLevel() throws Exception {
        getRequest().setParameter("state", "AK");
        getRequest().setParameter("city", "Anchorage");
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());
        assertNull(mAndV);
        String jsonOutput = getResponse().getContentAsString();
        assertEquals("{\"levels\":[\"e\",\"m\",\"h\"]}", jsonOutput);
    }

    public void testHandleRequestSingleLevel() throws Exception {
        getRequest().setParameter("state", "AK");
        getRequest().setParameter("city", "Willow");
        getResponse().flushBuffer();
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());
        assertNull(mAndV);
        String jsonOutput = getResponse().getContentAsString();
        assertEquals("{\"levels\":[\"e\"]}", jsonOutput);
    }
}


