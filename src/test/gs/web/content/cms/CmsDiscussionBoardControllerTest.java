package gs.web.content.cms;

import gs.web.BaseControllerTestCase;
import gs.data.content.cms.ICmsDiscussionBoardDao;
import gs.data.content.cms.CmsDiscussionBoard;
import gs.data.content.cms.CmsTopicCenter;
import gs.data.cms.IPublicationDao;
import org.springframework.web.servlet.ModelAndView;

import static org.easymock.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CmsDiscussionBoardControllerTest extends BaseControllerTestCase {
    CmsDiscussionBoardController _controller;
    ICmsDiscussionBoardDao _discussionBoardDao;
    IPublicationDao _publicationDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new CmsDiscussionBoardController();
        _controller.setViewName("myView");

        _discussionBoardDao = createStrictMock(ICmsDiscussionBoardDao.class);
        _publicationDao = createStrictMock(IPublicationDao.class);
        _controller.setCmsDiscussionBoardDao(_discussionBoardDao);
        _controller.setPublicationDao(_publicationDao);
    }

    private void replayAllMocks() {
        replayMocks(_discussionBoardDao, _publicationDao);
    }

    private void verifyAllMocks() {
        verifyMocks(_discussionBoardDao, _publicationDao);
    }

    public void testBasics() {
        assertEquals("myView", _controller.getViewName());
        assertSame(_discussionBoardDao, _controller.getCmsDiscussionBoardDao());
        assertSame(_publicationDao, _controller.getPublicationDao());
    }

    public void testNoContent() throws Exception {
        replayAllMocks();
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();

        assertNotNull(mAndV);
        assertEquals(CmsDiscussionBoardController.VIEW_NOT_FOUND, mAndV.getViewName());
        assertNull(mAndV.getModel().get(CmsDiscussionBoardController.MODEL_DISCUSSION_BOARD));
        assertNull(mAndV.getModel().get(CmsDiscussionBoardController.MODEL_TOPIC_CENTER));
    }

    public void testBadContent() throws Exception {
        getRequest().setParameter("content", "Not_a_Number");

        replayAllMocks();
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();

        assertNotNull(mAndV);
        assertEquals(CmsDiscussionBoardController.VIEW_NOT_FOUND, mAndV.getViewName());
        assertNull(mAndV.getModel().get(CmsDiscussionBoardController.MODEL_DISCUSSION_BOARD));
        assertNull(mAndV.getModel().get(CmsDiscussionBoardController.MODEL_TOPIC_CENTER));
    }

    public void testWithBoard() throws Exception {
        getRequest().setParameter("content", "1");
        CmsDiscussionBoard board = new CmsDiscussionBoard();
        expect(_discussionBoardDao.get(1l)).andReturn(board);
        CmsTopicCenter topicCenter = new CmsTopicCenter();
        expect(_publicationDao.populateByContentId(eq(board.getTopicCenterId()), isA(CmsTopicCenter.class)))
                .andReturn(topicCenter);

        replayAllMocks();
        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verifyAllMocks();

        assertEquals("myView", mAndV.getViewName());
        assertNotNull(mAndV.getModel().get(CmsDiscussionBoardController.MODEL_DISCUSSION_BOARD));
        assertSame(board, mAndV.getModel().get(CmsDiscussionBoardController.MODEL_DISCUSSION_BOARD));
        assertNotNull(mAndV.getModel().get(CmsDiscussionBoardController.MODEL_TOPIC_CENTER));
        assertSame(topicCenter, mAndV.getModel().get(CmsDiscussionBoardController.MODEL_TOPIC_CENTER));
    }
}
