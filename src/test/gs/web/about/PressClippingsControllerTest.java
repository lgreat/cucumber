package gs.web.about;

import gs.web.BaseControllerTestCase;
import gs.data.util.google.GoogleSpreadsheetDao;
import gs.data.util.google.GoogleSpreadsheetInfo;
import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableRow;
import gs.data.util.table.HashMapTableRow;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.ArrayList;

/**
 * This class tests the fetching of press clippings entries from a Google Spreadsheet.
 * @author <a href="yfan@greatschools.org">Young Fan</a>
 */
public class PressClippingsControllerTest extends BaseControllerTestCase {

    private PressClippingsController _controller;
    private ITableDao _tableDao;

    final private static HashMapTableRow _row1 = new HashMapTableRow();
    final private static HashMapTableRow _row2 = new HashMapTableRow();
    final private static HashMapTableRow _row3 = new HashMapTableRow();
    final private static List<ITableRow> _tableRowList = new ArrayList<ITableRow>();

    static {
        _row1.addCell(PressClippingsController.SPREADSHEET_TEXT, "Item 1");
        _row1.addCell(PressClippingsController.SPREADSHEET_URL, "http://www.greatschools.org");
        _row1.addCell(PressClippingsController.SPREADSHEET_DATE, "1/1/2008");
        _row1.addCell(PressClippingsController.SPREADSHEET_SOURCE, "Source");

        _row2.addCell(PressClippingsController.SPREADSHEET_TEXT, "Item 2");
        _row2.addCell(PressClippingsController.SPREADSHEET_URL, "http://www.greatschools.org");
        _row2.addCell(PressClippingsController.SPREADSHEET_DATE, "1/1/2008");
        _row2.addCell(PressClippingsController.SPREADSHEET_SOURCE, "Source");

        _row3.addCell(PressClippingsController.SPREADSHEET_TEXT, "Item 3");
        _row3.addCell(PressClippingsController.SPREADSHEET_URL, "http://www.greatschools.org");
        _row3.addCell(PressClippingsController.SPREADSHEET_DATE, "1/1/2008");
        _row3.addCell(PressClippingsController.SPREADSHEET_SOURCE, "Source");

        _tableRowList.add(_row1);
        _tableRowList.add(_row2);
        _tableRowList.add(_row3);
    }

    protected void setUp() throws Exception {
        super.setUp();
        _controller = new PressClippingsController();
        _tableDao = createMock(GoogleSpreadsheetDao.class);
        _controller.setTableDao(_tableDao);
    }

    public void testBasics() {
        assertSame("Table dao was not the same object", _tableDao, _controller.getTableDao());
    }


    public void testHandleRequestInternalFirstN() throws Exception {
        int numResults = 2;
        getRequest().setParameter(PressClippingsController.PARAM_FIRST, String.valueOf(numResults));

        GoogleSpreadsheetDao dao = (GoogleSpreadsheetDao) _tableDao;
        getRequest().setServerName("dev.greatschools.org");
        expect(dao.getSpreadsheetInfo()).andReturn(new GoogleSpreadsheetInfo(null,null,null,"od6"));
        expect(dao.getAllRows()).andReturn(_tableRowList);
        replay(dao);

        ModelAndView modelAndView = _controller.handleRequestInternal(getRequest(), getResponse());

        helperHandleRequestInternal(modelAndView);

        List<PressClipping> clippings =
                (List<PressClipping>) modelAndView.getModel().get(PressClippingsController.MODEL_PRESS_CLIPPINGS);
        assertEquals("Expected length of list to be " + numResults, numResults, clippings.size());
        assertEquals("Expected row 1 to match first row in results",
            _row1.getString(PressClippingsController.SPREADSHEET_TEXT), clippings.get(0).getText());
        assertEquals("Expected row 2 to match second row in results",
            _row2.getString(PressClippingsController.SPREADSHEET_TEXT), clippings.get(1).getText());

        verify(dao);
    }

