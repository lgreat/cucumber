package gs.web.community;

import gs.data.util.google.GoogleSpreadsheetDao;
import gs.data.util.google.GoogleSpreadsheetInfo;
import gs.data.util.table.HashMapTableRow;
import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableRow;
import gs.web.BaseControllerTestCase;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static gs.web.community.CommunityQuestionPromoController.*;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
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
        assertEquals("Expect default value", "http://community.greatschools.org", model.get(MODEL_QUESTION_LINK));
        assertEquals("Expect default value", DEFAULT_QUESTION_LINK_TEXT,
                model.get(MODEL_QUESTION_LINK_TEXT));
        assertEquals("Expect default value", "http://community.greatschools.org/members",
                model.get(MODEL_MEMBER_URL));
        assertEquals("Expect default value", "Avatar", model.get(MODEL_AVATAR_ALT));
        assertEquals("Expect default value", "/res/img/community/avatar_40x40.gif",
                model.get(MODEL_AVATAR_URL));

        model.put(MODEL_QUESTION_LINK, "/relative-url");
        model.put(MODEL_USERNAME, "username");
        model.put(MODEL_USER_ID, "15");

        _controller.addExtraInfoToModel(model, getRequest());

        assertEquals("Expect relative url converted to absolute",
                "http://community.greatschools.org/relative-url", model.get(MODEL_QUESTION_LINK));
        assertEquals("Expect member url", "http://community.greatschools.org/members/username",
                model.get(MODEL_MEMBER_URL));
        assertEquals("Expect avatar alt equal to username", "username", model.get(MODEL_AVATAR_ALT));
        assertEquals("Expect avatar image to point to user with id 15",
                "http://community.greatschools.org/avatar?id=15&width=40&height=40",
                model.get(MODEL_AVATAR_URL));

        model.put(MODEL_QUESTION_LINK, "http://absolute.url.com");

        _controller.addExtraInfoToModel(model, getRequest());

        assertEquals("Expect absolute url to remain absolute", "http://absolute.url.com", model.get(MODEL_QUESTION_LINK));
    }

    public void testHandleRequestInternal() throws Exception {
        GoogleSpreadsheetDao dao = (GoogleSpreadsheetDao) _dao;

        getRequest().setServerName("dev.greatschools.org");
        expect(dao.getSpreadsheetInfo()).andReturn(new GoogleSpreadsheetInfo(null,null,null,"od6"));

        expect(dao.getRandomRowByKey(WORKSHEET_PRIMARY_ID_COL, DEFAULT_CODE)).andReturn(null);
        replay(dao);

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());

        Map model = mAndV.getModel();

        assertEquals("Expect default value", "http://community.dev.greatschools.org", model.get(MODEL_QUESTION_LINK));
        assertEquals("Expect default value", DEFAULT_QUESTION_LINK_TEXT,
                model.get(MODEL_QUESTION_LINK_TEXT));
        assertEquals("Expect default value", "http://community.dev.greatschools.org/members",
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

        HttpServletRequest localRequest = createMock(HttpServletRequest.class);

        expect(localRequest.getParameter(WORKSHEET_PRIMARY_ID_COL)).andReturn("someKey");
        replay(localRequest);
        expect(_dao.getRandomRowByKey(WORKSHEET_PRIMARY_ID_COL, "someKey")).andReturn(row);
        replay(_dao);

        _controller.loadSpreadsheetDataIntoModel(localRequest, model);
        verify(_dao);
        verify(localRequest);

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

        HttpServletRequest localRequest = createMock(HttpServletRequest.class);

        expect(localRequest.getParameter(WORKSHEET_PRIMARY_ID_COL)).andReturn("someKey");
        replay(localRequest);
        expect(_dao.getRandomRowByKey(WORKSHEET_PRIMARY_ID_COL, "someKey")).andReturn(row);
        replay(_dao);

        _controller.loadSpreadsheetDataIntoModel(localRequest, model);
        verify(_dao);
        verify(localRequest);

        assertNull(model.get(MODEL_QUESTION_TEXT));
        assertNull(model.get(MODEL_QUESTION_LINK));
        assertNull(model.get(MODEL_USERNAME));
        assertNull(model.get(MODEL_USER_ID));
    }

    public void testLoadSpreadsheetDataNull() {
        // shouldn't crash
        Map<String, Object> model = new HashMap<String, Object>();

        HttpServletRequest localRequest = createMock(HttpServletRequest.class);

        expect(localRequest.getParameter(WORKSHEET_PRIMARY_ID_COL)).andReturn("someKey");
        replay(localRequest);
        expect(_dao.getRandomRowByKey(WORKSHEET_PRIMARY_ID_COL, "someKey")).andReturn(null);
        replay(_dao);

        _controller.loadSpreadsheetDataIntoModel(localRequest, model);

        verify(localRequest);
        verify(_dao);
        
        assertNull(model.get(MODEL_QUESTION_TEXT));
        assertNull(model.get(MODEL_QUESTION_LINK));
        assertNull(model.get(MODEL_USERNAME));
        assertNull(model.get(MODEL_USER_ID));
    }

    public void testGetWorksheet() {
        CommunityQuestionPromoController controller = (CommunityQuestionPromoController) getApplicationContext().getBean(CommunityQuestionPromoController.BEAN_ID);
        getRequest().setServerName("dev.greatschools.org");
        assertEquals("od6", controller.getWorksheet(getRequest()));

        getRequest().setServerName("sfgate.dev.greatschools.org");
        assertEquals("od6", controller.getWorksheet(getRequest()));

        getRequest().setServerName("aroy.dev.greatschools.org");
        assertEquals("od6", controller.getWorksheet(getRequest()));

        getRequest().setServerName("www.greatschools.org");
        assertEquals("od4", controller.getWorksheet(getRequest()));

        getRequest().setServerName("sfgate.greatschools.org");
        assertEquals("od4", controller.getWorksheet(getRequest()));

        getRequest().setServerName("staging.greatschools.org");
        assertEquals("oda", controller.getWorksheet(getRequest()));

        getRequest().setServerName("sfgate.staging.greatschools.org");
        assertEquals("oda", controller.getWorksheet(getRequest()));

        getRequest().setParameter("worksheet", "od5");
        assertEquals("Expect parameter to override", "od5", controller.getWorksheet(getRequest()));
    }

    public void testGetCode() {
        assertEquals("expect default", DEFAULT_CODE, _controller.getCode(getRequest()));

        getRequest().setParameter("code", "newCode");
        assertEquals("expect override by param", "newCode", _controller.getCode(getRequest()));
    }

    public void testInjectWorksheetName() {
        CommunityQuestionPromoController controller = (CommunityQuestionPromoController) getApplicationContext().getBean(CommunityQuestionPromoController.BEAN_ID);
        GoogleSpreadsheetDao dao = (GoogleSpreadsheetDao) controller.getTableDao();

        getRequest().setServerName("dev.greatschools.org");
        controller.injectWorksheetName(getRequest());
        assertEquals("od6", dao.getSpreadsheetInfo().getWorksheetName());
    }
}
