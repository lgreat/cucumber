package gs.web.community;

import gs.web.BaseControllerTestCase;
import gs.web.util.google.IGoogleSpreadsheetDao;
import gs.web.util.google.GoogleSpreadsheetFactory;
import gs.web.util.google.SpreadsheetRow;

import java.util.Map;
import java.util.HashMap;

import static gs.web.community.CommunityQuestionPromoController.*;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CommunityQuestionPromoControllerTest extends BaseControllerTestCase {
    private CommunityQuestionPromoController _controller;
    private GoogleSpreadsheetFactory _factory;
    private IGoogleSpreadsheetDao _dao;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new CommunityQuestionPromoController();

        _factory = createMock(GoogleSpreadsheetFactory.class);
        _dao = createMock(IGoogleSpreadsheetDao.class);
        _controller.setGoogleSpreadsheetFactory(_factory);
    }

    public void testBasic() {
        assertSame(_factory, _controller.getGoogleSpreadsheetFactory());
        _controller.setViewName("aView");
        assertEquals("aView", _controller.getViewName());
    }

    public void testLoadSpreadsheetData() {
        Map<String, Object> model = new HashMap<String, Object>();

        SpreadsheetRow row = new SpreadsheetRow();
        row.addCell(WORKSHEET_PRIMARY_ID_COL, "someKey");
        row.addCell("text", "text text");
        row.addCell("link", "link link");
        row.addCell("username", "user name");
        row.addCell("memberid", "member id");

        expect(_factory.getGoogleSpreadsheetDao()).andReturn(_dao);
        replay(_factory);

        expect(_dao.getFirstRowByKey(WORKSHEET_PRIMARY_ID_COL, "someKey")).andReturn(row);
        replay(_dao);

        _controller.loadSpreadsheetDataIntoModel(model, "someKey");
        verify(_factory);
        verify(_dao);

        assertEquals("text text", model.get(MODEL_QUESTION_TEXT));
        assertEquals("link link", model.get(MODEL_QUESTION_LINK));
        assertEquals("user name", model.get(MODEL_USERNAME));
        assertEquals("member id", model.get(MODEL_USER_ID));
    }

    public void testLoadSpreadsheetDataEmpty() {
        // shouldn't crash
        Map<String, Object> model = new HashMap<String, Object>();

        SpreadsheetRow row = new SpreadsheetRow();

        expect(_factory.getGoogleSpreadsheetDao()).andReturn(_dao);
        replay(_factory);

        expect(_dao.getFirstRowByKey(WORKSHEET_PRIMARY_ID_COL, "someKey")).andReturn(row);
        replay(_dao);

        _controller.loadSpreadsheetDataIntoModel(model, "someKey");
        verify(_factory);
        verify(_dao);

        assertNull(model.get(MODEL_QUESTION_TEXT));
        assertNull(model.get(MODEL_QUESTION_LINK));
        assertNull(model.get(MODEL_USERNAME));
        assertNull(model.get(MODEL_USER_ID));
    }

    public void testLoadSpreadsheetDataNull() {
        // shouldn't crash
        Map<String, Object> model = new HashMap<String, Object>();

        expect(_factory.getGoogleSpreadsheetDao()).andReturn(_dao);
        replay(_factory);

        expect(_dao.getFirstRowByKey(WORKSHEET_PRIMARY_ID_COL, "someKey")).andReturn(null);
        replay(_dao);

        _controller.loadSpreadsheetDataIntoModel(model, "someKey");
        verify(_factory);
        verify(_dao);

        assertNull(model.get(MODEL_QUESTION_TEXT));
        assertNull(model.get(MODEL_QUESTION_LINK));
        assertNull(model.get(MODEL_USERNAME));
        assertNull(model.get(MODEL_USER_ID));
    }

    public void testLoadSpreadsheetDataClearCache() throws Exception {
        getRequest().setParameter(CommunityQuestionPromoController.CACHE_CLEAR_PARAM, "1");
        getRequest().setServerName("dev.greatschools.net");

        _factory.setWorksheetName("od6");
        expect(_factory.getGoogleSpreadsheetDao()).andReturn(_dao);
        replay(_factory);

        _dao.clearCache();
        expect(_dao.getFirstRowByKey(WORKSHEET_PRIMARY_ID_COL, DEFAULT_CODE)).andReturn(null);
        replay(_dao);

        _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_dao);
    }

    public void testGetWorksheet() {
        getRequest().setServerName("dev.greatschools.net");
        assertEquals("od6", _controller.getWorksheet(getRequest()));

        getRequest().setServerName("www.greatschools.net");
        assertEquals("od4", _controller.getWorksheet(getRequest()));

        getRequest().setParameter("worksheet", "od5");
        assertEquals("od5", _controller.getWorksheet(getRequest()));
    }

    public void testGetCode() {
        assertEquals("expect default", DEFAULT_CODE, _controller.getCode(getRequest()));

        getRequest().setParameter("code", "newCode");
        assertEquals("expect override by param", "newCode", _controller.getCode(getRequest()));
    }
}
