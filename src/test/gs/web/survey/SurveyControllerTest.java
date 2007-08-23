package gs.web.survey;

import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.data.survey.Survey;
import gs.data.survey.UserResponse;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.List;

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
        ModelAndView mAndV = _controller.handleRequest(request, getResponse());
        assertEquals("survey/form", mAndV.getViewName());

        Map model = mAndV.getModel();
        Survey survey = (Survey)model.get("survey");
        assertEquals("Sample Survey", survey.getTitle());

        /*
        Set keys = model.keySet();
        Iterator keyIter = keys.iterator();
        for (;keyIter.hasNext();) {
            System.out.println ("key: " + keyIter.next());
        }
        */
    }

    public void testOnBindAndValidate() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        //request.set
        UserResponseCommand command = new UserResponseCommand();
//        _controller.onBindAndValidate(getRequest(), command, null);
//        List<UserResponse> response = command.getResponses();
    }

    public void testHandleSubmit() throws Exception {
        //_controller.onSubmit()
    }
}
