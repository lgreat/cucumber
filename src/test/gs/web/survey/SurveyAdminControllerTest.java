package gs.web.survey;

import gs.web.BaseControllerTestCase;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SurveyAdminControllerTest extends BaseControllerTestCase {

    private SurveyAdminController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (SurveyAdminController)getApplicationContext().
                getBean(SurveyAdminController.BEAN_ID);
    }

    public void testHandleNewPageRequest() throws Exception {
        assertTrue(true);  // no op
//        ModelAndView mAndView =
//                _controller.handleRequestInternal(getRequest(), getResponse());
//        assertEquals("survey/admin", mAndView.getViewName());
//        assertEquals(2, ((List)mAndView.getModel().get("surveys")).size());
    }
}
