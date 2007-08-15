package gs.web.survey;

import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.data.survey.Survey;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SurveyControllerTest extends BaseControllerTestCase {

    private SurveyController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (SurveyController)getApplicationContext().
                getBean(SurveyController.BEAN_ID);
    }

    public void testHandleNewPageRequest() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setMethod("GET");
        //request.setAttribute("");
//        assertNull(request.getAttribute("surveyCommand"));
        ModelAndView mAndV = _controller.handleRequest(request, getResponse());
//        assertNotNull(request.getAttribute("surveyCommand"));

//        assertEquals("survey/admin", mAndView.getViewName());
//        assertEquals(2, ((List)mAndView.getModel().get("surveys")).size());
    }

    public void testHandleFormSubmit() throws Exception {

        Survey surveyCommand = null;
        ModelAndView mAndView =
                _controller.onSubmit(surveyCommand);
        assertEquals("Valid form submit did not return success view.",
                _controller.getSuccessView(), mAndView.getViewName());
    }
}
