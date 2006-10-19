package gs.web.school;

import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

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
        ModelAndView mAndV = _controller.handleRequestInternal(request, getResponse());
        assertNotNull (mAndV);
    }

    public void testFindTopRatedSchoolsForm() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setParameter(ResearchController.FORM_PARAM ,"cities");
        request.setParameter(ResearchController.STATE_PARAM, "DC");
        ModelAndView mAndV = _controller.handleRequestInternal(request, getResponse());
        RedirectView v = (RedirectView)mAndV.getView();
        assertEquals("/city/Washington/DC", v.getUrl());
    }
}
