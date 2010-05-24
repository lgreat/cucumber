package gs.web.school;

import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.geo.StateSpecificFooterHelper;
import gs.web.util.google.GoogleSpreadsheetDaoFactory;
import gs.data.util.table.ITableDao;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.org>
 */
public class ResearchControllerTest extends BaseControllerTestCase {

    private ResearchController _controller;

    public void setUp () throws Exception {
        super.setUp();
        _controller = new ResearchController();
        GoogleSpreadsheetDaoFactory factory = new GoogleSpreadsheetDaoFactory();
        factory.setGoogleKey("pYwV1uQwaOCJGhxtFDPHjTg");
        factory.setVisibility("public");
        factory.setProjection("values");
        factory.setWorksheetName("od6");
        ITableDao tableDao = factory.getTableDao();
        _controller.setTableDao(tableDao);
        _controller.setStateSpecificFooterHelper(org.easymock.classextension.EasyMock.createMock(
                StateSpecificFooterHelper.class));
    }

    public void testLoadCache() throws Exception {
        Map<String, Map> cache = new HashMap<String, Map>();
        _controller.loadCache(cache);
        Map<String, Object> values = cache.get("CA");
        assertNotNull(values);
        assertNotNull(values.get("alert"));
        assertNotNull(values.get("alertlink"));
        assertNotNull(values.get("alertexpireDT"));
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
