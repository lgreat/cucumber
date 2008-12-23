package gs.web.school;

import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.RedirectView301;
import gs.data.school.TopSchoolCategory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

/**
 * @author thuss
 */
public class TopSchoolsControllerTest extends BaseControllerTestCase {

    private TopSchoolsController _controller;
    private SessionContextUtil _sessionContextUtil;


    protected void setUp() throws Exception {
        super.setUp();
        _controller = (TopSchoolsController) getApplicationContext().getBean(TopSchoolsController.BEAN_ID);
        _sessionContextUtil = (SessionContextUtil) getApplicationContext().getBean(SessionContextUtil.BEAN_ID);
    }

    public void testNational() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setRequestURI("/top-high-schools/");
        _sessionContextUtil.prepareSessionContext(request, getResponse());
        ModelAndView mv = _controller.handleRequestInternal(request, getResponse());
        assertEquals("/school/topSchools", mv.getViewName());
        assertEquals(true, mv.getModel().get(TopSchoolsController.MODEL_NATIONAL));
        assertEquals("California", mv.getModel().get(TopSchoolsController.MODEL_STATE_NAME));
        assertEquals("CA", mv.getModel().get(TopSchoolsController.MODEL_STATE_ABBREVIATION));
        assertTrue(((List)mv.getModel().get(TopSchoolsController.MODEL_ALL_STATES)).size() > 50);
    }

    public void testWyomingWhereWeHaveSampleData() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setRequestURI("/top-high-schools/wyoming");
        _sessionContextUtil.prepareSessionContext(request, getResponse());
        ModelAndView mv = _controller.handleRequestInternal(request, getResponse());
        assertEquals(false, mv.getModel().get(TopSchoolsController.MODEL_NATIONAL));
        assertEquals("Wyoming", mv.getModel().get(TopSchoolsController.MODEL_STATE_NAME));
        assertEquals("WY", mv.getModel().get(TopSchoolsController.MODEL_STATE_ABBREVIATION));
        List<TopSchoolsController.TopSchool> topSchools = (List<TopSchoolsController.TopSchool>) mv.getModel().get(TopSchoolsController.MODEL_TOP_SCHOOLS);
        assertTrue(topSchools.size() > 2);
        assertEquals("", topSchools.get(0).getReviewText());
        assertEquals(TopSchoolCategory.Poverty, topSchools.get(0).getTopSchoolCategory());
    }

    public void testRedirection() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setRequestURI("/top-high-schools/California");
        _sessionContextUtil.updateStateFromParam(getSessionContext(), request, getResponse());
        ModelAndView mAndV = _controller.handleRequestInternal(request, getResponse());
        assertTrue(mAndV.getView() instanceof RedirectView301);
        assertEquals("/top-high-schools/california", ((RedirectView) mAndV.getView()).getUrl());
    }

}