    public void testHandleRequestInternalAll() throws Exception {
        getRequest().removeParameter(PressClippingsController.PARAM_FIRST);

        GoogleSpreadsheetDao dao = (GoogleSpreadsheetDao) _tableDao;
        getRequest().setServerName("dev.greatschools.org");
        expect(dao.getSpreadsheetInfo()).andReturn(new GoogleSpreadsheetInfo(null,null,null,"od6"));

        expect(dao.getAllRows()).andReturn(_tableRowList);
        replay(dao);

        ModelAndView modelAndView = _controller.handleRequestInternal(getRequest(), getResponse());

        helperHandleRequestInternal(modelAndView);

        List<PressClipping> clippings =
                (List<PressClipping>) modelAndView.getModel().get(PressClippingsController.MODEL_PRESS_CLIPPINGS);
        assertEquals("Expected length of list to be " + _tableRowList.size(), _tableRowList.size(), clippings.size());
        assertEquals("Expected row 1 to match first row in results",
            _row1.getString(PressClippingsController.SPREADSHEET_TEXT), clippings.get(0).getText());
        assertEquals("Expected row 2 to match second row in results",
            _row2.getString(PressClippingsController.SPREADSHEET_TEXT), clippings.get(1).getText());
        assertEquals("Expected row 3 to match third row in results",
            _row3.getString(PressClippingsController.SPREADSHEET_TEXT), clippings.get(2).getText());

        verify(dao);
    }

    private void helperHandleRequestInternal(ModelAndView modelAndView) {
        assertEquals("View name did not match " + PressClippingsController.VIEW_NAME,
            PressClippingsController.VIEW_NAME, modelAndView.getViewName());
        assertTrue("Model did not contain press releases key",
            modelAndView.getModel().containsKey(PressClippingsController.MODEL_PRESS_CLIPPINGS));
        // instanceof doesn't work with generics, otherwise we'd check List<PressClipping>
        // http://javanotepad.blogspot.com/2007/09/instanceof-doesnt-work-with-generics.html
        assertTrue("Model press clippings list was not List object",
            modelAndView.getModel().get(PressClippingsController.MODEL_PRESS_CLIPPINGS) instanceof List);        
    }

    public void testInjectWorksheetName() {
        PressClippingsController controller = (PressClippingsController) getApplicationContext().getBean(PressClippingsController.BEAN_ID);
        GoogleSpreadsheetDao dao = (GoogleSpreadsheetDao) controller.getTableDao();

        getRequest().setServerName("dev.greatschools.org");
        controller.injectWorksheetName(getRequest());
        assertEquals(controller.getInternalWorksheetName(), dao.getSpreadsheetInfo().getWorksheetName());

        getRequest().setServerName("staging.greatschools.org");
        controller.injectWorksheetName(getRequest());
        assertEquals(controller.getInternalWorksheetName(), dao.getSpreadsheetInfo().getWorksheetName());

        getRequest().setServerName("www.greatschools.org");
        controller.injectWorksheetName(getRequest());
        assertEquals(controller.getProductionWorksheetName(), dao.getSpreadsheetInfo().getWorksheetName());
    }

    public void testGetWorksheet() {
        PressClippingsController controller = (PressClippingsController) getApplicationContext().getBean(PressClippingsController.BEAN_ID);
        getRequest().setServerName("dev.greatschools.org");
        assertEquals("Worksheet name was not internal worksheet name for dev server name",
            controller.getInternalWorksheetName(), controller.getWorksheet(getRequest()));

        getRequest().setServerName("staging.greatschools.org");
        assertEquals("Worksheet name was not internal worksheet name for staging server name",
            controller.getInternalWorksheetName(), controller.getWorksheet(getRequest()));

        getRequest().setServerName("www.greatschools.org");
        assertEquals("Worksheet name was not production worksheet name for production server name",
            controller.getProductionWorksheetName(), controller.getWorksheet(getRequest()));
    }
}
