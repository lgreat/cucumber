package gs.web.content.cms;

import gs.web.BaseControllerTestCase;
import gs.data.content.cms.ICmsDiscussionBoardDao;
import gs.data.content.cms.CmsDiscussionBoard;
import gs.data.content.cms.CmsTopicCenter;
import gs.data.content.cms.ContentKey;
import gs.data.cms.IPublicationDao;
import gs.data.community.IDiscussionDao;
import gs.data.community.Discussion;
import org.springframework.web.servlet.ModelAndView;

import static gs.data.community.IDiscussionDao.DiscussionSort;

import static org.easymock.EasyMock.*;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CmsDiscussionBoardControllerTest extends BaseControllerTestCase {
    CmsDiscussionBoardController _controller;
    ICmsDiscussionBoardDao _discussionBoardDao;
    IDiscussionDao _discussionDao;
    IPublicationDao _publicationDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new CmsDiscussionBoardController();
        _controller.setViewName("myView");

        _discussionBoardDao = createStrictMock(ICmsDiscussionBoardDao.class);
        _publicationDao = createStrictMock(IPublicationDao.class);
        _discussionDao = createStrictMock(IDiscussionDao.class);
        _controller.setCmsDiscussionBoardDao(_discussionBoardDao);
        _controller.setPublicationDao(_publicationDao);
        _controller.setDiscussionDao(_discussionDao);
    }

    private void replayAllMocks() {
        replayMocks(_discussionBoardDao, _discussionDao, _publicationDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_discussionBoardDao, _discussionDao, _publicationDao);
    }

    public void testBasics() {
        assertEquals("myView", _controller.getViewName());
        assertSame(_discussionBoardDao, _controller.getCmsDiscussionBoardDao());
        assertSame(_discussionDao, _controller.getDiscussionDao());
        assertSame(_publicationDao, _controller.getPublicationDao());
    }

    public void testNoContent() {
        replayAllMocks();
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();

        assertNotNull(mAndV);
        assertEquals(CmsDiscussionBoardController.VIEW_NOT_FOUND, mAndV.getViewName());
        assertNull(mAndV.getModel().get(CmsDiscussionBoardController.MODEL_DISCUSSION_BOARD));
        assertNull(mAndV.getModel().get(CmsDiscussionBoardController.MODEL_TOPIC_CENTER));
    }

    public void testBadContent() {
        getRequest().setParameter("content", "Not_a_Number");

        replayAllMocks();
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();

        assertNotNull(mAndV);
        assertEquals(CmsDiscussionBoardController.VIEW_NOT_FOUND, mAndV.getViewName());
        assertNull(mAndV.getModel().get(CmsDiscussionBoardController.MODEL_DISCUSSION_BOARD));
        assertNull(mAndV.getModel().get(CmsDiscussionBoardController.MODEL_TOPIC_CENTER));
    }

    public void testNoSuchBoard() {
        getRequest().setParameter("content", "1");
        expect(_discussionBoardDao.get(1l)).andReturn(null);

        replayAllMocks();
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();

        assertEquals("Expect 404 when can't find discussion board",
                CmsDiscussionBoardController.VIEW_NOT_FOUND, mAndV.getViewName());
        assertNull(mAndV.getModel().get(CmsDiscussionBoardController.MODEL_DISCUSSION_BOARD));
        assertNull(mAndV.getModel().get(CmsDiscussionBoardController.MODEL_TOPIC_CENTER));
    }

    public void testWithBoard() {
        getRequest().setParameter("content", "1");
        CmsDiscussionBoard board = new CmsDiscussionBoard();
        board.setContentKey(new ContentKey("CmsDiscussionBoard", 1l));
        expect(_discussionBoardDao.get(1l)).andReturn(board);
        CmsTopicCenter topicCenter = new CmsTopicCenter();
        expect(_publicationDao.populateByContentId(eq(board.getTopicCenterId()), isA(CmsTopicCenter.class)))
                .andReturn(topicCenter);

        expect(_discussionDao.getDiscussionsForPage
                (board, 1, CmsDiscussionBoardController.DEFAULT_PAGE_SIZE, DiscussionSort.NEWEST_FIRST))
                .andReturn(new ArrayList<Discussion>(0));

        expect(_discussionDao.getTotalDiscussions(board)).andReturn(0);

        replayAllMocks();
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();

        assertEquals("myView", mAndV.getViewName());
        assertNotNull(mAndV.getModel().get(CmsDiscussionBoardController.MODEL_DISCUSSION_BOARD));
        assertSame(board, mAndV.getModel().get(CmsDiscussionBoardController.MODEL_DISCUSSION_BOARD));
        assertNotNull(mAndV.getModel().get(CmsDiscussionBoardController.MODEL_TOPIC_CENTER));
        assertSame(topicCenter, mAndV.getModel().get(CmsDiscussionBoardController.MODEL_TOPIC_CENTER));
        assertNotNull(mAndV.getModel().get(CmsDiscussionBoardController.MODEL_DISCUSSION_LIST));
        assertEquals(0l, mAndV.getModel().get(CmsDiscussionBoardController.MODEL_TOTAL_DISCUSSIONS));
    }

    public void testGetPage() {
        assertEquals("Expect default page to be 1", 1, _controller.getPageNumber(getRequest()));
        getRequest().setParameter(CmsDiscussionBoardController.PARAM_PAGE, "1");
        assertEquals(1, _controller.getPageNumber(getRequest()));
        getRequest().setParameter(CmsDiscussionBoardController.PARAM_PAGE, "51");
        assertEquals(51, _controller.getPageNumber(getRequest()));
        getRequest().setParameter(CmsDiscussionBoardController.PARAM_PAGE, "foo");
        assertEquals(1, _controller.getPageNumber(getRequest()));
        getRequest().setParameter(CmsDiscussionBoardController.PARAM_PAGE, "-1");
        assertEquals("Any integer should be accepted?", -1, _controller.getPageNumber(getRequest()));
    }
    
    public void testGetPageSize() {
        assertEquals(CmsDiscussionBoardController.DEFAULT_PAGE_SIZE, _controller.getPageSize(getRequest()));
    }

    public void testGetDiscussionSortFromString() {
        assertEquals(DiscussionSort.NEWEST_FIRST, _controller.getDiscussionSortFromString("newest_first"));
        assertEquals(DiscussionSort.OLDEST_FIRST, _controller.getDiscussionSortFromString("oldest_first"));
        assertEquals(DiscussionSort.NEWEST_FIRST, _controller.getDiscussionSortFromString("something_else"));
        assertEquals(DiscussionSort.NEWEST_FIRST, _controller.getDiscussionSortFromString(null));
    }

    public void testGetNoDiscussionsForPage() {
        CmsDiscussionBoard board = new CmsDiscussionBoard();

        expect(_discussionDao.getDiscussionsForPage(board, 1, 5, DiscussionSort.NEWEST_FIRST))
                .andReturn(new ArrayList<Discussion>(0));

        replayAllMocks();
        List discussions = _controller.getDiscussionsForPage(board, 1, 5, DiscussionSort.NEWEST_FIRST);
        verifyAllMocks();

        assertNotNull(discussions);
        assertEquals(0, discussions.size());
    }

    public void testGetSomeDiscussionsForPage() {
        CmsDiscussionBoard board = new CmsDiscussionBoard();

        List<Discussion> discussions = new ArrayList<Discussion>(5);
        discussions.add(new Discussion());
        discussions.add(new Discussion());
        discussions.add(new Discussion());
        discussions.add(new Discussion());
        discussions.add(new Discussion());

        expect(_discussionDao.getDiscussionsForPage(board, 7, 10, DiscussionSort.NEWEST_FIRST))
                .andReturn(discussions);

        replayAllMocks();
        List rval = _controller.getDiscussionsForPage(board, 7, 10, DiscussionSort.NEWEST_FIRST);
        verifyAllMocks();

        assertNotNull(rval);
        assertEquals(5, rval.size());
    }

    public void testGetNoTotalDiscussions() {
        CmsDiscussionBoard board = new CmsDiscussionBoard();

        expect(_discussionDao.getTotalDiscussions(board)).andReturn(0);

        replayAllMocks();
        long totalDiscussions = _controller.getTotalDiscussions(board);
        verifyAllMocks();

        assertEquals(0, totalDiscussions);
    }

    public void testGetSomeTotalDiscussions() {
        CmsDiscussionBoard board = new CmsDiscussionBoard();

        expect(_discussionDao.getTotalDiscussions(board)).andReturn(51);

        replayAllMocks();
        long totalDiscussions = _controller.getTotalDiscussions(board);
        verifyAllMocks();

        assertEquals(51, totalDiscussions);
    }

    public void testGetTotalPages() {
        assertEquals(1, _controller.getTotalPages(5, 0));
        assertEquals(1, _controller.getTotalPages(5, 1));
        assertEquals(1, _controller.getTotalPages(5, 5));
        assertEquals(2, _controller.getTotalPages(5, 6));
        assertEquals(2, _controller.getTotalPages(5, 10));
        assertEquals(1, _controller.getTotalPages(50, 10));
        assertEquals(1, _controller.getTotalPages(50, 50));
        assertEquals(2, _controller.getTotalPages(50, 51));
    }
}
