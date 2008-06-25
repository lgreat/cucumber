package gs.web.content;

import gs.web.BaseControllerTestCase;
import gs.web.content.MicrositePromoController;
import gs.web.util.google.GoogleSpreadsheetDao;
import gs.web.util.list.AnchorListModel;
import gs.web.util.list.Anchor;
import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableRow;
import gs.data.util.table.HashMapTableRow;
//import static org.easymock.EasyMock.*;

import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.reset;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.ArrayList;

/**
 * @author <a href="yfan@greatschools.net">Young Fan</a>
 */
public class MicrositePromoControllerTest extends BaseControllerTestCase {

    private MicrositePromoController _controller;
    private ITableDao _tableDao;

    protected void setUp() throws Exception {
        super.setUp();
        _controller = (MicrositePromoController) getApplicationContext().getBean(MicrositePromoController.BEAN_ID);
        _tableDao = createMock(GoogleSpreadsheetDao.class);
        _controller.setTableDao(_tableDao);
    }

    public void testBasics() {
        assertSame("Table DAO was not the same object", _tableDao, _controller.getTableDao());
    }

    public void testHandleRequestInternal() throws Exception {
        // PARAM_PAGE not specified
        getRequest().removeParameter(MicrositePromoController.PARAM_PAGE);
        ModelAndView modelAndView = _controller.handleRequestInternal(getRequest(), getResponse());
        assertNull("Returned ModelAndView was not null when PARAM_PAGE not a parameter in request", modelAndView);

        // PARAM_PAGE specified
        getRequest().setParameter(MicrositePromoController.PARAM_PAGE, "backtoschool");

        GoogleSpreadsheetDao dao = (GoogleSpreadsheetDao) _tableDao;
        getRequest().setServerName("dev.greatschools.net");
        expect(dao.getWorksheetUrl()).andReturn("google/");
        dao.setWorksheetUrl("google/od6");

        List<ITableRow> tableRowList = new ArrayList<ITableRow>();
        HashMapTableRow hashMapTableRow = new HashMapTableRow();
        hashMapTableRow.addCell(MicrositePromoController.SPREADSHEET_TEXT, "GreatSchools.net");
        hashMapTableRow.addCell(MicrositePromoController.SPREADSHEET_URL, "http://www.greatschools.net");
        tableRowList.add(hashMapTableRow);
        expect(dao.getRowsByKey(MicrositePromoController.SPREADSHEET_PAGE,
            getRequest().getParameter(MicrositePromoController.PARAM_PAGE))).andReturn(tableRowList);
        replay(dao);

        modelAndView = _controller.handleRequestInternal(getRequest(), getResponse());

        assertEquals("View name did not match " + MicrositePromoController.VIEW_NAME,
            MicrositePromoController.VIEW_NAME, modelAndView.getViewName());
        assertTrue("Model did not contain anchor list key",
            modelAndView.getModel().containsKey(MicrositePromoController.MODEL_ANCHOR_LIST));
        assertTrue("Model anchor list was not AnchorListModel object",
            modelAndView.getModel().get(MicrositePromoController.MODEL_ANCHOR_LIST) instanceof AnchorListModel);

        AnchorListModel anchorListModel =
            (AnchorListModel) modelAndView.getModel().get(MicrositePromoController.MODEL_ANCHOR_LIST);
        Anchor firstAnchor = (Anchor) anchorListModel.getResults().get(0);
        assertEquals("Anchor href was not GreatSchools.net", "GreatSchools.net", firstAnchor.getContents());
        assertEquals("Anchor href was not http://www.greatschools.net", "http://www.greatschools.net", firstAnchor.getHref());

        verify(dao);
    }

    public void testInjectWorksheetName() {
        GoogleSpreadsheetDao dao = (GoogleSpreadsheetDao) _tableDao;

        getRequest().setServerName("dev.greatschools.net");
        expect(dao.getWorksheetUrl()).andReturn("google/");
        dao.setWorksheetUrl("google/" + _controller.getDevWorksheetName());
        replay(dao);

        _controller.injectWorksheetName(getRequest());
        verify(dao);

        reset(dao);

        getRequest().setServerName("staging.greatschools.net");
        expect(dao.getWorksheetUrl()).andReturn("google/");
        dao.setWorksheetUrl("google/" + _controller.getStagingWorksheetName());
        replay(dao);

        _controller.injectWorksheetName(getRequest());
        verify(dao);

        reset(dao);

        getRequest().setServerName("www.greatschools.net");
        expect(dao.getWorksheetUrl()).andReturn("google/");
        dao.setWorksheetUrl("google/" + _controller.getProductionWorksheetName());
        replay(dao);

        _controller.injectWorksheetName(getRequest());
        verify(dao);

        reset(dao);

        getRequest().setServerName("dev.greatschools.net");
        expect(dao.getWorksheetUrl()).andReturn("google/" + _controller.getDevWorksheetName());
        replay(dao);

        _controller.injectWorksheetName(getRequest());
        verify(dao);
    }

    public void testGetWorksheet() {
        getRequest().setServerName("dev.greatschools.net");
        assertEquals("Worksheet name was not dev worksheet name for dev server name",
            _controller.getDevWorksheetName(), _controller.getWorksheet(getRequest()));

        getRequest().setServerName("staging.greatschools.net");
        assertEquals("Worksheet name was not staging worksheet name for staging server name",
            _controller.getStagingWorksheetName(), _controller.getWorksheet(getRequest()));

        getRequest().setServerName("www.greatschools.net");
        assertEquals("Worksheet name was not production worksheet name for production server name",
            _controller.getProductionWorksheetName(), _controller.getWorksheet(getRequest()));
    }
}
