package gs.web.school;

import gs.web.MockHttpServletRequest;
import gs.web.BaseControllerTestCase;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
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
        ModelAndView mav = controller.handleRequestInternal(getRequest(), (HttpServletResponse)null);
        RedirectView view = (RedirectView)mav.getView();
        assertEquals("http://www.greatschools.net/modperl/msl_compare/ca/?ids=ca1,ca2,ca3", view.getUrl());
    }

    public void testConfirm() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("confirm.x", "1234");
        String[] stateIds = {"ca1", "ca2", "ca3"};
        request.addParameter("sc", stateIds);
        ModelAndView mav = controller.handleRequestInternal(request, (HttpServletResponse)null);
        RedirectView view = (RedirectView)mav.getView();
        assertEquals("/cgi-bin/msl_confirm/ca/?add_ids=1&add_ids=2&add_ids=3", view.getUrl());
    }
    public void testNullSessionContext() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("compare.x", "1234");
        ModelAndView mav = controller.handleRequestInternal(request, (HttpServletResponse)null);
        RedirectView view = (RedirectView)mav.getView();
        assertEquals("/modperl/msl_compare/ca/?ids=", view.getUrl());
    }

    public void testCompareWithoutState() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
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
