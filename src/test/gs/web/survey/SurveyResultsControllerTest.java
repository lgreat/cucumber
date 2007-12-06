package gs.web.survey;

import gs.data.survey.SurveyResults;
import gs.web.BaseControllerTestCase;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * @author chriskimm@greatschools.net
 */
public class SurveyResultsControllerTest extends BaseControllerTestCase {

    private SurveyResultsController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (SurveyResultsController)getApplicationContext().
                getBean(SurveyResultsController.BEAN_ID);
    }

    public void testGetResultsForNonExistentSchool() throws Exception {
        //getRequest().setParameter("level", "h");
        //ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        //Map model = mAndV.getModel();
        //SurveyResults results = (SurveyResults)model.get("results");
        //assertEquals("SurveyResults should have 3 totat responses", 3, results.getTotalResponses());
    }

}
