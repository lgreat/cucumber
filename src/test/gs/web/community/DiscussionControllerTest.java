package gs.web.community;

import gs.web.BaseControllerTestCase;
import gs.web.content.cms.CmsDiscussionBoardController;
import gs.data.content.cms.ICmsDiscussionBoardDao;
import gs.data.community.IDiscussionDao;
import gs.data.cms.IPublicationDao;
import static org.easymock.EasyMock.createStrictMock;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class DiscussionControllerTest extends BaseControllerTestCase {
    DiscussionController _controller;
    ICmsDiscussionBoardDao _discussionBoardDao;
    IDiscussionDao _discussionDao;
    IPublicationDao _publicationDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new DiscussionController();
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
