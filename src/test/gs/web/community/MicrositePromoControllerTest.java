package gs.web.community;

import gs.web.BaseControllerTestCase;
import static gs.web.community.CommunityQuestionPromoController.WORKSHEET_PRIMARY_ID_COL;
import static gs.web.community.CommunityQuestionPromoController.DEFAULT_CODE;
import gs.web.util.google.GoogleSpreadsheetDao;
import gs.web.util.list.AnchorListModel;
import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableRow;
//import static org.easymock.EasyMock.*;

import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.easymock.classextension.EasyMock.expect;
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

        verify(dao);
    }
}
