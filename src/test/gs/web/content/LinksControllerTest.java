package gs.web.content;

import gs.web.BaseControllerTestCase;
import gs.data.util.google.GoogleSpreadsheetDao;
import gs.data.util.google.GoogleSpreadsheetInfo;
import gs.web.util.list.AnchorListModel;
import gs.web.util.list.Anchor;
import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableRow;
import gs.data.util.table.HashMapTableRow;

import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.reset;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.ArrayList;

/**
 * @author <a href="yfan@greatschools.org">Young Fan</a>
 */
public class LinksControllerTest extends BaseControllerTestCase {

    private LinksController _controller;
    private ITableDao _tableDao;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new LinksController();
        _tableDao = createMock(GoogleSpreadsheetDao.class);
        _controller.setTableDao(_tableDao);
    }

    public void testBasics() {
        assertSame("Table dao was not the same object", _tableDao, _controller.getTableDao());
    }

    public void testIsValidRequest() throws Exception {
        // all required parameters not specified
        getRequest().removeParameter(LinksController.PARAM_PAGE);
        getRequest().removeParameter(LinksController.PARAM_TYPE);
        getRequest().removeParameter(LinksController.PARAM_LAYOUT);
        ModelAndView modelAndView = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNull("Returned ModelAndView was not null when PARAM_PAGE and PARAM_TYPE not parameters in request", modelAndView);

        // one of required parameters not specified
        getRequest().setParameter(LinksController.PARAM_PAGE, "backtoschool");
        getRequest().setParameter(LinksController.PARAM_TYPE, LinksController.TYPE_ALL);
        getRequest().removeParameter(LinksController.PARAM_LAYOUT);
        modelAndView = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNull("Returned ModelAndView was not null when PARAM_LAYOUT not parameter in request", modelAndView);

        getRequest().removeParameter(LinksController.PARAM_PAGE);
        getRequest().setParameter(LinksController.PARAM_TYPE, LinksController.TYPE_ALL);
        getRequest().setParameter(LinksController.PARAM_LAYOUT, LinksController.LAYOUT_BASIC);
        modelAndView = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNull("Returned ModelAndView was not null when PARAM_PAGE not parameter in request", modelAndView);

        getRequest().setParameter(LinksController.PARAM_PAGE, "backtoschool");
        getRequest().removeParameter(LinksController.PARAM_TYPE);
        getRequest().setParameter(LinksController.PARAM_LAYOUT, LinksController.LAYOUT_BASIC);
        modelAndView = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNull("Returned ModelAndView was not null when PARAM_TYPE not parameter in request", modelAndView);

        // all required parameters present but PARAM_TYPE invalid
        getRequest().setParameter(LinksController.PARAM_PAGE, "backtoschool");
        getRequest().setParameter(LinksController.PARAM_TYPE, "");
        getRequest().setParameter(LinksController.PARAM_LAYOUT, LinksController.LAYOUT_BASIC);
        modelAndView = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNull("Returned ModelAndView was not null when required parameters present but PARAM_TYPE invalid", modelAndView);

        getRequest().setParameter(LinksController.PARAM_PAGE, "backtoschool");
        getRequest().setParameter(LinksController.PARAM_TYPE, LinksController.TYPE_ALL);
        getRequest().setParameter(LinksController.PARAM_LAYOUT, "");
        modelAndView = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNull("Returned ModelAndView was not null when required parameters present but PARAM_LAYOUT invalid", modelAndView);
    }

    public void testHandleRequestInternalTypeFirst() throws Exception {
        getRequest().setParameter(LinksController.PARAM_PAGE, "backtoschool");
        getRequest().setParameter(LinksController.PARAM_TYPE, LinksController.TYPE_FIRST);
        getRequest().setParameter(LinksController.PARAM_LAYOUT, LinksController.LAYOUT_BASIC);

        GoogleSpreadsheetDao dao = (GoogleSpreadsheetDao) _tableDao;
        getRequest().setServerName("dev.greatschools.org");
        expect(dao.getSpreadsheetInfo()).andReturn(new GoogleSpreadsheetInfo(null,null,null,"od6"));

        HashMapTableRow hashMapTableRow = new HashMapTableRow();
        hashMapTableRow.addCell(LinksController.SPREADSHEET_TEXT, "GreatSchools.org");
        hashMapTableRow.addCell(LinksController.SPREADSHEET_URL, "http://www.greatschools.org");
        expect(dao.getFirstRowByKey(LinksController.SPREADSHEET_PAGE,
            getRequest().getParameter(LinksController.PARAM_PAGE))).andReturn(hashMapTableRow);
        replay(dao);

        ModelAndView modelAndView = _controller.handleRequestInternal(getRequest(), getResponse());

        assertEquals("View name did not match " + LinksController.VIEW_NAME,
            LinksController.VIEW_NAME, modelAndView.getViewName());
        assertEquals("Layout did not match " + LinksController.LAYOUT_BASIC,
            LinksController.LAYOUT_BASIC, modelAndView.getModel().get(LinksController.MODEL_LAYOUT));
        assertTrue("Model did not contain anchor",
            modelAndView.getModel().containsKey(LinksController.MODEL_ANCHOR));
        assertTrue("Model anchor was not Anchor object",
            modelAndView.getModel().get(LinksController.MODEL_ANCHOR) instanceof Anchor);

        Anchor anchor = (Anchor) modelAndView.getModel().get(LinksController.MODEL_ANCHOR);
        assertEquals("Anchor href was not GreatSchools.org", "GreatSchools.org", anchor.getContents());
        assertEquals("Anchor href was not http://www.greatschools.org", "http://www.greatschools.org", anchor.getHref());

        reset(dao);

        expect(dao.getSpreadsheetInfo()).andReturn(new GoogleSpreadsheetInfo(null,null,null,"od6"));
        expect(dao.getFirstRowByKey(LinksController.SPREADSHEET_PAGE,
            getRequest().getParameter(LinksController.PARAM_PAGE))).andReturn(null);
        replay(dao);

        modelAndView = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNull("Expected null anchor", modelAndView.getModel().get(LinksController.MODEL_ANCHOR));

        verify(dao);
    }

    public void testHandleRequestInternalTypeRandom() throws Exception {
        getRequest().setParameter(LinksController.PARAM_PAGE, "backtoschool");
        getRequest().setParameter(LinksController.PARAM_TYPE, LinksController.TYPE_RANDOM);
        getRequest().setParameter(LinksController.PARAM_LAYOUT, LinksController.LAYOUT_BASIC);

        GoogleSpreadsheetDao dao = (GoogleSpreadsheetDao) _tableDao;
        getRequest().setServerName("dev.greatschools.org");
        expect(dao.getSpreadsheetInfo()).andReturn(new GoogleSpreadsheetInfo(null,null,null,"od6"));

        HashMapTableRow hashMapTableRow = new HashMapTableRow();
        hashMapTableRow.addCell(LinksController.SPREADSHEET_TEXT, "GreatSchools.org");
        hashMapTableRow.addCell(LinksController.SPREADSHEET_URL, "http://www.greatschools.org");
        expect(dao.getRandomRowByKey(LinksController.SPREADSHEET_PAGE,
            getRequest().getParameter(LinksController.PARAM_PAGE))).andReturn(hashMapTableRow);
        replay(dao);

        ModelAndView modelAndView = _controller.handleRequestInternal(getRequest(), getResponse());

        assertEquals("View name did not match " + LinksController.VIEW_NAME,
            LinksController.VIEW_NAME, modelAndView.getViewName());
        assertEquals("Layout did not match " + LinksController.LAYOUT_BASIC,
            LinksController.LAYOUT_BASIC, modelAndView.getModel().get(LinksController.MODEL_LAYOUT));
        assertTrue("Model did not contain anchor",
            modelAndView.getModel().containsKey(LinksController.MODEL_ANCHOR));
        assertTrue("Model anchor was not Anchor object",
            modelAndView.getModel().get(LinksController.MODEL_ANCHOR) instanceof Anchor);

        Anchor anchor = (Anchor) modelAndView.getModel().get(LinksController.MODEL_ANCHOR);
        assertEquals("Anchor href was not GreatSchools.org", "GreatSchools.org", anchor.getContents());
        assertEquals("Anchor href was not http://www.greatschools.org", "http://www.greatschools.org", anchor.getHref());

        reset(dao);

        expect(dao.getSpreadsheetInfo()).andReturn(new GoogleSpreadsheetInfo(null,null,null,"od6"));
        expect(dao.getRandomRowByKey(LinksController.SPREADSHEET_PAGE,
            getRequest().getParameter(LinksController.PARAM_PAGE))).andReturn(null);
        replay(dao);

        modelAndView = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNull("Expected null anchor", modelAndView.getModel().get(LinksController.MODEL_ANCHOR));

        verify(dao);
    }

    public void testHandleRequestInternalTypeAll() throws Exception {
        getRequest().setParameter(LinksController.PARAM_PAGE, "backtoschool");
        getRequest().setParameter(LinksController.PARAM_TYPE, LinksController.TYPE_ALL);
        getRequest().setParameter(LinksController.PARAM_LAYOUT, LinksController.LAYOUT_BASIC);

        GoogleSpreadsheetDao dao = (GoogleSpreadsheetDao) _tableDao;
        getRequest().setServerName("dev.greatschools.org");
        expect(dao.getSpreadsheetInfo()).andReturn(new GoogleSpreadsheetInfo(null,null,null,"od6"));

        List<ITableRow> tableRowList = new ArrayList<ITableRow>();
        HashMapTableRow hashMapTableRow = new HashMapTableRow();
        hashMapTableRow.addCell(LinksController.SPREADSHEET_TEXT, "GreatSchools.org");
        hashMapTableRow.addCell(LinksController.SPREADSHEET_URL, "http://www.greatschools.org");
        tableRowList.add(hashMapTableRow);
        expect(dao.getRowsByKey(LinksController.SPREADSHEET_PAGE,
            getRequest().getParameter(LinksController.PARAM_PAGE))).andReturn(tableRowList);
        replay(dao);

        ModelAndView modelAndView = _controller.handleRequestInternal(getRequest(), getResponse());

        assertEquals("View name did not match " + LinksController.VIEW_NAME,
            LinksController.VIEW_NAME, modelAndView.getViewName());
        assertEquals("Layout did not match " + LinksController.LAYOUT_BASIC,
            LinksController.LAYOUT_BASIC, modelAndView.getModel().get(LinksController.MODEL_LAYOUT));
        assertTrue("Model did not contain anchor list key",
            modelAndView.getModel().containsKey(LinksController.MODEL_ANCHOR_LIST));
        assertTrue("Model anchor list was not AnchorListModel object",
            modelAndView.getModel().get(LinksController.MODEL_ANCHOR_LIST) instanceof AnchorListModel);

        AnchorListModel anchorListModel =
            (AnchorListModel) modelAndView.getModel().get(LinksController.MODEL_ANCHOR_LIST);
        Anchor firstAnchor = (Anchor) anchorListModel.getResults().get(0);
        assertEquals("Anchor href was not GreatSchools.org", "GreatSchools.org", firstAnchor.getContents());
        assertEquals("Anchor href was not http://www.greatschools.org", "http://www.greatschools.org", firstAnchor.getHref());

        assertNull("Before text should be null", firstAnchor.getBefore());
        assertNull("After text should be null", firstAnchor.getAfter());
        
        reset(dao);

        expect(dao.getSpreadsheetInfo()).andReturn(new GoogleSpreadsheetInfo(null,null,null,"od6"));
        expect(dao.getRowsByKey(LinksController.SPREADSHEET_PAGE,
            getRequest().getParameter(LinksController.PARAM_PAGE))).andReturn(null);
        replay(dao);

        modelAndView = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNull("Expected null anchor list", modelAndView.getModel().get(LinksController.MODEL_ANCHOR_LIST));

        verify(dao);
    }

    public void testInjectWorksheetName() {
        LinksController controller = (LinksController) getApplicationContext().getBean(LinksController.BEAN_ID);
        GoogleSpreadsheetDao dao = (GoogleSpreadsheetDao) controller.getTableDao();

        getRequest().setServerName("dev.greatschools.org");
        controller.injectWorksheetName(getRequest());
        assertEquals(controller.getDevWorksheetName(), dao.getSpreadsheetInfo().getWorksheetName());

        getRequest().setServerName("staging.greatschools.org");
        controller.injectWorksheetName(getRequest());
        assertEquals(controller.getStagingWorksheetName(), dao.getSpreadsheetInfo().getWorksheetName());

        getRequest().setServerName("www.greatschools.org");
        controller.injectWorksheetName(getRequest());
        assertEquals(controller.getProductionWorksheetName(), dao.getSpreadsheetInfo().getWorksheetName());
    }

    public void testGetWorksheet() {
        LinksController controller = (LinksController) getApplicationContext().getBean(LinksController.BEAN_ID);
        getRequest().setServerName("dev.greatschools.org");
        assertEquals("Worksheet name was not dev worksheet name for dev server name",
            controller.getDevWorksheetName(), controller.getWorksheet(getRequest()));

        getRequest().setServerName("staging.greatschools.org");
        assertEquals("Worksheet name was not staging worksheet name for staging server name",
            controller.getStagingWorksheetName(), controller.getWorksheet(getRequest()));

        getRequest().setServerName("www.greatschools.org");
        assertEquals("Worksheet name was not production worksheet name for production server name",
            controller.getProductionWorksheetName(), controller.getWorksheet(getRequest()));
    }

    public void testBeforeAndAfterText () throws Exception {
        getRequest().setParameter(LinksController.PARAM_PAGE, "latest_tools");
        getRequest().setParameter(LinksController.PARAM_TYPE, LinksController.TYPE_ALL);
        getRequest().setParameter(LinksController.PARAM_LAYOUT, LinksController.LAYOUT_BASIC);

        GoogleSpreadsheetDao dao = (GoogleSpreadsheetDao) _tableDao;
        getRequest().setServerName("dev.greatschools.org");
        expect(dao.getSpreadsheetInfo()).andReturn(new GoogleSpreadsheetInfo(null,null,null,"od6"));

        List<ITableRow> tableRowList = new ArrayList<ITableRow>();
        HashMapTableRow hashMapTableRow = new HashMapTableRow();
        hashMapTableRow.addCell(LinksController.SPREADSHEET_TEXT, "GreatSchools.org");
        hashMapTableRow.addCell(LinksController.SPREADSHEET_URL, "http://www.greatschools.org");
        hashMapTableRow.addCell(LinksController.SPREADSHEET_BEFORE, "before");
        hashMapTableRow.addCell(LinksController.SPREADSHEET_AFTER, "after");
        tableRowList.add(hashMapTableRow);
        expect(dao.getRowsByKey(LinksController.SPREADSHEET_PAGE,
            getRequest().getParameter(LinksController.PARAM_PAGE))).andReturn(tableRowList);
        replay(dao);

        ModelAndView modelAndView = _controller.handleRequestInternal(getRequest(), getResponse());

        assertEquals("View name did not match " + LinksController.VIEW_NAME,
            LinksController.VIEW_NAME, modelAndView.getViewName());
        assertEquals("Layout did not match " + LinksController.LAYOUT_BASIC,
            LinksController.LAYOUT_BASIC, modelAndView.getModel().get(LinksController.MODEL_LAYOUT));
        assertTrue("Model did not contain anchor list key",
            modelAndView.getModel().containsKey(LinksController.MODEL_ANCHOR_LIST));
        assertTrue("Model anchor list was not AnchorListModel object",
            modelAndView.getModel().get(LinksController.MODEL_ANCHOR_LIST) instanceof AnchorListModel);

        AnchorListModel anchorListModel =
            (AnchorListModel) modelAndView.getModel().get(LinksController.MODEL_ANCHOR_LIST);
        Anchor firstAnchor = (Anchor) anchorListModel.getResults().get(0);
        assertEquals("Anchor href was not GreatSchools.org", "GreatSchools.org", firstAnchor.getContents());
        assertEquals("Anchor href was not http://www.greatschools.org", "http://www.greatschools.org", firstAnchor.getHref());

        assertEquals("Before text should be \"before\"", "before", firstAnchor.getBefore());
        assertEquals("After text should be \"after\"", "after", firstAnchor.getAfter());

        reset(dao);
                                                                                        
        expect(dao.getSpreadsheetInfo()).andReturn(new GoogleSpreadsheetInfo(null,null,null,"od6"));
        expect(dao.getRowsByKey(LinksController.SPREADSHEET_PAGE,
            getRequest().getParameter(LinksController.PARAM_PAGE))).andReturn(null);
        replay(dao);

        modelAndView = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNull("Expected null anchor list", modelAndView.getModel().get(LinksController.MODEL_ANCHOR_LIST));

        verify(dao);
    }

    public void testHandleRequestInternalTypeImage() throws Exception {
        getRequest().setParameter(LinksController.PARAM_PAGE, "library_HotTopics_image");
        getRequest().setParameter(LinksController.PARAM_TYPE, LinksController.TYPE_IMAGE);
        getRequest().setParameter(LinksController.PARAM_LAYOUT, LinksController.LAYOUT_IMAGE);

        GoogleSpreadsheetDao dao = (GoogleSpreadsheetDao) _tableDao;
        getRequest().setServerName("dev.greatschools.org");
        expect(dao.getSpreadsheetInfo()).andReturn(new GoogleSpreadsheetInfo(null,null,null,"od6"));

        HashMapTableRow hashMapTableRow = new HashMapTableRow();
        hashMapTableRow.addCell(LinksController.SPREADSHEET_TEXT, "GreatSchools.org");
        hashMapTableRow.addCell(LinksController.SPREADSHEET_URL, "http://www.greatschools.org");
        expect(dao.getFirstRowByKey(LinksController.SPREADSHEET_PAGE,
            getRequest().getParameter(LinksController.PARAM_PAGE))).andReturn(hashMapTableRow);
        replay(dao);

        ModelAndView modelAndView = _controller.handleRequestInternal(getRequest(), getResponse());

        assertEquals("View name did not match " + LinksController.VIEW_NAME,
            LinksController.VIEW_NAME, modelAndView.getViewName());
        assertEquals("Layout did not match " + LinksController.LAYOUT_IMAGE,
            LinksController.LAYOUT_IMAGE, modelAndView.getModel().get(LinksController.MODEL_LAYOUT));
        assertTrue("Model did not contain anchor",
            modelAndView.getModel().containsKey(LinksController.MODEL_ANCHOR));
        assertTrue("Model anchor was not Anchor object",
            modelAndView.getModel().get(LinksController.MODEL_ANCHOR) instanceof Anchor);

        Anchor anchor = (Anchor) modelAndView.getModel().get(LinksController.MODEL_ANCHOR);
        assertEquals("Anchor href was not GreatSchools.org", "GreatSchools.org", anchor.getContents());
        assertEquals("Anchor href was not http://www.greatschools.org", "http://www.greatschools.org", anchor.getHref());

        reset(dao);

        expect(dao.getSpreadsheetInfo()).andReturn(new GoogleSpreadsheetInfo(null,null,null,"od6"));
        expect(dao.getFirstRowByKey(LinksController.SPREADSHEET_PAGE,
            getRequest().getParameter(LinksController.PARAM_PAGE))).andReturn(null);
        replay(dao);

        modelAndView = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNull("Expected null anchor", modelAndView.getModel().get(LinksController.MODEL_ANCHOR));

        verify(dao);
    }
}
