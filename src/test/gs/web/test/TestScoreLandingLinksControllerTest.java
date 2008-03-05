package gs.web.test;

import gs.web.BaseControllerTestCase;
import gs.web.util.google.GoogleSpreadsheetDao;
import gs.web.util.list.Anchor;
import gs.web.community.CommunityQuestionPromoController;
import static gs.web.community.CommunityQuestionPromoController.WORKSHEET_PRIMARY_ID_COL;
import gs.data.util.table.ITableDao;
import gs.data.util.table.HashMapTableRow;
import gs.data.util.table.ITableRow;
import gs.data.state.StateManager;
import static org.easymock.classextension.EasyMock.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * @author droy <droy@greatschools.net>
 */
public class TestScoreLandingLinksControllerTest  extends BaseControllerTestCase {
    private TestScoreLandingLinksController _controller;
    private ITableDao _dao;
    private StateManager _stateManager;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new TestScoreLandingLinksController();

        _dao = createMock(ITableDao.class);
        _controller.setTableDao(_dao);

        _stateManager = new StateManager();
        _controller.setStateManager(_stateManager);
    }

    public void testLoadSpreadsheetDataWithRowsWithFinder() {
        Map<String, Object> model = new HashMap<String, Object>();
        getRequest().setParameter(TestScoreLandingLinksController.STATE_PARAM, "ca");

        List<ITableRow> rows = new ArrayList<ITableRow>();
        HashMapTableRow row = new HashMapTableRow();
        row.addCell(TestScoreLandingLinksController.WORKSHEET_PRIMARY_ID_COL, "CA");
        row.addCell(TestScoreLandingLinksController.TABLE_DATA_TYPE_ID_COL, "1");
        row.addCell(TestScoreLandingLinksController.TABLE_SHORT_NAME_COL, "Short Test A");
        row.addCell(TestScoreLandingLinksController.TABLE_LONG_NAME_COL, "Long Test A");
        rows.add(row);
        row = new HashMapTableRow();
        row.addCell(TestScoreLandingLinksController.WORKSHEET_PRIMARY_ID_COL, "CA");
        row.addCell(TestScoreLandingLinksController.TABLE_DATA_TYPE_ID_COL, "2");
        row.addCell(TestScoreLandingLinksController.TABLE_SHORT_NAME_COL, "");
        row.addCell(TestScoreLandingLinksController.TABLE_LONG_NAME_COL, "Long Test B");
        rows.add(row);
        row = new HashMapTableRow();
        row.addCell(TestScoreLandingLinksController.WORKSHEET_PRIMARY_ID_COL, "CA");
        row.addCell(TestScoreLandingLinksController.TABLE_DATA_TYPE_ID_COL, "3");
        row.addCell(TestScoreLandingLinksController.TABLE_SHORT_NAME_COL, "Short Test C Ratings");
        row.addCell(TestScoreLandingLinksController.TABLE_LONG_NAME_COL, "Long Test C Ratings");
        rows.add(row);
        row = new HashMapTableRow();
        row.addCell(TestScoreLandingLinksController.WORKSHEET_PRIMARY_ID_COL, "CA");
        row.addCell(TestScoreLandingLinksController.TABLE_DATA_TYPE_ID_COL, "");
        row.addCell(TestScoreLandingLinksController.TABLE_SHORT_NAME_COL, "");
        row.addCell(TestScoreLandingLinksController.TABLE_LONG_NAME_COL, "");
        rows.add(row);

        expect(_dao.getRowsByKey(TestScoreLandingLinksController.WORKSHEET_PRIMARY_ID_COL, "CA")).andReturn(rows);
        replay(_dao);

        _controller.loadSpreadsheetDataIntoModel(model, getRequest());
        verify(_dao);

        assertNotNull(TestScoreLandingLinksController.MODEL_LANDING_LINKS_KEY + " should not be null in the model", model.get(TestScoreLandingLinksController.MODEL_LANDING_LINKS_KEY));
        List<Anchor> anchors = (List<Anchor>)model.get(TestScoreLandingLinksController.MODEL_LANDING_LINKS_KEY);

        assertEquals("Unexpected number of anchors returned,", 4, anchors.size());
        assertEquals("Unexpected anchor text", "Find California schools", anchors.get(0).getContents());

        assertEquals("Unexpected anchor text", "California Short Test A results", anchors.get(1).getContents());
        assertEquals("Unexpected href", "/test/landing.page?state=CA&amp;tid=1", anchors.get(1).getHref());

        assertEquals("Unexpected anchor text", "California Long Test B results", anchors.get(2).getContents());
        assertEquals("Unexpected href", "/test/landing.page?state=CA&amp;tid=2", anchors.get(2).getHref());

        assertEquals("Unexpected anchor text", "California Short Test C Ratings", anchors.get(3).getContents());
        assertEquals("Unexpected href", "/test/landing.page?state=CA&amp;tid=3", anchors.get(3).getHref());
    }

    public void testLoadSpreadsheetDataWithNoRowsWithFinder() {
        loadSpreadsheetDataWithNoRows(true);
    }

    public void testLoadSpreadsheetDataWithNoRowsWithoutFinder() {
        loadSpreadsheetDataWithNoRows(false);
    }

    protected void loadSpreadsheetDataWithNoRows(boolean showFinder) {
        Map<String, Object> model = new HashMap<String, Object>();
        getRequest().setParameter(TestScoreLandingLinksController.STATE_PARAM, "ca");
        if (!showFinder) {
            getRequest().setParameter(TestScoreLandingLinksController.SHOW_FINDER_PARAM, "false");
        }

        expect(_dao.getRowsByKey(TestScoreLandingLinksController.WORKSHEET_PRIMARY_ID_COL, "CA")).andReturn(null);
        replay(_dao);

        _controller.loadSpreadsheetDataIntoModel(model, getRequest());

        verify(_dao);

        assertNotNull(TestScoreLandingLinksController.MODEL_LANDING_LINKS_KEY + " should not be null in the model", model.get(TestScoreLandingLinksController.MODEL_LANDING_LINKS_KEY));
        List<Anchor> anchors = (List<Anchor>)model.get(TestScoreLandingLinksController.MODEL_LANDING_LINKS_KEY);

        int expectedAnchors;
        if (showFinder) {
            expectedAnchors = 1;
        } else {
            expectedAnchors = 0;
        }
        assertEquals("Unexpected number of anchors returned,", expectedAnchors, anchors.size());
        if (expectedAnchors > 0) {
            assertEquals("Unexpected anchor text", "Find California schools", anchors.get(0).getContents());
        }
    }
}
