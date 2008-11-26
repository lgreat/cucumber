package gs.web.util.google;

import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableRow;

import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.easymock.classextension.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author thuss
 */
public class GoogleSpreadsheetControllerTest extends BaseControllerTestCase {

    private GoogleSpreadsheetController _controller;
    private ITableDao _tableDao;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new GoogleSpreadsheetController();
        _tableDao = createMock(GoogleSpreadsheetDao.class);
        _controller.setTableDao(_tableDao);
    }

    public void testGoogleSpreadsheetController() throws Exception {
        List<ITableRow> rows = new ArrayList<ITableRow>();
        expect(_tableDao.getAllRows()).andReturn(rows);
        replay(_tableDao);
        GsMockHttpServletRequest request = getRequest();
        ModelAndView mv = _controller.handleRequestInternal(request, getResponse());
        assertEquals(rows, mv.getModel().get(GoogleSpreadsheetController.MODEL_ROWS));
        verify(_tableDao);
    }

}
