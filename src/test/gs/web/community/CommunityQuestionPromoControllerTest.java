package gs.web.community;

import gs.web.BaseControllerTestCase;
import gs.web.util.google.ICachedGoogleSpreadsheetDao;

import java.util.Map;
import java.util.HashMap;

import static gs.web.community.CommunityQuestionPromoController.*;
import static org.easymock.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CommunityQuestionPromoControllerTest extends BaseControllerTestCase {
    private CommunityQuestionPromoController _controller;
    private ICachedGoogleSpreadsheetDao _dao;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new CommunityQuestionPromoController();

        _dao = createMock(ICachedGoogleSpreadsheetDao.class);
        _controller.setCachedGoogleSpreadsheetDao(_dao);
    }

    public void testBasic() {
        assertSame(_dao, _controller.getCachedGoogleSpreadsheetDao());
        _controller.setViewName("aView");
        assertEquals("aView", _controller.getViewName());
    }

    public void testLoadSpreadsheetData() {
        Map<String, Object> model = new HashMap<String, Object>();

        Map<String, String> dataMap = new HashMap<String, String>();

        dataMap.put(WORKSHEET_PRIMARY_ID_COL, "someKey");
        dataMap.put("text", "text text");
        dataMap.put("link", "link link");
        dataMap.put("username", "user name");
        dataMap.put("memberid", "member id");

        expect(_dao.getDataFromRow("http://someUrl",
                WORKSHEET_PRIMARY_ID_COL,
                "someKey", ICachedGoogleSpreadsheetDao.HOUR)).andReturn(dataMap);
        replay(_dao);

        _controller.loadSpreadsheetDataIntoModel(model, "http://someUrl", "someKey");
        verify(_dao);

        assertEquals("text text", model.get(MODEL_QUESTION_TEXT));
        assertEquals("link link", model.get(MODEL_QUESTION_LINK));
        assertEquals("user name", model.get(MODEL_USERNAME));
        assertEquals("member id", model.get(MODEL_USER_ID));
    }

    public void testLoadSpreadsheetDataEmpty() {
        // shouldn't crash
        Map<String, Object> model = new HashMap<String, Object>();

        Map<String, String> dataMap = new HashMap<String, String>();

        expect(_dao.getDataFromRow("http://someUrl",
                WORKSHEET_PRIMARY_ID_COL,
                "someKey", ICachedGoogleSpreadsheetDao.HOUR)).andReturn(dataMap);
        replay(_dao);

        _controller.loadSpreadsheetDataIntoModel(model, "http://someUrl", "someKey");
        verify(_dao);

        assertNull(model.get(MODEL_QUESTION_TEXT));
        assertNull(model.get(MODEL_QUESTION_LINK));
        assertNull(model.get(MODEL_USERNAME));
        assertNull(model.get(MODEL_USER_ID));
    }

    public void testLoadSpreadsheetDataNull() {
        // shouldn't crash
        Map<String, Object> model = new HashMap<String, Object>();

        expect(_dao.getDataFromRow("http://someUrl",
                WORKSHEET_PRIMARY_ID_COL,
                "someKey", ICachedGoogleSpreadsheetDao.HOUR)).andReturn(null);
        replay(_dao);

        _controller.loadSpreadsheetDataIntoModel(model, "http://someUrl", "someKey");
        verify(_dao);

        assertNull(model.get(MODEL_QUESTION_TEXT));
        assertNull(model.get(MODEL_QUESTION_LINK));
        assertNull(model.get(MODEL_USERNAME));
        assertNull(model.get(MODEL_USER_ID));
    }

    public void testLoadSpreadsheetDataClearCache() throws Exception {
        getRequest().setParameter(CommunityQuestionPromoController.CACHE_CLEAR_PARAM, "1");
        getRequest().setParameter("worksheet", "od7");
        String worksheet = WORKSHEET_PREFIX + "/" +
                WORKSHEET_KEY + "/" +
                WORKSHEET_VISIBILITY + "/" +
                WORKSHEET_PROJECTION + "/" + "od7";

        _dao.clearCache();
        expect(_dao.getDataFromRow(worksheet,
                WORKSHEET_PRIMARY_ID_COL,
                DEFAULT_CODE, ICachedGoogleSpreadsheetDao.HOUR)).andReturn(null);
        replay(_dao);

        _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_dao);
    }

    public void testGetWorksheetUrl() {
        String baseWorksheet = CommunityQuestionPromoController.WORKSHEET_PREFIX + "/" +
                CommunityQuestionPromoController.WORKSHEET_KEY + "/" +
                CommunityQuestionPromoController.WORKSHEET_VISIBILITY + "/" +
                CommunityQuestionPromoController.WORKSHEET_PROJECTION + "/";

        getRequest().setServerName("localhost");
        assertEquals(baseWorksheet + "od6", _controller.getWorksheetUrl(getRequest()));

        getRequest().setServerName("dev.greatschools.net");
        assertEquals(baseWorksheet + "od6", _controller.getWorksheetUrl(getRequest()));

        getRequest().setServerName("yahooed.dev.greatschools.net");
        assertEquals(baseWorksheet + "od6", _controller.getWorksheetUrl(getRequest()));

        getRequest().setServerName("staging.greatschools.net");
        assertEquals(baseWorksheet + "od4", _controller.getWorksheetUrl(getRequest()));

        getRequest().setServerName("rithmatic.greatschools.net");
        assertEquals(baseWorksheet + "od4", _controller.getWorksheetUrl(getRequest()));

        getRequest().setServerName("sfgate.staging.greatschools.net");
        assertEquals(baseWorksheet + "od4", _controller.getWorksheetUrl(getRequest()));

        getRequest().setServerName("sfgate.greatschools.net");
        assertEquals(baseWorksheet + "od4", _controller.getWorksheetUrl(getRequest()));

        getRequest().setServerName("www.greatschools.net");
        assertEquals(baseWorksheet + "od4", _controller.getWorksheetUrl(getRequest()));

        getRequest().setParameter("worksheet", "lala");
        assertEquals("expect override by param",
                baseWorksheet + "lala", _controller.getWorksheetUrl(getRequest()));
    }

    public void testGetWorksheetKey() {
        assertEquals("expect default", WORKSHEET_KEY, _controller.getWorksheetKey(getRequest()));

        getRequest().setParameter("key", "newKey");
        assertEquals("expect override by param", "newKey", _controller.getWorksheetKey(getRequest()));
    }

    public void testGetCode() {
        assertEquals("expect default", DEFAULT_CODE, _controller.getCode(getRequest()));

        getRequest().setParameter("code", "newCode");
        assertEquals("expect override by param", "newCode", _controller.getCode(getRequest()));
    }
}
