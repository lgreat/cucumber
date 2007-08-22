package gs.web.survey;

import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.data.survey.Survey;
import gs.data.survey.Question;
import gs.data.survey.SurveyQuestionResponse;
import org.springframework.web.servlet.ModelAndView;

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
    }
}
