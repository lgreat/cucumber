package gs.web.test;

import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContextUtil;
import gs.data.state.State;
import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableRow;

import static org.easymock.EasyMock.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class FirstTestScoreLandingControllerTest extends BaseControllerTestCase {
    private FirstTestScoreLandingController _controller;

    private ITableDao _tableDao;

    public void setUp() throws Exception {
        super.setUp();

        _tableDao = createStrictMock(ITableDao.class);

        _controller = new FirstTestScoreLandingController();
        _controller.setTableDao(_tableDao);
    }

    public void testBasics() {
        assertSame(_tableDao, _controller.getTableDao());
    }

    public void testHandleRequestInternal() throws Exception {
        State state = State.CA;
        String expectedId = "5";
        ITableRow row = createStrictMock(ITableRow.class);
        SessionContextUtil.getSessionContext(getRequest()).setState(state);

        expect(_tableDao.getFirstRowByKey(FirstTestScoreLandingController.STATE_COLUMN,
                state.getAbbreviation())).andReturn(row);
        expect(row.getString(FirstTestScoreLandingController.TEST_ID_COLUMN)).andReturn(expectedId);

        replay(_tableDao);
        replay(row);
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_tableDao);
        verify(row);

        assertNotNull(mAndV);
        assertNotNull(mAndV.getView());
        assertTrue(mAndV.getView() instanceof RedirectView);
        RedirectView rView = (RedirectView) mAndV.getView();
        assertEquals("/test/landing.page?state=CA&tid=5", rView.getUrl());
    }

    public void testHandleRequestInternalNullId() throws Exception {
        State state = State.AK;
        SessionContextUtil.getSessionContext(getRequest()).setState(state);

        expect(_tableDao.getFirstRowByKey(FirstTestScoreLandingController.STATE_COLUMN,
                state.getAbbreviation())).andReturn(null);

        replay(_tableDao);
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_tableDao);

        assertNotNull(mAndV);
        assertNotNull(mAndV.getView());
        assertTrue(mAndV.getView() instanceof RedirectView);
        RedirectView rView = (RedirectView) mAndV.getView();
        assertEquals("/modperl/go/AK", rView.getUrl());
    }

    public void testHandleRequestInternalWithCpn() throws Exception {
        State state = State.CA;
        String expectedId = "5";
        ITableRow row = createStrictMock(ITableRow.class);
        SessionContextUtil.getSessionContext(getRequest()).setState(state);
        getRequest().setParameter("cpn", "foobar");

        expect(_tableDao.getFirstRowByKey(FirstTestScoreLandingController.STATE_COLUMN,
                state.getAbbreviation())).andReturn(row);
        expect(row.getString(FirstTestScoreLandingController.TEST_ID_COLUMN)).andReturn(expectedId);

        replay(_tableDao);
        replay(row);
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_tableDao);
        verify(row);

        assertNotNull(mAndV);
        assertNotNull(mAndV.getView());
        assertTrue(mAndV.getView() instanceof RedirectView);
        RedirectView rView = (RedirectView) mAndV.getView();
        assertEquals("/test/landing.page?cpn=foobar&state=CA&tid=5", rView.getUrl());
    }

    public void testGetFirstTestIdForState() {
        State state = State.CA;
        String expectedId = "5";

        ITableRow row = createStrictMock(ITableRow.class);

        expect(_tableDao.getFirstRowByKey(FirstTestScoreLandingController.STATE_COLUMN,
                state.getAbbreviation())).andReturn(row);
        expect(row.getString(FirstTestScoreLandingController.TEST_ID_COLUMN)).andReturn(expectedId);

        replay(_tableDao);
        replay(row);
        String id = _controller.getFirstTestIdForState(state);
        verify(_tableDao);
        verify(row);

        assertEquals("Expect id from spreadsheet to be returned", expectedId, id);
    }

    public void testGetFirstTestIdForStateWhenIdNull() {
        State state = State.CA;

        expect(_tableDao.getFirstRowByKey(FirstTestScoreLandingController.STATE_COLUMN,
                state.getAbbreviation())).andReturn(null);

        replay(_tableDao);
        String id = _controller.getFirstTestIdForState(state);
        verify(_tableDao);

        assertNull("Expect null to be returned in error case", id);
    }

    public void testGetTestScoreLandingPagePath() {
        State state = State.CA;
        String id = "5";

        String url = _controller.getTestScoreLandingPagePath(state, id, null);

        assertEquals("/test/landing.page?state=CA&tid=5", url);
    }

    public void testGetTestScoreLandingPagePathNullId() {
        State state = State.CA;
        String id = null;

        String url = _controller.getTestScoreLandingPagePath(state, id, null);

        assertEquals("/modperl/go/CA", url);
    }

    public void testGetTestScoreLandingPagePathWithCpn() {
        State state = State.CA;
        String id = "5";

        String url = _controller.getTestScoreLandingPagePath(state, id, "foo");

        assertEquals("/test/landing.page?cpn=foo&state=CA&tid=5", url);
    }

    public void testGetTestScoreLandingPagePathNullIdWithCpn() {
        State state = State.CA;
        String id = null;

        String url = _controller.getTestScoreLandingPagePath(state, id, "bar");

        assertEquals("/modperl/go/CA?cpn=bar", url);
    }
}
