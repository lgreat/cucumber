package gs.web.school;

import gs.web.GsMockHttpServletRequest;
import gs.web.BaseControllerTestCase;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.org>
 */
public class CompareSchoolsControllerTest extends BaseControllerTestCase {

    private CompareSchoolsController controller;

    protected void setUp() throws Exception {
        super.setUp();
        controller = (CompareSchoolsController) getApplicationContext().
                getBean(CompareSchoolsController.BEAN_ID);
    }

    public void testCompareWithState() throws Exception {
        String[] stateIds = {"ca1", "ca2", "ca3"};
        getRequest().addParameter("sc", stateIds);
        getRequest().setParameter("compare.x", "1234");
        ModelAndView mav = controller.handleRequestInternal(getRequest(), null);
        RedirectView view = (RedirectView)mav.getView();
        assertEquals("http://www.greatschools.org/school-comparison-tool/results.page?schools=ca1%2Cca2%2Cca3", view.getUrl());
    }

    public void testConfirm() throws Exception {
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setParameter("confirm.x", "1234");
        String[] stateIds = {"ca1", "ca2", "ca3"};
        request.addParameter("sc", stateIds);
        ModelAndView mav = controller.handleRequestInternal(request, null);
        RedirectView view = (RedirectView)mav.getView();
        assertEquals("/cgi-bin/msl_confirm/ca/?add_ids=1&add_ids=2&add_ids=3", view.getUrl());
    }
    public void testNullSessionContext() throws Exception {
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setParameter("compare.x", "1234");
        ModelAndView mav = controller.handleRequestInternal(request, null);
        RedirectView view = (RedirectView)mav.getView();
        assertEquals("http://localhost/school-comparison-tool/results.page", view.getUrl());
    }

    public void testCompareWithoutState() throws Exception {
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        String[] stateIds = {"ca1", "ca2", "ca3"};
        request.addParameter("ids", stateIds);
        request.addParameter("compare.x", "1234");
        //ModelAndView mav = controller.handleRequestInternal(request, (HttpServletResponse)null);
        //RedirectView view = (RedirectView)mav.getView();
        //assertTrue(view.getUrl().matches("/modperl/msl_compare/ca/\\?ids=$"));
        // todo
    }

    public void testCompareWithoutStateMixedIds() {
        // todo
    }
}
