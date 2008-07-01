package gs.web.community;

import gs.web.BaseControllerTestCase;
import gs.data.util.table.ITableDao;
import gs.web.util.google.GoogleSpreadsheetDao;
import gs.data.util.table.HashMapTableRow;
import gs.data.util.table.ITableRow;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import static gs.web.community.CommunityQuestionPromoController.*;
import static org.easymock.classextension.EasyMock.*;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CommunityQuestionPromoControllerTest extends BaseControllerTestCase {
    private CommunityQuestionPromoController _controller;
    private ITableDao _dao;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new CommunityQuestionPromoController();

        _dao = createMock(GoogleSpreadsheetDao.class);
        _controller.setTableDao(_dao);
    }

    public void testBasics() {
        assertSame(_dao, _controller.getTableDao());
        _controller.setViewName("aView");
        assertEquals("aView", _controller.getViewName());
    }

    public void testAddExtraInfoToModel() {
        Map<String, Object> model = new HashMap<String, Object>();

        _controller.addExtraInfoToModel(model, getRequest());

        // test defaults
        assertEquals("Expect default value", "http://community.greatschools.net", model.get(MODEL_QUESTION_LINK));
        assertEquals("Expect default value", DEFAULT_QUESTION_LINK_TEXT,
                model.get(MODEL_QUESTION_LINK_TEXT));
        assertEquals("Expect default value", "http://community.greatschools.net/members",
                model.get(MODEL_MEMBER_URL));
        assertEquals("Expect default value", "Avatar", model.get(MODEL_AVATAR_ALT));
        assertEquals("Expect default value", "/res/img/community/avatar_40x40.gif",
                model.get(MODEL_AVATAR_URL));

        model.put(MODEL_QUESTION_LINK, "/relative-url");
        model.put(MODEL_USERNAME, "username");
        model.put(MODEL_USER_ID, "15");

        _controller.addExtraInfoToModel(model, getRequest());

        assertEquals("Expect relative url converted to absolute",
                "http://community.greatschools.net/relative-url", model.get(MODEL_QUESTION_LINK));
        assertEquals("Expect member url", "http://community.greatschools.net/members/username",
                model.get(MODEL_MEMBER_URL));
        assertEquals("Expect avatar alt equal to username", "username", model.get(MODEL_AVATAR_ALT));
        assertEquals("Expect avatar image to point to user with id 15",
                "http://community.greatschools.net/avatar?id=15&width=40&height=40",
                model.get(MODEL_AVATAR_URL));

        model.put(MODEL_QUESTION_LINK, "http://absolute.url.com");

        _controller.addExtraInfoToModel(model, getRequest());

        assertEquals("Expect absolute url to remain absolute", "http://absolute.url.com", model.get(MODEL_QUESTION_LINK));
    }

    public void testHandleRequestInternal() throws Exception {
        GoogleSpreadsheetDao dao = (GoogleSpreadsheetDao) _dao;

        getRequest().setServerName("dev.greatschools.net");
        expect(dao.getWorksheetUrl()).andReturn("google/");
        dao.setWorksheetUrl("google/od6");

        expect(dao.getRandomRowByKey(WORKSHEET_PRIMARY_ID_COL, DEFAULT_CODE)).andReturn(null);
        replay(dao);

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());

        Map model = mAndV.getModel();

        assertEquals("Expect default value", "http://community.dev.greatschools.net", model.get(MODEL_QUESTION_LINK));
        assertEquals("Expect default value", DEFAULT_QUESTION_LINK_TEXT,
                model.get(MODEL_QUESTION_LINK_TEXT));
        assertEquals("Expect default value", "http://community.dev.greatschools.net/members",
                model.get(MODEL_MEMBER_URL));
        assertEquals("Expect default value", "Avatar", model.get(MODEL_AVATAR_ALT));
        assertEquals("Expect default value", "/res/img/community/avatar_40x40.gif",
                model.get(MODEL_AVATAR_URL));

        verify(dao);
    }

    public void testLoadSpreadsheetData() {
        Map<String, Object> model = new HashMap<String, Object>();

        HashMapTableRow row = new HashMapTableRow();
        row.addCell(WORKSHEET_PRIMARY_ID_COL, "someKey");
        row.addCell("text", "text text");
        row.addCell("link", "link link");
        row.addCell("linktext", "link text");
        row.addCell("username", "user name");
        row.addCell("memberid", "member id");

        expect(_dao.getRandomRowByKey(WORKSHEET_PRIMARY_ID_COL, "someKey")).andReturn(row);
        replay(_dao);

        _controller.loadSpreadsheetDataIntoModel(model, "someKey");
        verify(_dao);

        assertEquals("text text", model.get(MODEL_QUESTION_TEXT));
        assertEquals("link link", model.get(MODEL_QUESTION_LINK));
        assertEquals("link text", model.get(MODEL_QUESTION_LINK_TEXT));
        assertEquals("user name", model.get(MODEL_USERNAME));
        assertEquals("member id", model.get(MODEL_USER_ID));
    }

    public void testLoadSpreadsheetDataEmpty() {
        // shouldn't crash
        Map<String, Object> model = new HashMap<String, Object>();

        ITableRow row = new HashMapTableRow();

        expect(_dao.getRandomRowByKey(WORKSHEET_PRIMARY_ID_COL, "someKey")).andReturn(row);
        replay(_dao);

        _controller.loadSpreadsheetDataIntoModel(model, "someKey");
        verify(_dao);

        assertNull(model.get(MODEL_QUESTION_TEXT));
        assertNull(model.get(MODEL_QUESTION_LINK));
        assertNull(model.get(MODEL_USERNAME));
        assertNull(model.get(MODEL_USER_ID));
    }

    public void testLoadSpreadsheetDataNull() {
        // shouldn't crash
        Map<String, Object> model = new HashMap<String, Object>();

        expect(_dao.getRandomRowByKey(WORKSHEET_PRIMARY_ID_COL, "someKey")).andReturn(null);
        replay(_dao);

        _controller.loadSpreadsheetDataIntoModel(model, "someKey");
        verify(_dao);

        assertNull(model.get(MODEL_QUESTION_TEXT));
        assertNull(model.get(MODEL_QUESTION_LINK));
        assertNull(model.get(MODEL_USERNAME));
        assertNull(model.get(MODEL_USER_ID));
    }

    public void testGetWorksheet() {
        getRequest().setServerName("dev.greatschools.net");
        assertEquals("od6", _controller.getWorksheet(getRequest()));

        getRequest().setServerName("sfgate.dev.greatschools.net");
        assertEquals("od6", _controller.getWorksheet(getRequest()));

        getRequest().setServerName("aroy.dev.greatschools.net");
        assertEquals("od6", _controller.getWorksheet(getRequest()));

        getRequest().setServerName("www.greatschools.net");
        assertEquals("od4", _controller.getWorksheet(getRequest()));

        getRequest().setServerName("sfgate.greatschools.net");
        assertEquals("od4", _controller.getWorksheet(getRequest()));

        getRequest().setServerName("staging.greatschools.net");
        assertEquals("oda", _controller.getWorksheet(getRequest()));

        getRequest().setServerName("sfgate.staging.greatschools.net");
        assertEquals("oda", _controller.getWorksheet(getRequest()));

        getRequest().setParameter("worksheet", "od5");
        assertEquals("Expect parameter to override", "od5", _controller.getWorksheet(getRequest()));
    }

    public void testGetCode() {
        assertEquals("expect default", DEFAULT_CODE, _controller.getCode(getRequest()));

        getRequest().setParameter("code", "newCode");
        assertEquals("expect override by param", "newCode", _controller.getCode(getRequest()));
    }

    public void testInjectWorksheetName() {
        GoogleSpreadsheetDao dao = (GoogleSpreadsheetDao) _dao;

        getRequest().setServerName("dev.greatschools.net");
        expect(dao.getWorksheetUrl()).andReturn("google/");
        dao.setWorksheetUrl("google/od6");
        replay(dao);

        _controller.injectWorksheetName(getRequest());
        verify(dao);

        reset(dao);

        expect(dao.getWorksheetUrl()).andReturn("google/od6");
        replay(dao);

        _controller.injectWorksheetName(getRequest());
        verify(dao);

    }
}
