package gs.web.survey;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.mock.web.MockHttpServletRequest;
import gs.web.BaseControllerTestCase;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SurveyEditControllerTest extends BaseControllerTestCase {

    private SurveyEditController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (SurveyEditController)getApplicationContext().
                getBean(SurveyEditController.BEAN_ID);        
    }

    public void testGetForm() throws Exception {
        MockHttpServletRequest request = getRequest();
        request.setMethod("GET");
        ModelAndView mAndView = _controller.handleRequest(request, getResponse());
    }
}
