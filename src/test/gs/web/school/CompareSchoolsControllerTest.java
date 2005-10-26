package gs.web.school;

import gs.web.BaseTestCase;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class CompareSchoolsControllerTest extends BaseTestCase {

    private CompareSchoolsController controller;

    protected void setUp() {
        controller = (CompareSchoolsController) _sApplicationContext.
                getBean(CompareSchoolsController.BEAN_ID);
    }

    public void testCompareWithState() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String[] stateIds = {"ca1", "ca2", "ca3"};
        request.addParameter("ids", stateIds);
        request.setAttribute("compare.x", "1234");
        //ModelAndView mav = controller.handleRequestInternal(request, (HttpServletResponse)null);
        // todo
    }

    public void testCompareWithoutState() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String[] stateIds = {"ca1", "ca2", "ca3"};
        request.addParameter("ids", stateIds);
        request.addParameter("compare.x", "1234");
        //ModelAndView mav = controller.handleRequestInternal(request, (HttpServletResponse)null);
        //RedirectView view = (RedirectView)mav.getView();
        //assertTrue(view.getUrl().matches("/cgi-bin/msl_compare/ca/\\?ids=$"));
        // todo
    }

    public void testCompareWithoutStateMixedIds() {
        // todo
    }

    public void testConfirm() {
        // todo
    }
}
