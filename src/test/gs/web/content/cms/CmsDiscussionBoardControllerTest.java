package gs.web.content.cms;

import gs.web.BaseControllerTestCase;
import gs.web.util.PageHelper;
import gs.web.community.DiscussionFacade;
import gs.data.content.cms.ICmsDiscussionBoardDao;
import gs.data.content.cms.CmsDiscussionBoard;
import gs.data.content.cms.ContentKey;
import gs.data.cms.IPublicationDao;
import gs.data.community.*;
import org.springframework.web.servlet.ModelAndView;

import static gs.data.community.IDiscussionDao.DiscussionSort;

import static org.easymock.EasyMock.*;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class CmsDiscussionBoardControllerTest extends BaseControllerTestCase {
    CmsDiscussionBoardController _controller;
    ICmsDiscussionBoardDao _discussionBoardDao;
    IDiscussionDao _discussionDao;
    IDiscussionReplyDao _discussionReplyDao;
    IPublicationDao _publicationDao;
    IUserDao _userDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new CmsDiscussionBoardController();
        _controller.setViewName("myView");

        _discussionBoardDao = createStrictMock(ICmsDiscussionBoardDao.class);
        _publicationDao = createStrictMock(IPublicationDao.class);
        _discussionDao = createStrictMock(IDiscussionDao.class);
        _discussionReplyDao = createStrictMock(IDiscussionReplyDao.class);
        _userDao = createStrictMock(IUserDao.class);
        _controller.setCmsDiscussionBoardDao(_discussionBoardDao);
        _controller.setPublicationDao(_publicationDao);
        _controller.setDiscussionDao(_discussionDao);
        _controller.setDiscussionReplyDao(_discussionReplyDao);
        _controller.setUserDao(_userDao);

        PageHelper pageHelper = new PageHelper(_sessionContext, _request);
        _request.setAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME, pageHelper);
    }

    private void replayAllMocks() {
        replayMocks(_discussionBoardDao, _discussionDao, _discussionReplyDao, _publicationDao, _userDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_discussionBoardDao, _discussionDao, _discussionReplyDao, _publicationDao, _userDao);
    }

    public void testBasics() {
        assertEquals("myView", _controller.getViewName());
        assertSame(_discussionBoardDao, _controller.getCmsDiscussionBoardDao());
        assertSame(_discussionDao, _controller.getDiscussionDao());
        assertSame(_discussionReplyDao, _controller.getDiscussionReplyDao());
        assertSame(_publicationDao, _controller.getPublicationDao());
        assertSame(_userDao, _controller.getUserDao());
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
        //CmsTopicCenter topicCenter = new CmsTopicCenter();
        //expect(_publicationDao.populateByContentId(eq(board.getTopicCenterId()), isA(CmsTopicCenter.class)))
        //        .andReturn(topicCenter);

        expect(_discussionDao.getDiscussionsForPage
                (board, 1, CmsDiscussionBoardController.DEFAULT_PAGE_SIZE, DiscussionSort.RECENT_ACTIVITY, false))
                .andReturn(new ArrayList<Discussion>(0));

        expect(_discussionDao.getTotalDiscussions(board, false)).andReturn(0);

        _userDao.populateWithUsers(isA(List.class));
        
        replayAllMocks();
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();

        assertEquals("myView", mAndV.getViewName());
        assertNotNull(mAndV.getModel().get(CmsDiscussionBoardController.MODEL_DISCUSSION_BOARD));
        assertSame(board, mAndV.getModel().get(CmsDiscussionBoardController.MODEL_DISCUSSION_BOARD));
        //assertNotNull(mAndV.getModel().get(CmsDiscussionBoardController.MODEL_TOPIC_CENTER));
        //assertSame(topicCenter, mAndV.getModel().get(CmsDiscussionBoardController.MODEL_TOPIC_CENTER));
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
        assertEquals(DiscussionSort.RECENT_ACTIVITY, _controller.getDiscussionSortFromString("recent_activity"));
        assertEquals(DiscussionSort.OLDEST_FIRST, _controller.getDiscussionSortFromString("oldest_first"));
        assertEquals(DiscussionSort.RECENT_ACTIVITY, _controller.getDiscussionSortFromString("something_else"));
        assertEquals(DiscussionSort.RECENT_ACTIVITY, _controller.getDiscussionSortFromString(null));
    }

    public void testGetNoDiscussionsForPage() {
        CmsDiscussionBoard board = new CmsDiscussionBoard();

        expect(_discussionDao.getDiscussionsForPage(board, 1, 5, DiscussionSort.NEWEST_FIRST, false))
                .andReturn(new ArrayList<Discussion>(0));

        replayAllMocks();
        List discussions = _controller.getDiscussionsForPage(board, 1, 5, DiscussionSort.NEWEST_FIRST, false);
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

        expect(_discussionDao.getDiscussionsForPage(board, 7, 10, DiscussionSort.NEWEST_FIRST, false))
                .andReturn(discussions);

        replayAllMocks();
        List rval = _controller.getDiscussionsForPage(board, 7, 10, DiscussionSort.NEWEST_FIRST, false);
        verifyAllMocks();

        assertNotNull(rval);
        assertEquals(5, rval.size());
    }

    public void testGetNoTotalDiscussions() {
        CmsDiscussionBoard board = new CmsDiscussionBoard();

        expect(_discussionDao.getTotalDiscussions(board, false)).andReturn(0);

        replayAllMocks();
        long totalDiscussions = _controller.getTotalDiscussions(board, false);
        verifyAllMocks();

        assertEquals(0, totalDiscussions);
    }

    public void testGetSomeTotalDiscussions() {
        CmsDiscussionBoard board = new CmsDiscussionBoard();

        expect(_discussionDao.getTotalDiscussions(board, false)).andReturn(51);

        replayAllMocks();
        long totalDiscussions = _controller.getTotalDiscussions(board, false);
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

    public void testPopulateFacadesWithNoDiscussions() {
        CmsDiscussionBoard board = new CmsDiscussionBoard();
        List<Discussion> discussions = new ArrayList<Discussion>(0);

        replayAllMocks();
        List<DiscussionFacade> facades = _controller.populateFacades(board, discussions);
        verifyAllMocks();

        assertNotNull(facades);
        assertEquals(0, facades.size());
    }

    public void testPopulateFacades() {
        CmsDiscussionBoard board = new CmsDiscussionBoard();
        List<Discussion> discussions = new ArrayList<Discussion>(1);
        Discussion discussion = new Discussion();
        discussions.add(discussion);

        List<DiscussionReply> replies = new ArrayList<DiscussionReply>(1);
        DiscussionReply reply1 = new DiscussionReply();
        replies.add(reply1);

        expect(_discussionReplyDao.getRepliesForPage
                (discussion, 1, 2, IDiscussionReplyDao.DiscussionReplySort.NEWEST_FIRST)).andReturn(replies);
        expect(_discussionReplyDao.getTotalReplies
                (discussion)).andReturn(3);

        replayAllMocks();
        List<DiscussionFacade> facades = _controller.populateFacades(board, discussions);
        verifyAllMocks();

        assertNotNull("Expect a facade to be returned", facades);
        assertEquals("Expect a facade to be returned", 1, facades.size());
        assertNotNull("Expect the facade to have a list of replies", facades.get(0).getReplies());
        assertEquals("Expect the facade to have exactly 1 reply", 1, facades.get(0).getReplies().size());
        assertEquals("Expect the facade to report 3 total replies", 3, facades.get(0).getTotalReplies());
        assertSame("Expect the reply to be the same as the one returned from the dao",
                reply1, facades.get(0).getReplies().get(0));
    }
}
