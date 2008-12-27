package gs.web.school;

import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.RedirectView301;
import gs.web.util.google.GoogleSpreadsheetDao;
import gs.data.school.TopSchoolCategory;
import gs.data.util.table.ITableRow;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import static org.easymock.classextension.EasyMock.*;

import java.util.List;
import java.util.ArrayList;

/**
 * @author thuss
 */
public class TopSchoolsControllerTest extends BaseControllerTestCase {

    private TopSchoolsController _controller;
    private SessionContextUtil _sessionContextUtil;
    private GoogleSpreadsheetDao _tableDao;


    protected void setUp() throws Exception {
        super.setUp();
        _controller = (TopSchoolsController) getApplicationContext().getBean(TopSchoolsController.BEAN_ID);
        _sessionContextUtil = (SessionContextUtil) getApplicationContext().getBean(SessionContextUtil.BEAN_ID);
        _tableDao = createMock(GoogleSpreadsheetDao.class);
        ITableRow row = createMock(ITableRow.class);
        expect(row.getString("title")).andReturn("A Title");
        expect(row.getString("link")).andReturn("http://www.greatschools.net");
        expect(row.getString("target")).andReturn("_blank");
        expect(row.getString("class")).andReturn("a-class");
        expect(row.getString("text")).andReturn("Some text");
        List<ITableRow> rows = new ArrayList<ITableRow>();
        rows.add(row);
        expect(_tableDao.getAllRows()).andReturn(rows);
        replay(row);
        replay(_tableDao);
        _controller.setTableDao(_tableDao);
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
        List<TopSchoolsController.ContentLink> content = (List<TopSchoolsController.ContentLink>) mv.getModel().get(TopSchoolsController.MODEL_WHAT_MAKES_A_SCHOOL_GREAT);
        assertEquals(1, content.size());
        assertEquals("A Title", content.get(0).getTitle());
        assertEquals("http://www.greatschools.net", content.get(0).getLink());
        assertEquals("_blank", content.get(0).getTarget());
        assertEquals("a-class", content.get(0).getStyleClass());
        assertEquals("Some text", content.get(0).getText());
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
        for (TopSchoolsController.TopSchool school : topSchools) {
            assertNotNull(school.getReviewText());
            assertNotNull(school.getTopSchoolCategory());
        }
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
