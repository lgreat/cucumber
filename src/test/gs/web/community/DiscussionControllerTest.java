package gs.web.community;

import gs.web.BaseControllerTestCase;
import gs.data.content.cms.ICmsDiscussionBoardDao;
import gs.data.content.cms.CmsDiscussionBoard;
import gs.data.content.cms.ContentKey;
import gs.data.content.cms.CmsTopicCenter;
import gs.data.community.*;
import gs.data.cms.IPublicationDao;
import gs.web.geo.StateSpecificFooterHelper;
import org.springframework.web.servlet.ModelAndView;

import static gs.data.community.IDiscussionReplyDao.DiscussionReplySort;
import static org.easymock.classextension.EasyMock.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class DiscussionControllerTest extends BaseControllerTestCase {
    DiscussionController _controller;
    ICmsDiscussionBoardDao _discussionBoardDao;
    IDiscussionDao _discussionDao;
    IDiscussionReplyDao _discussionReplyDao;
    IPublicationDao _publicationDao;
    IUserDao _userDao;
    IReportedEntityDao _reportedEntityDao;
    StateSpecificFooterHelper _stateSpecificFooterHelper;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new DiscussionController();
        _controller.setViewName("myView");

        _discussionBoardDao = createStrictMock(ICmsDiscussionBoardDao.class);
        _publicationDao = createStrictMock(IPublicationDao.class);
        _discussionDao = createStrictMock(IDiscussionDao.class);
        _discussionReplyDao = createStrictMock(IDiscussionReplyDao.class);
        _userDao = createStrictMock(IUserDao.class);
        _reportedEntityDao = createStrictMock(IReportedEntityDao.class);
        _stateSpecificFooterHelper = createStrictMock(StateSpecificFooterHelper.class);
        _controller.setCmsDiscussionBoardDao(_discussionBoardDao);
        _controller.setPublicationDao(_publicationDao);
        _controller.setDiscussionDao(_discussionDao);
        _controller.setDiscussionReplyDao(_discussionReplyDao);
        _controller.setUserDao(_userDao);
        _controller.setReportedEntityDao(_reportedEntityDao);
        _controller.setStateSpecificFooterHelper(_stateSpecificFooterHelper);
    }

    private void replayAllMocks() {
        replayMocks(_discussionBoardDao, _discussionDao, _discussionReplyDao, _publicationDao, _userDao, _reportedEntityDao, _stateSpecificFooterHelper);
    }

    private void verifyAllMocks() {
        verifyMocks(_discussionBoardDao, _discussionDao, _discussionReplyDao, _publicationDao, _userDao, _reportedEntityDao, _stateSpecificFooterHelper);
    }

    private void resetAllMocks() {
        resetMocks(_discussionBoardDao, _discussionDao, _discussionReplyDao, _publicationDao, _userDao, _reportedEntityDao, _stateSpecificFooterHelper);
    }

    public void testBasics() {
        assertEquals("myView", _controller.getViewName());
        assertSame(_discussionBoardDao, _controller.getCmsDiscussionBoardDao());
        assertSame(_discussionDao, _controller.getDiscussionDao());
        assertSame(_discussionReplyDao, _controller.getDiscussionReplyDao());
        assertSame(_publicationDao, _controller.getPublicationDao());
        assertSame(_reportedEntityDao, _controller.getReportedEntityDao());
        assertSame(_stateSpecificFooterHelper, _controller.getStateSpecificFooterHelper());
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

    public void testNoContent() {
        replayAllMocks();
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();

        assertNotNull(mAndV);
        assertEquals(DiscussionController.VIEW_NOT_FOUND, mAndV.getViewName());
        assertNull(mAndV.getModel().get(DiscussionController.MODEL_DISCUSSION));
        assertNull(mAndV.getModel().get(DiscussionController.MODEL_DISCUSSION_BOARD));
        assertNull(mAndV.getModel().get(DiscussionController.MODEL_TOPIC_CENTER));
    }

    public void testBadContentId() {
        getRequest().setParameter("content", "foo_bar");
        
        replayAllMocks();
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();

        assertNotNull(mAndV);
        assertEquals(DiscussionController.VIEW_NOT_FOUND, mAndV.getViewName());
        assertNull(mAndV.getModel().get(DiscussionController.MODEL_DISCUSSION));
        assertNull(mAndV.getModel().get(DiscussionController.MODEL_DISCUSSION_BOARD));
        assertNull(mAndV.getModel().get(DiscussionController.MODEL_TOPIC_CENTER));
    }

    public void testNoSuchDiscussion() {
        getRequest().setParameter("content", "1");

        expect(_discussionDao.findById(1)).andReturn(null);

        replayAllMocks();
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();

        assertNotNull(mAndV);
        assertEquals(DiscussionController.VIEW_NOT_FOUND, mAndV.getViewName());
        assertNull(mAndV.getModel().get(DiscussionController.MODEL_DISCUSSION));
        assertNull(mAndV.getModel().get(DiscussionController.MODEL_DISCUSSION_BOARD));
        assertNull(mAndV.getModel().get(DiscussionController.MODEL_TOPIC_CENTER));
    }

    public void xtestWithDiscussion() {
        getRequest().setParameter("content", "1");

        Discussion discussion = new Discussion();
        discussion.setId(1);

        CmsDiscussionBoard board = new CmsDiscussionBoard();
        board.setContentKey(new ContentKey("CmsDiscussionBoard", 1081l));
        board.setTopicCenterId(15L);
        CmsTopicCenter topicCenter = new CmsTopicCenter();
        expect(_discussionDao.findById(1)).andReturn(discussion);
        expect(_discussionBoardDao.get(discussion.getBoardId())).andReturn(board);
        expect(_publicationDao
                .populateByContentId(eq(board.getTopicCenterId()), isA(CmsTopicCenter.class))).andReturn(topicCenter);
        expect(_discussionReplyDao.getRepliesForPage
                (discussion, 1, DiscussionController.DEFAULT_PAGE_SIZE, DiscussionReplySort.NEWEST_FIRST))
                .andReturn(new ArrayList<DiscussionReply>(0));
        expect(_discussionReplyDao.getTotalReplies(discussion)).andReturn(0);
        replayAllMocks();
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();

        assertNotNull(mAndV);
        assertEquals(discussion, mAndV.getModel().get(DiscussionController.MODEL_DISCUSSION));
        assertEquals(board, mAndV.getModel().get(DiscussionController.MODEL_DISCUSSION_BOARD));
        assertEquals(topicCenter, mAndV.getModel().get(DiscussionController.MODEL_TOPIC_CENTER));
        assertEquals(1, mAndV.getModel().get(DiscussionController.MODEL_TOTAL_PAGES));
        assertEquals(0, mAndV.getModel().get(DiscussionController.MODEL_TOTAL_REPLIES));
    }
    
    public void testGetPage() {
        assertEquals("Expect default page to be 1", 1, _controller.getPageNumber(getRequest(), null, 1, null, false));
        getRequest().setParameter(DiscussionController.PARAM_PAGE, "1");
        assertEquals(1, _controller.getPageNumber(getRequest(), null, 1, null, false));
        getRequest().setParameter(DiscussionController.PARAM_PAGE, "51");
        assertEquals(51, _controller.getPageNumber(getRequest(), null, 1, null, false));
        getRequest().setParameter(DiscussionController.PARAM_PAGE, "foo");
        assertEquals(1, _controller.getPageNumber(getRequest(), null, 1, null, false));
        getRequest().setParameter(DiscussionController.PARAM_PAGE, "-1");
        assertEquals("Any integer should be accepted?", -1, _controller.getPageNumber(getRequest(), null, 1, null, false));
    }

    public void testGetPageSize() {
        assertEquals(DiscussionController.DEFAULT_PAGE_SIZE, _controller.getPageSize(getRequest(), false));
    }

    public void testGetDiscussionSortFromString() {
        assertEquals(DiscussionReplySort.NEWEST_FIRST, _controller.getReplySortFromString("newest_first"));
        assertEquals(DiscussionReplySort.OLDEST_FIRST, _controller.getReplySortFromString("oldest_first"));
        assertEquals(DiscussionReplySort.NEWEST_FIRST, _controller.getReplySortFromString("something_else"));
        assertEquals(DiscussionReplySort.NEWEST_FIRST, _controller.getReplySortFromString(null));
    }

    public void testGetNoRepliesForPage() {
        Discussion discussion = new Discussion();

        expect(_discussionReplyDao.getRepliesForPage(discussion, 1, 5, DiscussionReplySort.NEWEST_FIRST, false))
                .andReturn(new ArrayList<DiscussionReply>(0));

        replayAllMocks();
        List<DiscussionReply> replies = _controller.getRepliesForPage(discussion, 1, 5, DiscussionReplySort.NEWEST_FIRST, false);
        verifyAllMocks();

        assertNotNull(replies);
        assertEquals(0, replies.size());
    }

    public void testGetSomeRepliesForPage() {
        Discussion discussion = new Discussion();

        List<DiscussionReply> replies = new ArrayList<DiscussionReply>(5);
        replies.add(new DiscussionReply());
        replies.add(new DiscussionReply());
        replies.add(new DiscussionReply());
        replies.add(new DiscussionReply());
        replies.add(new DiscussionReply());

        expect(_discussionReplyDao.getRepliesForPage(discussion, 7, 10, DiscussionReplySort.NEWEST_FIRST, false))
                .andReturn(replies);

        replayAllMocks();
        List<DiscussionReply> rval = _controller.getRepliesForPage(discussion, 7, 10, DiscussionReplySort.NEWEST_FIRST, false);
        verifyAllMocks();

        assertNotNull(rval);
        assertEquals(5, rval.size());
    }

    public void testGetNoTotalReplies() {
        Discussion discussion = new Discussion();

        expect(_discussionReplyDao.getTotalReplies(discussion, false)).andReturn(0);

        replayAllMocks();
        int totalDiscussions = _controller.getTotalReplies(discussion, false);
        verifyAllMocks();

        assertEquals(0, totalDiscussions);
    }

    public void testGetSomeTotalReplies() {
        Discussion discussion = new Discussion();

        expect(_discussionReplyDao.getTotalReplies(discussion, false)).andReturn(17);

        replayAllMocks();
        int totalDiscussions = _controller.getTotalReplies(discussion, false);
        verifyAllMocks();

        assertEquals(17, totalDiscussions);
    }

}
