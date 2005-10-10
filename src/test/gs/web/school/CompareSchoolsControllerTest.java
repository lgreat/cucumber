package gs.web.school;

import gs.web.BaseTestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class CompareSchoolsControllerTest extends BaseTestCase {

    private CompareSchoolsController controller;

    protected void setUp() {
        controller = (CompareSchoolsController)_sApplicationContext.
                getBean(CompareSchoolsController.BEAN_ID);
    }

    public void testCompareWithState() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String[] stateIds = {"ca1", "ca2", "ca3"};
        request.addParameter("ids", stateIds);
        request.setAttribute("compare.x", "1234");
        ModelAndView mav = controller.handleRequestInternal(request, (HttpServletResponse)null);
        // todo
    }

    public void testCompareWithoutState() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String[] stateIds = {"ca1", "ca2", "ca3"};
        request.addParameter("ids", stateIds);
        request.addParameter("compare.x", "1234");
        ModelAndView mav = controller.handleRequestInternal(request, (HttpServletResponse)null);
        RedirectView view = (RedirectView)mav.getView();
        System.out.println ("url: " + view.getUrl());
        assertTrue(view.getUrl().matches("/cgi-bin/msl_compare/ca/\\?ids=$"));
    }

    public void testCompareWithoutStateMixedIds() {
        // todo
    }

    public void testConfirm() {
        // todo
    }
}
